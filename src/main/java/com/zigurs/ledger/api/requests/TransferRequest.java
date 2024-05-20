package com.zigurs.ledger.api.requests;

import lombok.NonNull;

import java.util.UUID;

public record TransferRequest(@NonNull UUID sourceAccountId,
                              @NonNull UUID destinationAccountId,
                              @NonNull String currency,
                              @NonNull String amount) {
}
