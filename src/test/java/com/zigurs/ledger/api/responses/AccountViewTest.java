package com.zigurs.ledger.api.responses;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountViewTest {

    @Test
    void assertConstructor() {
        assertThrows(NullPointerException.class, () -> {
                    new AccountView(
                            null,
                            "XYZ",
                            BigInteger.valueOf(1L)
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new AccountView(
                            UUID.randomUUID(),
                            null,
                            BigInteger.valueOf(1L)
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new AccountView(
                            UUID.randomUUID(),
                            "XYZ",
                            null
                    );
                }
        );
    }
}