package com.zigurs.ledger.infra.fxrates.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyBeaconResponseTest {

    @Test
    void nullChecks() {
        assertThrows(NullPointerException.class, () -> {
                    new CurrencyBeaconResponse(
                            "from",
                            "to",
                            BigDecimal.valueOf(1L),
                            null
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new CurrencyBeaconResponse(
                            "from",
                            "to",
                            null,
                            BigDecimal.valueOf(2L)
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new CurrencyBeaconResponse(
                            "from",
                            null,
                            BigDecimal.valueOf(1L),
                            BigDecimal.valueOf(2L)
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new CurrencyBeaconResponse(
                            null,
                            "to",
                            BigDecimal.valueOf(1L),
                            BigDecimal.valueOf(2L)
                    );
                }
        );
    }
}