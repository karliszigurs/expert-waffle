package com.zigurs.ledger.api;

import com.zigurs.ledger.model.Transaction;

import lombok.NonNull;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public interface TransferService {

    /**
     * Implementation should create and store transfer with the specified parameters.
     * Once transaction is safely stored it should be returned so that it can be returned to client.
     *
     * @param timestamp            transaction timestamp
     * @param sourceAccountId      source account id
     * @param sourceAmount         amount to deduct from source account
     * @param destinationAccountId destination account
     * @param destinationAmount    amount to credit in destination amount
     * @param description          transaction description (shown to user)
     * @return created transaction
     */
    Transaction transfer(
            @NonNull Instant timestamp,
            @NonNull UUID sourceAccountId,
            @NonNull BigInteger sourceAmount,
            @NonNull UUID destinationAccountId,
            @NonNull BigInteger destinationAmount,
            @NonNull String description
    ) throws TransferException;

    class TransferException extends RuntimeException {
        public TransferException() {
            super("internal error");
        }
    }
}
