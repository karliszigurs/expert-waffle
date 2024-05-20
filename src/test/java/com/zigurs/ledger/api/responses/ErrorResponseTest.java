package com.zigurs.ledger.api.responses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrorResponseTest {

    @Test
    void assertConstructor() {
        assertThrows(NullPointerException.class, () -> {
                    new ErrorResponse(null);
                }
        );
    }
}