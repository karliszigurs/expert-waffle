package com.zigurs.ledger.api.responses;

import com.zigurs.ledger.model.Account;
import com.zigurs.ledger.model.Transaction;

import lombok.NonNull;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record TransactionView(@NonNull UUID transaction_id,
                              @NonNull Instant timestamp,
                              @NonNull TransactionDirection type,
                              @NonNull String status,
                              @NonNull UUID source_account_id,
                              @NonNull UUID destination_account_id,
                              @NonNull BigInteger amount,
                              @NonNull BigInteger account_balance,
                              @NonNull String description) {

    public static TransactionView fromModel(
            Account requestingAccount,
            Transaction transaction
    ) {
        BigInteger amount, balance;
        TransactionDirection direction;

        // Depending on who requests the transactions history format it in accordance
        // that would make sense from a users perspective (direction, balance impacting their account,
        // running balance of their account).
        if (transaction.getSourceAccount().getId().equals(requestingAccount.getId())) {
            direction = TransactionDirection.DEBIT;
            amount = transaction.getSourceAmount();
            balance = transaction.getSourceBalance();
        } else {
            direction = TransactionDirection.CREDIT;
            amount = transaction.getDestinationAmount();
            balance = transaction.getDestinationBalance();
        }

        return new TransactionView(
                transaction.getId(),
                transaction.getTimestamp(),
                direction,
                transaction.getStatus().toString(),
                transaction.getSourceAccount().getId(),
                transaction.getDestinationAccount().getId(),
                amount,
                balance,
                transaction.getDescription()
        );
    }
}
