package com.zigurs.ledger.api;

import com.zigurs.ledger.api.exceptions.BadRequestException;
import com.zigurs.ledger.api.exceptions.NotFoundException;
import com.zigurs.ledger.api.requests.TransferRequest;
import com.zigurs.ledger.api.responses.TransactionView;
import com.zigurs.ledger.data.AccountsRepository;
import com.zigurs.ledger.model.Account;
import com.zigurs.ledger.model.Currency;
import com.zigurs.ledger.model.Transaction;
import com.zigurs.ledger.model.currencies.EUR;
import com.zigurs.ledger.model.currencies.JPY;
import com.zigurs.ledger.model.currencies.TwoDecimalsCurrency;
import com.zigurs.ledger.model.currencies.USD;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.text.ParseException;
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

        Currency currency = lookupCurrency(request.currency());

        // extract supplied amount string
        BigInteger amount = parseTransferAmount(currency, request.amount());

        if (amount.compareTo(BigInteger.ZERO) < 1) {
            throw new BadRequestException("amount must be positive");
        }

        // And proceed with transfer
        return ResponseEntity.ok(
                TransactionView.fromModel(
                        sourceAccount,
                        transfer(sourceAccount, destinationAccount, currency, amount)
                )
        );
    }

    // TODO - for now we always find at least 2-decimals currency.
    private Currency lookupCurrency(String currency) {
        return switch (currency) {
            case "USD" -> new USD();
            case "EUR" -> new EUR();
            case "JPY" -> new JPY();
            default -> (TwoDecimalsCurrency) () -> currency;
        };
    }

    private Transaction transfer(Account sourceAccount, Account destinationAccount, Currency currency, BigInteger amount) throws TransferService.TransferException, CurrencyConverterService.CurrencyConversionException {
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
                            currency.toFriendlyString(amount),
                            currency.currencyCode(),
                            sourceAccount.getId(),
                            destinationAccount.getId()
                    )
            );
        } else {
            // Let's do the FX conversion outside our transaction below
            // as any external request shouldn't lock up our DB.
            CurrencyConverterService.CurrencyConversionResult convertedAmount = currencyConverterService.convert(
                    currency.currencyCode(),
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
                            convertedAmount.value(), // TODO - need to handle fractions here
                            sourceAccount.getCurrency(),
                            destinationAccount.getId(),
                            currency.toFriendlyString(amount),
                            destinationAccount.getCurrency()
                    )
            );
        }
    }

    private BigInteger parseTransferAmount(Currency currency, String amount) {
        try {
            return currency.parseFromString(amount);
        } catch (ParseException e) {
            throw new BadRequestException(String.format("amount '%s' is invalid", amount));
        }
    }
}
