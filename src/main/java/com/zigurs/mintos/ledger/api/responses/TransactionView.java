package com.zigurs.mintos.ledger.api.responses;

import com.zigurs.mintos.ledger.model.Account;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record TransactionView(
        UUID transaction_id,
        Instant timestamp,
        TransactionDirection type,
        String status,
        UUID source_account_id,
        UUID destination_account_id,
        BigInteger amount,
        BigInteger account_balance,
        String description
) {

    public static TransactionView fromModel(
            Account requestingAccount,
            com.zigurs.mintos.ledger.model.Transaction transaction
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
