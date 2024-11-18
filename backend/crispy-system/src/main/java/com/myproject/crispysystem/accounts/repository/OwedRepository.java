package com.myproject.crispysystem.accounts.repository;

import com.myproject.crispysystem.accounts.model.Account;
import com.myproject.crispysystem.accounts.model.Owed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwedRepository extends JpaRepository<Owed, UUID> {
    Optional<Owed> findByBorrowerAccountAndLenderAccount(Account borrowerAccount, Account lenderAccount);
    List<Owed> findByBorrowerAccount(Account borrowerAccount);
    List<Owed> findByLenderAccount(Account lenderAccount);
}

