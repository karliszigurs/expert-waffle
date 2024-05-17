package com.zigurs.mintos.ledger.api;

import lombok.NonNull;

import java.math.BigInteger;

/**
 * FX conversion outcome
 *
 * @param from  original currency
 * @param to    converted currency
 * @param value conversion amount in converted currency
 */
public record FXConversionResult(
        @NonNull String from,
        @NonNull String to,
        @NonNull BigInteger value
) {
}
