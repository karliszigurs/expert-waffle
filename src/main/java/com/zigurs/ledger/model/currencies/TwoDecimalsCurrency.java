package com.zigurs.ledger.model.currencies;

import com.zigurs.ledger.model.Currency;
import com.zigurs.ledger.utils.AmountParser;

import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;

public interface TwoDecimalsCurrency extends Currency {

    @Override
    default BigInteger parseFromString(@NonNull String amount) throws ParseException {
        return AmountParser.getInstance().parseDecimalAmount(amount, 2);
    }

    @Override
    default String toFriendlyString(@NonNull BigInteger amount) {
        return new BigDecimal(amount).movePointLeft(2).toPlainString();
    }
}
