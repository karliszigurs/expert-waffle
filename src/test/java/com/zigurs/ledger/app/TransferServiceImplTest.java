package com.zigurs.ledger.app;

import com.zigurs.ledger.api.TransferService;
import com.zigurs.ledger.data.AccountsRepository;
import com.zigurs.ledger.data.TransactionsRepository;
import com.zigurs.ledger.model.Account;
import com.zigurs.ledger.model.Transaction;
import com.zigurs.ledger.model.TransactionStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceImplTest {

    private TransferServiceImpl transferService;
    private AccountsRepository accountsRepository;
    private TransactionsRepository transactionsRepository;

    private static final UUID exampleUUID = UUID.fromString("d4a8ad83-93b5-4114-9df5-61127a6c61de");
    private static final UUID exampleUUID2 = UUID.fromString("413a0b12-985d-4437-9490-bdeddb617e9e");

    private static final BigInteger exampleAmount = BigInteger.valueOf(1L);

    @BeforeEach
    void setUp() {
        accountsRepository = mock(AccountsRepository.class);
        transactionsRepository = mock(TransactionsRepository.class);

        transferService = new TransferServiceImpl(
                accountsRepository,
                transactionsRepository
        );
    }

    @Test
    void greenPath() throws TransferService.TransferException {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(exampleUUID);
        when(account.getBalance()).thenReturn(BigInteger.valueOf(1L));

        Account destinationAccount = mock(Account.class);
        when(destinationAccount.getId()).thenReturn(exampleUUID2);
        when(destinationAccount.getBalance()).thenReturn(BigInteger.valueOf(1L));

        when(accountsRepository.findByIdForUpdate(exampleUUID)).thenReturn(Optional.of(account));
        when(accountsRepository.findByIdForUpdate(exampleUUID2)).thenReturn(Optional.of(destinationAccount));
        when(accountsRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(accountsRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionsRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction tx = transferService.transfer(
                Instant.now(),
                exampleUUID,
                exampleAmount,
                exampleUUID2,
                exampleAmount,
                "happy transfer"
        );

        assertNotNull(tx, "Expected transaction, but none returned");
        assertEquals(exampleUUID, tx.getSourceAccount().getId(), "incorrect source account");
        assertEquals(exampleUUID2, tx.getDestinationAccount().getId(), "incorrect destination account");
        assertEquals(TransactionStatus.COMPLETED, tx.getStatus(), "incorrect status");
        assertEquals(1L, tx.getDestinationAmount().longValue(), "incorrect transaction amount");
        assertEquals("happy transfer", tx.getDescription(), "incorrect description");

        verify(accountsRepository, times(1)).findByIdForUpdate(exampleUUID);
        verify(accountsRepository, times(1)).findByIdForUpdate(exampleUUID2);
        verify(account, times(1)).setBalance(BigInteger.valueOf(0L));
        verify(destinationAccount, times(1)).setBalance(BigInteger.valueOf(2L));
        verify(accountsRepository, times(1)).save(account);
        verify(accountsRepository, times(1)).save(destinationAccount);
    }

    @Test
    void invalidAccounts() throws TransferService.TransferException {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(exampleUUID);
        when(account.getBalance()).thenReturn(BigInteger.valueOf(1L));

        when(accountsRepository.findByIdForUpdate(exampleUUID)).thenReturn(Optional.of(account));

        assertThrows(TransferService.TransferException.class, () -> {
                    Transaction tx = transferService.transfer(
                            Instant.now(),
                            exampleUUID,
                            exampleAmount,
                            exampleUUID,
                            exampleAmount,
                            "happy transfer"
                    );
                }
        );

        verify(accountsRepository, times(0)).findByIdForUpdate(any(UUID.class));
    }

    @Test
    void insufficientBalance() throws TransferService.TransferException {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(exampleUUID);
        when(account.getBalance()).thenReturn(BigInteger.valueOf(0L));

        Account destinationAccount = mock(Account.class);
        when(destinationAccount.getId()).thenReturn(exampleUUID2);
        when(destinationAccount.getBalance()).thenReturn(BigInteger.valueOf(1L));

        when(accountsRepository.findByIdForUpdate(exampleUUID)).thenReturn(Optional.of(account));
        when(accountsRepository.findByIdForUpdate(exampleUUID2)).thenReturn(Optional.of(destinationAccount));
        when(accountsRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(accountsRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionsRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction tx = transferService.transfer(
                Instant.now(),
                exampleUUID,
                exampleAmount,
                exampleUUID2,
                exampleAmount,
                "insufficient balance"
        );

        assertNotNull(tx, "Expected transaction, but none returned");
        assertEquals(exampleUUID, tx.getSourceAccount().getId(), "incorrect source account");
        assertEquals(exampleUUID2, tx.getDestinationAccount().getId(), "incorrect destination account");
        assertEquals(TransactionStatus.FAILED, tx.getStatus(), "incorrect status");
        assertEquals("insufficient balance", tx.getDescription(), "incorrect description");

        verify(accountsRepository, times(1)).findByIdForUpdate(exampleUUID);
        verify(accountsRepository, times(1)).findByIdForUpdate(exampleUUID2);
        verify(account, times(0)).setBalance(any(BigInteger.class));
        verify(destinationAccount, times(0)).setBalance(any(BigInteger.class));
        verify(accountsRepository, times(0)).save(any(Account.class));
    }


    @Test
    void invalidSourceAccount() throws TransferService.TransferException {
        when(accountsRepository.findByIdForUpdate(exampleUUID)).thenReturn(Optional.empty());

        assertThrows(TransferService.TransferException.class,
                () -> {
                    transferService.transfer(
                            Instant.now(),
                            exampleUUID,
                            exampleAmount,
                            exampleUUID2,
                            exampleAmount,
                            ""
                    );
                }
        );

        verify(accountsRepository, times(1)).findByIdForUpdate(exampleUUID);
    }

    @Test
    void invalidDestinationAccount() throws TransferService.TransferException {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(exampleUUID);

        when(accountsRepository.findByIdForUpdate(exampleUUID)).thenReturn(Optional.of(account));
        when(accountsRepository.findByIdForUpdate(exampleUUID2)).thenReturn(Optional.empty());

        assertThrows(TransferService.TransferException.class,
                () -> {
                    transferService.transfer(
                            Instant.now(),
                            exampleUUID,
                            exampleAmount,
                            exampleUUID2,
                            exampleAmount,
                            ""
                    );
                }
        );

        verify(accountsRepository, times(1)).findByIdForUpdate(exampleUUID);
        verify(accountsRepository, times(1)).findByIdForUpdate(exampleUUID2);
    }

    @Test
    void reversedLockingAttempt() throws TransferService.TransferException {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(exampleUUID);
        when(account.getBalance()).thenReturn(exampleAmount);

        Account destinationAccount = mock(Account.class);
        when(destinationAccount.getId()).thenReturn(exampleUUID2);
        when(destinationAccount.getBalance()).thenReturn(exampleAmount);

        when(accountsRepository.findByIdForUpdate(exampleUUID)).thenReturn(Optional.of(account));
        when(accountsRepository.findByIdForUpdate(exampleUUID2)).thenReturn(Optional.empty());

        assertThrows(TransferService.TransferException.class, () -> {
                    transferService.transfer(
                            Instant.now(),
                            exampleUUID2,
                            exampleAmount,
                            exampleUUID,
                            exampleAmount,
                            ""
                    );
                }
        );

        verify(accountsRepository, times(1)).findByIdForUpdate(exampleUUID2);
        verify(accountsRepository, times(1)).findByIdForUpdate(exampleUUID);
    }

    @Test
    void constructorNullChecks() throws TransferService.TransferException {
        assertThrows(NullPointerException.class, () -> {
                    new TransferServiceImpl(
                            accountsRepository,
                            null
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransferServiceImpl(
                            null,
                            transactionsRepository
                    );
                }
        );
    }

    @Test
    void transferNullChecks() throws TransferService.TransferException {
        assertThrows(NullPointerException.class, () -> {
                    transferService.transfer(
                            null,
                            exampleUUID,
                            exampleAmount,
                            exampleUUID2,
                            exampleAmount,
                            "happy transfer"
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    transferService.transfer(
                            Instant.now(),
                            null,
                            exampleAmount,
                            exampleUUID2,
                            exampleAmount,
                            "happy transfer"
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    transferService.transfer(
                            Instant.now(),
                            exampleUUID,
                            null,
                            exampleUUID2,
                            exampleAmount,
                            "happy transfer"
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    transferService.transfer(
                            Instant.now(),
                            exampleUUID,
                            exampleAmount,
                            null,
                            exampleAmount,
                            "happy transfer"
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    transferService.transfer(
                            Instant.now(),
                            exampleUUID,
                            exampleAmount,
                            exampleUUID2,
                            null,
                            "happy transfer"
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    transferService.transfer(
                            Instant.now(),
                            exampleUUID,
                            exampleAmount,
                            exampleUUID2,
                            exampleAmount,
                            null
                    );
                }
        );
    }
}
