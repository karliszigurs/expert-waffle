package com.zigurs.mintos.ledger.data;

import com.zigurs.mintos.ledger.model.Account;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountsRepository extends Repository<Account, UUID> {

    List<Account> findByClientId(UUID client_id);

    Optional<Account> findById(UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(UUID id);

    Account save(Account account);
}
