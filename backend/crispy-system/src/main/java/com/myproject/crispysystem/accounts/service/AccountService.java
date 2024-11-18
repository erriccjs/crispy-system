package com.myproject.crispysystem.accounts.service;

import com.myproject.crispysystem.accounts.model.Account;
import com.myproject.crispysystem.accounts.repository.AccountRepository;
import com.myproject.crispysystem.users.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    /**
     * Create a new account for a user.
     * @param user The user for whom the account is being created.
     * @return The created account.
     */
    public Account createAccount(User user){
        if (accountRepository.existsByUser(user)) {
            throw new IllegalArgumentException("Account already exists for the user.");
        }

        Account account = new Account();
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    /**
     * Get an account by user ID.
     * @param userId The user ID.
     * @return The account associated with the user.
     */
    public Account getAccountByUserId(UUID userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No account found for the user ID: " + userId));
    }

}

