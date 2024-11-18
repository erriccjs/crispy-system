package com.myproject.crispysystem.accounts.repository;

import com.myproject.crispysystem.accounts.model.Account;
import com.myproject.crispysystem.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    //Find account by relational userId
    Optional<Account> findByUserId(UUID userId);

    boolean existsByUser(User user);
}
