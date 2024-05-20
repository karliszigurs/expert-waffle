package com.zigurs.ledger.api;

import com.zigurs.ledger.api.exceptions.BadRequestException;
import com.zigurs.ledger.api.exceptions.NotFoundException;
import com.zigurs.ledger.api.requests.HistoryRequest;
import com.zigurs.ledger.api.responses.AccountView;
import com.zigurs.ledger.api.responses.HistoryResponse;
import com.zigurs.ledger.api.responses.TransactionView;
import com.zigurs.ledger.data.AccountsRepository;
import com.zigurs.ledger.data.TransactionsRepository;
import com.zigurs.ledger.model.Account;
import com.zigurs.ledger.model.Transaction;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * History controller following Spring Boot patterns.
 */
@RestController
public class HistoryController {

    // Default pagination support
    public static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LIMIT = 10;

    private final AccountsRepository accountsRepository;
    private final TransactionsRepository transactionsRepository;

    public HistoryController(
            AccountsRepository accountsRepository,
            TransactionsRepository transactionsRepository
    ) {
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
    }

    @PostMapping("/history")
    public ResponseEntity<HistoryResponse> getHistory(@RequestBody HistoryRequest request) {
        Account account = accountsRepository.findById(request.account_id()).orElseThrow(
                () -> new NotFoundException("account not found")
        );

        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        if (limit < 1) {
            throw new BadRequestException("limit must be positive");
        }

        int offset = request.offset() == null ? DEFAULT_OFFSET : request.offset();
        if (offset < 0) {
            throw new BadRequestException("offset must be 0 or positive");
        }

        // convert offset to page
        // TODO - Note - this will snap to page boundaries. We might have to do something smarter
        //  with pageable or control paging manually. Leaving it as a cleanup exercise for later.
        int page = offset / limit;

        List<Transaction> transactions = transactionsRepository.findAllBySourceAccountIdOrDestinationAccountIdOrderByTimestampDesc(
                account.getId(),
                account.getId(),
                PageRequest.of(page, limit)
        );

        AccountView accountView = AccountView.fromModel(account);
        List<TransactionView> transactionsView = transactions.stream()
                .map(t -> TransactionView.fromModel(account, t))
                .toList();

        return ResponseEntity.ok(new HistoryResponse(
                accountView,
                offset,
                limit,
                transactionsView
        ));
    }
}
