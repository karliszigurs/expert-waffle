package com.zigurs.ledger.api;

import com.zigurs.ledger.api.exceptions.BadRequestException;
import com.zigurs.ledger.api.exceptions.NotFoundException;
import com.zigurs.ledger.api.requests.TransferRequest;
import com.zigurs.ledger.api.responses.TransactionView;
import com.zigurs.ledger.data.AccountsRepository;
import com.zigurs.ledger.model.Account;
import com.zigurs.ledger.model.Transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Transfer controller following Spring Boot patterns.
 */
@RestController
public class TransferController {

    private final AccountsRepository accountsRepository;
    private final CurrencyConverterService currencyConverterService;
    private final TransferService transferService;

    public TransferController(
            AccountsRepository accountsRepository,
            CurrencyConverterService currencyConverterService,
            TransferService transferService
    ) {
        this.accountsRepository = accountsRepository;
        this.currencyConverterService = currencyConverterService;
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionView> transfer(@RequestBody TransferRequest request) throws TransferService.TransferException, CurrencyConverterService.CurrencyConversionException {
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

    private Transaction transfer(Account sourceAccount, Account destinationAccount, BigInteger amount) throws TransferService.TransferException, CurrencyConverterService.CurrencyConversionException {
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
            CurrencyConverterService.CurrencyConversionResult convertedAmount = currencyConverterService.convert(
                    destinationAccount.getCurrency(),
                    sourceAccount.getCurrency(),
                    amount);

            if (convertedAmount.value().compareTo(BigInteger.ZERO) < 1) {
                throw new CurrencyConverterService.CurrencyConversionException("currency conversion error (rounding?)");
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
