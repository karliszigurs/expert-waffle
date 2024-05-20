package com.zigurs.ledger.api.requests;

import lombok.NonNull;

import java.util.UUID;

public record AccountsRequest(@NonNull UUID client_id) {
}
