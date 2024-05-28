package com.zigurs.ledger.model.currencies;

public class USD implements TwoDecimalsCurrency {

    @Override
    public String currencyCode() {
        return "USD";
    }
}
