package com.zigurs.mintos.ledger.api.responses;

import lombok.NonNull;

import java.math.BigInteger;
import java.util.UUID;

public record AccountView(@NonNull UUID account_id,
                          @NonNull String currency,
                          @NonNull BigInteger balance) {

    public static AccountView fromModel(com.zigurs.mintos.ledger.model.Account account) {
        return new AccountView(account.getId(), account.getCurrency(), account.getBalance());
    }

}
