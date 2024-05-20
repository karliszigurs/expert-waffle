package com.zigurs.ledger.api.responses;

import com.zigurs.ledger.model.Account;

import lombok.NonNull;

import java.math.BigInteger;
import java.util.UUID;

public record AccountView(@NonNull UUID account_id,
                          @NonNull String currency,
                          @NonNull BigInteger balance) {

    public static AccountView fromModel(Account account) {
        return new AccountView(account.getId(), account.getCurrency(), account.getBalance());
    }
}
