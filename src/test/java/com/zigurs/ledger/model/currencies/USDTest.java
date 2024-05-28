package com.zigurs.ledger.model.currencies;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class USDTest {

    @Test
    void happyPath() throws ParseException {
        USD usd = new USD();
        BigInteger parsed = usd.parseFromString("2.01");
        assertEquals(BigInteger.valueOf(201), parsed);

        assertEquals("2.01", usd.toFriendlyString(parsed));
    }

    @Test
    void happyPathOneDecimal() throws ParseException {
        USD usd = new USD();
        BigInteger parsed = usd.parseFromString("2.1");
        assertEquals(BigInteger.valueOf(210), parsed);

        assertEquals("2.10", usd.toFriendlyString(parsed));
    }

    @Test
    void happyPathNoDecimals() throws ParseException {
        USD usd = new USD();
        BigInteger parsed = usd.parseFromString("2");
        assertEquals(BigInteger.valueOf(200), parsed);

        assertEquals("2.00", usd.toFriendlyString(parsed));
    }

    @Test
    void happyPathCurrencyCode() {
        USD usd = new USD();
        assertEquals("USD", usd.currencyCode());
    }
}