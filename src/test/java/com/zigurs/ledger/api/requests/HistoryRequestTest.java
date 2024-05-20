package com.zigurs.ledger.api.requests;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HistoryRequestTest {

    @Test
    void assertConstructor() {
        assertThrows(NullPointerException.class, () -> {
                    new HistoryRequest(
                            null,
                            null,
                            null
                    );
                }
        );

        assertDoesNotThrow(() -> {
                    new HistoryRequest(
                            UUID.randomUUID(),
                            null,
                            null
                    );
                }
        );
    }
}