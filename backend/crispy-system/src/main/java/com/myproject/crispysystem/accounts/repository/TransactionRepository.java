package com.myproject.crispysystem.accounts.repository;

import com.myproject.crispysystem.accounts.model.Transaction;
import com.myproject.crispysystem.accounts.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    // Fetch all transactions for a specific account
    List<Transaction> findByAccount_AccountId(UUID accountId);

    // Fetch transactions related to a specific account (sender or receiver)
    List<Transaction> findByAccount_AccountIdOrRelatedAccountID(UUID accountId, UUID relatedAccountId);

    // Fetch transactions of a specific type for an account
    List<Transaction> findByAccount_AccountIdAndTransactionType(UUID accountId, TransactionType transactionType);

    // Fetch all transactions with a description containing a specific keyword
    List<Transaction> findByDescriptionContainingIgnoreCase(String keyword);
}
