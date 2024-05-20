package com.zigurs.ledger.api;

import com.zigurs.ledger.api.exceptions.NotFoundException;
import com.zigurs.ledger.api.requests.AccountsRequest;
import com.zigurs.ledger.api.responses.AccountView;
import com.zigurs.ledger.data.AccountsRepository;
import com.zigurs.ledger.data.ClientsRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Accounts controller following Spring Boot patterns.
 */
@RestController
public class AccountsController {

    private final ClientsRepository clientsRepository;
    private final AccountsRepository accountsRepository;

    public AccountsController(
            ClientsRepository clientsRepository,
            AccountsRepository accountsRepository
    ) {
        this.clientsRepository = clientsRepository;
        this.accountsRepository = accountsRepository;
    }

    @PostMapping("accounts")
    public ResponseEntity<List<AccountView>> getAccounts(@RequestBody AccountsRequest request) {
        // Validate client id to differentiate between invalid client vs client, but without accounts.
        clientsRepository.findById(request.client_id()).orElseThrow(
                () -> new NotFoundException("client not found")
        );

        // We know the client id is valid here, we are guaranteed to get at least an empty list
        return ResponseEntity.ok(
                accountsRepository
                        .findByClientId(request.client_id())
                        .stream()
                        .map(AccountView::fromModel)
                        .toList()
        );
    }
}
