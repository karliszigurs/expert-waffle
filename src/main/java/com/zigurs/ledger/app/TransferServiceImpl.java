package com.zigurs.ledger.app;

import com.zigurs.ledger.data.AccountsRepository;
import com.zigurs.ledger.api.TransferService;
import com.zigurs.ledger.data.TransactionsRepository;
import com.zigurs.ledger.model.Account;
import com.zigurs.ledger.model.Transaction;
import com.zigurs.ledger.model.TransactionStatus;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {

    private final AccountsRepository accountsRepository;
    private final TransactionsRepository transactionsRepository;

    public TransferServiceImpl(
            @NonNull AccountsRepository accountsRepository,
            @NonNull TransactionsRepository transactionsRepository
    ) {
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction transfer(
            @NonNull Instant timestamp,
            @NonNull UUID sourceAccountId,
            @NonNull BigInteger sourceAmount,
            @NonNull UUID destinationAccountId,
            @NonNull BigInteger destinationAmount,
            @NonNull String description
    ) throws TransferException {
        // Magic here. There is no eloquent way of reliably locking accounts of possibly different clients,
        // and serializable is not really reliable across different DB engines.
        // Instead we will lock accounts FOR UPDATE in alphabetical sequence - this guarantees that we are in control
        // and avoid possible deadlocking if attempting to lock A>B and B>A at the same time in different transactions.
        //
        // Note that we still risk circular deadlocks (A>B>C, B>C>A, C>A>B, etc), however chances of this are
        // significantly lower. Solving this would require implementing a reliable jobs/queue mechanic
        // that decouples debit transactions from credit ones completely.
        Account sourceAccount = null, destinationAccount = null;

        // lock "smallest" account ID first
        if (sourceAccountId.compareTo(destinationAccountId) > 0) {
            destinationAccount = accountsRepository.findByIdForUpdate(destinationAccountId).orElseThrow(TransferException::new);
            sourceAccount = accountsRepository.findByIdForUpdate(sourceAccountId).orElseThrow(TransferException::new);
        } else if (sourceAccountId.compareTo(destinationAccountId) < 0) {
            sourceAccount = accountsRepository.findByIdForUpdate(sourceAccountId).orElseThrow(TransferException::new);
            destinationAccount = accountsRepository.findByIdForUpdate(destinationAccountId).orElseThrow(TransferException::new);
        } else {
            throw new TransferException();
        }

        // should be safe to proceed
        if (sourceAccount.getBalance().subtract(sourceAmount).compareTo(BigInteger.ZERO) < 0) {
            // Insufficient balance
            return transactionsRepository.save(
                    new Transaction(null,
                            TransactionStatus.FAILED,
                            timestamp,
                            sourceAccount,
                            sourceAmount,
                            sourceAccount.getBalance(),
                            destinationAccount,
                            destinationAmount,
                            destinationAccount.getBalance(),
                            "insufficient balance")
            );
        } else {
            // enough funds to proceed
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(sourceAmount));
            sourceAccount = accountsRepository.save(sourceAccount);

            destinationAccount.setBalance(destinationAccount.getBalance().add(destinationAmount));
            destinationAccount = accountsRepository.save(destinationAccount);

            return transactionsRepository.save(
                    new Transaction(null,
                            TransactionStatus.COMPLETED,
                            timestamp,
                            sourceAccount,
                            sourceAmount,
                            sourceAccount.getBalance(),
                            destinationAccount,
                            destinationAmount,
                            destinationAccount.getBalance(),
                            description)
            );
        }
    }
}
