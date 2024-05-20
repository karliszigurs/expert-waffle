package com.zigurs.ledger.api.responses;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class HistoryResponseTest {

    @Test
    void assertConstructor() {
        assertThrows(NullPointerException.class, () -> {
                    new HistoryResponse(
                            null,
                            0,
                            0,
                            Collections.emptyList()
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new HistoryResponse(
                            new AccountView(UUID.randomUUID(), "XYZ", BigInteger.valueOf(1L)),
                            0,
                            0,
                            null
                    );
                }
        );
    }
}