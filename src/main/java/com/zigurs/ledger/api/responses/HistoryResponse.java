package com.zigurs.ledger.api.responses;

import lombok.NonNull;

import java.util.List;

public record HistoryResponse(@NonNull AccountView account,
                              int offset,
                              int limit,
                              @NonNull List<TransactionView> transactions) {
}
