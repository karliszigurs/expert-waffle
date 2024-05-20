package com.zigurs.ledger.infra.fxrates.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NonNull;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CurrencyBeaconResponse(@NonNull String from,
                                     @NonNull String to,
                                     @NonNull BigDecimal amount,
                                     @NonNull BigDecimal value) {
}
