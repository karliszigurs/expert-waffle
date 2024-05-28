package com.zigurs.ledger.model.currencies;

public class EUR implements TwoDecimalsCurrency {

    @Override
    public String currencyCode() {
        return "EUR";
    }
}
