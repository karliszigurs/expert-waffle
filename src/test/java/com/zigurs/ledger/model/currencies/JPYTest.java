package com.zigurs.ledger.model.currencies;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JPYTest {

    @Test
    void happyPath() throws ParseException {
        JPY jpy = new JPY();
        BigInteger parsed = jpy.parseFromString("2.01");
        assertEquals(BigInteger.valueOf(2), parsed);

        assertEquals("2", jpy.toFriendlyString(parsed));
    }

    @Test
    void happyPathOneDecimal() throws ParseException {
        JPY jpy = new JPY();
        BigInteger parsed = jpy.parseFromString("2.1");
        assertEquals(BigInteger.valueOf(2), parsed);

        assertEquals("2", jpy.toFriendlyString(parsed));
    }

    @Test
    void happyPathNoDecimals() throws ParseException {
        JPY jpy = new JPY();
        BigInteger parsed = jpy.parseFromString("2");
        assertEquals(BigInteger.valueOf(2), parsed);

        assertEquals("2", jpy.toFriendlyString(parsed));
    }

    @Test
    void happyPathCode() {
        JPY jpy = new JPY();
        assertEquals("JPY", jpy.currencyCode());
    }
}