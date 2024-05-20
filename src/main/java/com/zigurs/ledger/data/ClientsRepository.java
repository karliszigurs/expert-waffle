package com.zigurs.ledger.data;

import com.zigurs.ledger.model.Client;

import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ClientsRepository extends Repository<Client, UUID> {

    Optional<Client> findById(UUID id);
}
