package com.myproject.crispysystem.accounts.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "transactionid", updatable = false, nullable = false)
    private UUID transactionID;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after_transaction", nullable = false)
    private BigDecimal balanceAfterTransaction;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 255)
    private String description;

    @Column(name = "related_accountid")
    private UUID relatedAccountID;

    // Default constructor for JPA
    public Transaction() {
    }

    // Parameterized constructor for convenience
    public Transaction(Account account, TransactionType transactionType, BigDecimal amount,
                       BigDecimal balanceAfterTransaction, LocalDateTime timestamp, String description, UUID relatedAccountID) {
        this.account = account;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.description = description;
        this.relatedAccountID = relatedAccountID;
    }

    // Getters and Setters
    public UUID getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(UUID transactionID) {
        this.transactionID = transactionID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getRelatedAccountID() {
        return relatedAccountID;
    }

    public void setRelatedAccountID(UUID relatedAccountID) {
        this.relatedAccountID = relatedAccountID;
    }
}
