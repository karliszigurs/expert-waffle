package com.zigurs.ledger.utils;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class AmountParserTest {

    private final AmountParser amountParser = AmountParser.getInstance();

    @Test
    void getInstance() {
        assertNotNull(amountParser);
    }

    @Test
    void parseNoDecimalsFormatHappyPath() throws ParseException {
        assertEquals(BigInteger.valueOf(0), amountParser.parseDecimalAmount("0", 0));
        assertEquals(BigInteger.valueOf(0), amountParser.parseDecimalAmount("0.00", 0));
        assertEquals(BigInteger.valueOf(0), amountParser.parseDecimalAmount("0.01", 0));
        assertEquals(BigInteger.valueOf(2), amountParser.parseDecimalAmount("2.00", 0));
        assertEquals(BigInteger.valueOf(2), amountParser.parseDecimalAmount("2.01", 0)); // rounds to 2.00 if using float
        assertEquals(BigInteger.valueOf(2), amountParser.parseDecimalAmount("2.02", 0));
        assertEquals(BigInteger.valueOf(2), amountParser.parseDecimalAmount("2.1", 0));
        assertEquals(BigInteger.valueOf(21), amountParser.parseDecimalAmount("21", 0));
        assertEquals(BigInteger.valueOf(201), amountParser.parseDecimalAmount("201", 0));
    }

    @Test
    void parseDecimalAmountHappyPath() throws ParseException {
        assertEquals(BigInteger.valueOf(0), amountParser.parseDecimalAmount("0", 2));
        assertEquals(BigInteger.valueOf(0), amountParser.parseDecimalAmount("0.00", 2));
        assertEquals(BigInteger.valueOf(1), amountParser.parseDecimalAmount("0.01", 2));
        assertEquals(BigInteger.valueOf(200), amountParser.parseDecimalAmount("2.00", 2));
        assertEquals(BigInteger.valueOf(201), amountParser.parseDecimalAmount("2.01", 2)); // rounds to 2.00 if using float
        assertEquals(BigInteger.valueOf(202), amountParser.parseDecimalAmount("2.02", 2));
        assertEquals(BigInteger.valueOf(210), amountParser.parseDecimalAmount("2.1", 2));
    }

    @Test
    void parseNoDecimalsFormatWithRounding() throws ParseException {
        assertEquals(BigInteger.valueOf(2), amountParser.parseDecimalAmount("2.00", 0));
        assertEquals(BigInteger.valueOf(2), amountParser.parseDecimalAmount("2.0099999999", 0));
        assertEquals(BigInteger.valueOf(2), amountParser.parseDecimalAmount("2.0100000001", 0));
    }

    @Test
    void parseDecimalAmountWithRounding() throws ParseException {
        assertEquals(BigInteger.valueOf(200), amountParser.parseDecimalAmount("2.00", 2));
        assertEquals(BigInteger.valueOf(200), amountParser.parseDecimalAmount("2.0099999999", 2));
        assertEquals(BigInteger.valueOf(201), amountParser.parseDecimalAmount("2.0100000001", 2));
    }

    @Test
    void parseNoDecimalsFormatErrors() {
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("", 0));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("this is garbage2.0", 0));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("2.garbage", 0));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("2.0garbage", 0));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("0.004garbage", 0));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("4garbage", 0));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("garbage4", 0));
    }

    @Test
    void parseDecimalAmountErrors() {
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("", 2));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("this is garbage2.0", 2));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("2.garbage", 2));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("2.0garbage", 2));
        assertThrows(ParseException.class, () -> amountParser.parseDecimalAmount("0.004garbage", 2));
    }

    @Test
    void parseNegativeDecimalAmount() throws ParseException {
        assertEquals(BigInteger.valueOf(-200), amountParser.parseDecimalAmount("-2.00", 2));
        assertEquals(BigInteger.valueOf(-201), amountParser.parseDecimalAmount("-2.01", 2)); // rounds to 2.00 if using float
        assertEquals(BigInteger.valueOf(-202), amountParser.parseDecimalAmount("-2.02", 2));
        assertEquals(BigInteger.valueOf(-210), amountParser.parseDecimalAmount("-2.1", 2));
    }
}