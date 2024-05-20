package com.zigurs.ledger.api.responses;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionViewTest {

    @Test
    void assertConstructor() {
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();
        TransactionDirection direction = TransactionDirection.DEBIT;
        String status = UUID.randomUUID().toString();
        UUID source = UUID.randomUUID();
        UUID destination = UUID.randomUUID();
        BigInteger amount = BigInteger.valueOf(1L);
        BigInteger balance = BigInteger.valueOf(2L);
        String description = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> {
                    new TransactionView(
                            id,
                            timestamp,
                            direction,
                            status,
                            source,
                            destination,
                            amount,
                            balance,
                            description
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransactionView(
                            null,
                            timestamp,
                            direction,
                            status,
                            source,
                            destination,
                            amount,
                            balance,
                            description
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransactionView(
                            id,
                            null,
                            direction,
                            status,
                            source,
                            destination,
                            amount,
                            balance,
                            description
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransactionView(
                            id,
                            timestamp,
                            null,
                            status,
                            source,
                            destination,
                            amount,
                            balance,
                            description
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransactionView(
                            id,
                            timestamp,
                            direction,
                            null,
                            source,
                            destination,
                            amount,
                            balance,
                            description
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransactionView(
                            id,
                            timestamp,
                            direction,
                            status,
                            null,
                            destination,
                            amount,
                            balance,
                            description
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransactionView(
                            id,
                            timestamp,
                            direction,
                            status,
                            source,
                            null,
                            amount,
                            balance,
                            description
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransactionView(
                            id,
                            timestamp,
                            direction,
                            status,
                            source,
                            destination,
                            null,
                            balance,
                            description
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransactionView(
                            id,
                            timestamp,
                            direction,
                            status,
                            source,
                            destination,
                            amount,
                            null,
                            description
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransactionView(
                            id,
                            timestamp,
                            direction,
                            status,
                            source,
                            destination,
                            amount,
                            balance,
                            null
                    );
                }
        );
    }
}