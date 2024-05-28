package com.zigurs.ledger.utils;

import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;

public class AmountParser {

    private static final AmountParser amountParser = new AmountParser();
    private final DecimalFormat decimalFormat;

    private AmountParser() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        String pattern = "#,##0.0#";

        decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);
    }

    public static AmountParser getInstance() {
        return amountParser;
    }

    /**
     * Parse supplied string from decimal format (',' thousands separator, '.' decimal separator) as fractions determined by supplied decimal digits param.
     * <p>
     * Examples:
     * * ("2.01", 2) => 201
     * * ("2.01", 0) => 200
     * * ("2", 3) => 2000
     * <p>
     * Note that DecimalFormat/NumberFormat is not thread-safe, so we sync this access
     *
     * @param amount string to parse
     * @return parsed value as specified fractions / decimal digits
     * @throws ParseException thrown if string cannot be parsed or contains unexpected surplus information
     */
    public synchronized BigInteger parseDecimalAmount(@NonNull String amount, int decimalDigits) throws ParseException {
        String toParse = amount.trim();
        ParsePosition pos = new ParsePosition(0);
        BigDecimal parsedStringValue = (BigDecimal) decimalFormat.parse(toParse, pos);

        if (pos.getIndex() != toParse.length() || pos.getErrorIndex() != -1) {
            throw new ParseException(toParse, pos.getErrorIndex());
        }

        return parsedStringValue.movePointRight(decimalDigits).toBigInteger(); // no rounding, explicitly discard any trailing digits
    }
}
