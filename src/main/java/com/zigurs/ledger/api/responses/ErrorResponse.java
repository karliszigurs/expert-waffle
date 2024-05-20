package com.zigurs.ledger.api.responses;

import lombok.NonNull;

public record ErrorResponse(@NonNull String error) {
}
