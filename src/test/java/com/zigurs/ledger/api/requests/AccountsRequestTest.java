package com.zigurs.ledger.api.requests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountsRequestTest {

    @Test
    void assertConstructor() {
        assertThrows(
                NullPointerException.class, () ->
                        new AccountsRequest(null)
        );
    }
}