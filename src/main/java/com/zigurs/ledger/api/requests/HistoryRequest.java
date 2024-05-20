package com.zigurs.ledger.api.requests;

import lombok.NonNull;

import java.util.UUID;

public record HistoryRequest(@NonNull UUID account_id,
                             Integer offset,
                             Integer limit) {
}
