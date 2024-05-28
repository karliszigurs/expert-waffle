package com.zigurs.ledger.model.currencies;

public class JPY implements NoDecimalsCurrency {

    @Override
    public String currencyCode() {
        return "JPY";
    }
}
