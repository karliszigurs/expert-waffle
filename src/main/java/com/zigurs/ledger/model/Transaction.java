package com.zigurs.ledger.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @UuidGenerator
    private UUID id;
    private TransactionStatus status;
    private Instant timestamp;
    @ManyToOne
    private Account sourceAccount;
    private BigInteger sourceAmount;
    private BigInteger sourceBalance;
    @ManyToOne
    private Account destinationAccount;
    private BigInteger destinationAmount;
    private BigInteger destinationBalance;
    private String description;

    public Transaction() {

    }

    public Transaction(UUID id, TransactionStatus status, Instant timestamp, Account sourceAccount, BigInteger sourceAmount, BigInteger sourceBalance, Account destinationAccount, BigInteger destinationAmount, BigInteger destinationBalance, String description) {
        this.id = id;
        this.status = status;
        this.timestamp = timestamp;
        this.sourceAccount = sourceAccount;
        this.sourceAmount = sourceAmount;
        this.sourceBalance = sourceBalance;
        this.destinationAccount = destinationAccount;
        this.destinationAmount = destinationAmount;
        this.destinationBalance = destinationBalance;
        this.description = description;
    }

}
