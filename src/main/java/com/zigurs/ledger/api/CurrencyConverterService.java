package com.zigurs.ledger.api;

import lombok.NonNull;

import java.math.BigInteger;

// TODO - generalize amount class so it is suitable for use here
public interface CurrencyConverterService {

    /**
     * Should convert the amount specified in fromCurrency to targetCurrency.
     *
     * @param fromCurrency currency to convert from
     * @param toCurrency   currency to convert to
     * @param amount       amount to convert
     * @return converted amount
     * @throws CurrencyConversionException in case of a failure. Note that runtime exceptions (e.g. NPE) will be thrown as-is.
     */
    CurrencyConversionResult convert(String fromCurrency, String toCurrency, BigInteger amount) throws CurrencyConversionException;

    class CurrencyConversionException extends RuntimeException {
        public CurrencyConversionException(String message) {
            super(message);
        }
    }

    /**
     * FX conversion outcome
     *
     * @param from  original currency
     * @param to    converted currency
     * @param value conversion amount in converted currency
     */
    record CurrencyConversionResult(@NonNull String from, @NonNull String to, @NonNull BigInteger value) {
    }
}
