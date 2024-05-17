package com.zigurs.mintos.ledger.api;

import java.math.BigInteger;

public interface FXConverterService {

    class FXConversionException extends RuntimeException {
        public FXConversionException(String message) {
            super(message);
        }
    }

    /**
     * Should convert the amount specified in fromCurrency to targetCurrency.
     *
     * @param fromCurrency currency to convert from
     * @param toCurrency   currency to convert to
     * @param amount       amount to convert
     * @return converted amount
     * @throws FXConversionException in case of a failure. Note that runtime exceptions (e.g. NPE) will be thrown as-is.
     */
    FXConversionResult convert(String fromCurrency, String toCurrency, BigInteger amount) throws FXConversionException;

}
