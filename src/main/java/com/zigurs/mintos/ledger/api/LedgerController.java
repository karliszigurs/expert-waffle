package com.zigurs.mintos.ledger.api;

import com.zigurs.mintos.ledger.api.exceptions.BadRequestException;
import com.zigurs.mintos.ledger.api.exceptions.NotFoundException;
import com.zigurs.mintos.ledger.api.requests.AccountsRequest;
import com.zigurs.mintos.ledger.api.requests.HistoryRequest;
import com.zigurs.mintos.ledger.api.requests.TransferRequest;
import com.zigurs.mintos.ledger.api.responses.AccountView;
import com.zigurs.mintos.ledger.api.responses.HistoryResponse;
import com.zigurs.mintos.ledger.api.responses.TransactionView;
import com.zigurs.mintos.ledger.data.AccountsRepository;
import com.zigurs.mintos.ledger.data.ClientsRepository;
import com.zigurs.mintos.ledger.data.TransactionsRepository;
import com.zigurs.mintos.ledger.model.Account;
import com.zigurs.mintos.ledger.model.Transaction;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

/**
 * Main controller following Spring Boot patterns.
 * <p>
 * TODO: On second thoughts I could have split it into three controllers - one per endpoint. Leaving this as an exercise for cleanup.
 */
@RestController
public class LedgerController {

    // Default pagination support
    public static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LIMIT = 10;

    // Dependencies (via interfaces)
    private final ClientsRepository clientsRepository;
    private final AccountsRepository accountsRepository;
    private final TransactionsRepository transactionsRepository;
    private final FXConverterService fxConverterService;
    private final TransferService transferService;

    public LedgerController(
            ClientsRepository clientsRepository,
            AccountsRepository accountsRepository,
            TransactionsRepository transactionsRepository,
            FXConverterService fxConverterService,
            TransferService transferService
    ) {
        this.clientsRepository = clientsRepository;
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
        this.fxConverterService = fxConverterService;
        this.transferService = transferService;
    }

    @PostMapping("accounts")
    public ResponseEntity<List<AccountView>> getAccounts(@RequestBody AccountsRequest request) {
        // Validate client id to differentiate between invalid client vs client, but without accounts.
        clientsRepository.findById(request.client_id()).orElseThrow(
                () -> new NotFoundException("client not found")
        );

        // We know the client id is valid here, we are guaranteed to get at least an empty list
        return ResponseEntity.ok(
                accountsRepository
                        .findByClientId(request.client_id())
                        .stream()
                        .map(AccountView::fromModel)
                        .toList()
        );
    }

    @PostMapping("/history")
    public ResponseEntity<HistoryResponse> getHistory(@RequestBody HistoryRequest request) {
        Account account = accountsRepository.findById(request.account_id()).orElseThrow(
                () -> new NotFoundException("account not found")
        );

        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        if (limit < 1) {
            throw new BadRequestException("limit must be positive");
        }

        int offset = request.offset() == null ? DEFAULT_OFFSET : request.offset();
        if (offset < 0) {
            throw new BadRequestException("offset must be 0 or positive");
        }

        // convert offset to page
        // TODO - Note - this will snap to page boundaries. We might have to do something smarter
        //  with pageable or control paging manually. Leaving it as a cleanup exercise for later.
        int page = offset / limit;

        List<Transaction> transactions = transactionsRepository.findAllBySourceAccountIdOrDestinationAccountIdOrderByTimestampDesc(
                account.getId(),
                account.getId(),
                PageRequest.of(page, limit)
        );

        AccountView accountView = AccountView.fromModel(account);
        List<TransactionView> transactionsView = transactions.stream()
                .map(t -> TransactionView.fromModel(account, t))
                .toList();

        return ResponseEntity.ok(new HistoryResponse(
                accountView,
                offset,
                limit,
                transactionsView
        ));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionView> transfer(@RequestBody TransferRequest request) throws TransferService.TransferException, FXConverterService.FXConversionException {
        // validate provided accounts.
        Account sourceAccount = accountsRepository.findById(request.sourceAccountId()).orElseThrow(
                () -> new NotFoundException("source_account_id invalid")
        );

        Account destinationAccount = accountsRepository.findById(request.destinationAccountId()).orElseThrow(
                () -> new NotFoundException("destination_account_id invalid")
        );

        // small sanity check
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BadRequestException("source and destination accounts cannot be the same");
        }

        // validate transfer currency
        if (!destinationAccount.getCurrency().equals(request.currency())) {
            throw new BadRequestException("currency does not match destination account");
        }

        // extract supplied amount string
        BigInteger amount = parseTransferAmount(request.amount());

        if (amount.compareTo(BigInteger.ZERO) < 1) {
            throw new BadRequestException("amount must be positive");
        }

        // And proceed with transfer
        return ResponseEntity.ok(
                TransactionView.fromModel(
                        sourceAccount,
                        transfer(sourceAccount, destinationAccount, amount)
                )
        );
    }

    private Transaction transfer(Account sourceAccount, Account destinationAccount, BigInteger amount) throws TransferService.TransferException, FXConverterService.FXConversionException {
        if (sourceAccount.getCurrency().equals(destinationAccount.getCurrency())) {
            // Simple transfer, we can proceed immediately
            return transferService.transfer(
                    Instant.now(),
                    sourceAccount.getId(),
                    amount,
                    destinationAccount.getId(),
                    amount,
                    String.format( // TODO - add better formatter
                            "Transferred %s %s from %s to %s",
                            amount,
                            destinationAccount.getCurrency(),
                            sourceAccount.getId(),
                            destinationAccount.getId()
                    )
            );
        } else {
            // Let's do the FX conversion outside our transaction below
            // as any external request shouldn't lock up our DB.
            FXConversionResult convertedAmount = fxConverterService.convert(
                    destinationAccount.getCurrency(),
                    sourceAccount.getCurrency(),
                    amount);

            if (convertedAmount.value().compareTo(BigInteger.ZERO) < 1) {
                throw new FXConverterService.FXConversionException("currency conversion error (rounding?)");
            }

            return transferService.transfer(
                    Instant.now(),
                    sourceAccount.getId(),
                    convertedAmount.value(),
                    destinationAccount.getId(),
                    amount,
                    String.format( // TODO - add better formatter, inject actual conversion rate
                            "Transfer from %s (%s %s) to %s (%s %s) complete",
                            sourceAccount.getId(),
                            convertedAmount.value(),
                            sourceAccount.getCurrency(),
                            destinationAccount.getId(),
                            amount,
                            destinationAccount.getCurrency()
                    )
            );
        }
    }

    // TODO - would need to implement better amount parser and
    //  introduce concept of currencies with different denominators (e.g. JPY vs EUR)
    private BigInteger parseTransferAmount(String amount) {
        try {
            return new BigInteger(amount);
        } catch (NumberFormatException e) {
            throw new BadRequestException(String.format("amount '%s' is invalid", amount));
        }
    }
}
