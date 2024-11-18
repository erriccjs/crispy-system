package com.myproject.crispysystem.accounts.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "owed_balances")
public class Owed {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "borrower_account_id", nullable = false)
    private Account borrowerAccount;

    @ManyToOne
    @JoinColumn(name = "lender_account_id", nullable = false)
    private Account lenderAccount;

    @Column(nullable = false)
    private BigDecimal owedAmount;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Owed(){}

    // Constructor with parameters
    public Owed(Account borrowerAccount, Account lenderAccount, BigDecimal owedAmount) {
        this.borrowerAccount = borrowerAccount;
        this.lenderAccount = lenderAccount;
        this.owedAmount = owedAmount;
    }
    // Getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Account getBorrowerAccount() {
        return borrowerAccount;
    }

    public void setBorrowerAccount(Account borrowerAccount) {
        this.borrowerAccount = borrowerAccount;
    }

    public Account getLenderAccount() {
        return lenderAccount;
    }

    public void setLenderAccount(Account lenderAccount) {
        this.lenderAccount = lenderAccount;
    }

    public BigDecimal getOwedAmount() {
        return owedAmount;
    }

    public void setOwedAmount(BigDecimal owedAmount) {
        this.owedAmount = owedAmount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

