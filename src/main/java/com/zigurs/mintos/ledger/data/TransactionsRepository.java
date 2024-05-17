package com.zigurs.mintos.ledger.data;

import com.zigurs.mintos.ledger.model.Transaction;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface TransactionsRepository extends Repository<Transaction, UUID> {

    Transaction save(Transaction transaction);

    List<Transaction> findAllBySourceAccountIdOrDestinationAccountIdOrderByTimestampDesc(UUID sourceAccountId, UUID destinationAccountId, Pageable pageable);
}
