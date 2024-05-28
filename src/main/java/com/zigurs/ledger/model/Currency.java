package com.zigurs.ledger.model;

import java.math.BigInteger;
import java.text.ParseException;

public interface Currency {

    BigInteger parseFromString(String amount) throws ParseException;

    String toFriendlyString(BigInteger amount);

    String currencyCode();

}
