package com.zigurs.ledger.model.currencies;

import com.zigurs.ledger.model.Currency;
import com.zigurs.ledger.utils.AmountParser;

import lombok.NonNull;

import java.math.BigInteger;
import java.text.ParseException;

public interface NoDecimalsCurrency extends Currency {

    @Override
    default BigInteger parseFromString(@NonNull String amount) throws ParseException {
        return AmountParser.getInstance().parseDecimalAmount(amount, 0);
    }

    @Override
    default String toFriendlyString(@NonNull BigInteger amount) {
        return amount.toString();
    }
}
