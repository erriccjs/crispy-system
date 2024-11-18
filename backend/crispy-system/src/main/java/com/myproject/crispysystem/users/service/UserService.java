package com.myproject.crispysystem.users.service;

import com.myproject.crispysystem.accounts.model.Account;
import com.myproject.crispysystem.accounts.model.Owed;
import com.myproject.crispysystem.accounts.repository.AccountRepository;
import com.myproject.crispysystem.accounts.repository.OwedRepository;
import com.myproject.crispysystem.accounts.service.AccountService;
import com.myproject.crispysystem.common.security.JwtUtil;
import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.users.model.User;
import com.myproject.crispysystem.users.payload.LoginResponse;
import com.myproject.crispysystem.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;
    private final OwedRepository owedRepository;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil,
                       AccountService accountService,
                       AccountRepository accountRepository, OwedRepository owedRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.owedRepository = owedRepository;
    }

    @Transactional
    public LoginResponse login(String username, String password) throws Exception {
        String hashedUsername = HashUtil.sha256(username); // Hash the username for lookup
        Optional<User> existingUser = userRepository.findByUsernameHashed(hashedUsername);

        User user;
        String token;
        BigDecimal balance;
        Account account;

        if (existingUser.isPresent()) {
            user = existingUser.get();

            // Validate password
            if (!HashUtil.sha256(password).equals(user.getPassword())) {
                throw new IllegalArgumentException("Invalid username or password");
            }
            token = jwtUtil.generateToken(user.getId().toString());
            account = accountRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        } else {
            User newUser = createNewUser(username, password);
            User savedUser = userRepository.save(newUser);

            // Create account and explicitly flush to ensure visibility
            Account newAccount = new Account(UUID.randomUUID(), savedUser, BigDecimal.valueOf(0.0));
            accountRepository.save(newAccount);
            user = savedUser;
            token = jwtUtil.generateToken(user.getId().toString());
        }
        account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found after save"));
        balance = account.getBalance();

        // Retrieve owed information
        List<Owed> asLender = owedRepository.findByLenderAccount(account);
        List<Owed> asBorrower = owedRepository.findByBorrowerAccount(account);

        List<String> owedDescriptions = new ArrayList<>();

        // Add lender information
        for (Owed owed : asLender) {
            if (owed.getOwedAmount().compareTo(BigDecimal.ZERO) > 0) {
                String borrowerName = EncryptionUtil.decrypt(owed.getBorrowerAccount().getUser().getUsernameEncrypted());
                owedDescriptions.add(String.format("Owed $%.2f from %s", owed.getOwedAmount(), borrowerName));
            }
        }

        // Add borrower information
        for (Owed owed : asBorrower) {
            if (owed.getOwedAmount().compareTo(BigDecimal.ZERO) > 0) {
                String lenderName = EncryptionUtil.decrypt(owed.getLenderAccount().getUser().getUsernameEncrypted());
                owedDescriptions.add(String.format("Owed $%.2f to %s", owed.getOwedAmount(), lenderName));
            }
        }

        return new LoginResponse(username, balance, token, owedDescriptions);
    }


    public String logout(String token) {
        if (jwtUtil.validateToken(token)) {
            String userId = jwtUtil.getSubjectFromToken(token);
            Optional<User> user = userRepository.findById(UUID.fromString(userId));

            String username;

            if (user.isPresent()) {
                try {
                    username = EncryptionUtil.decrypt(user.get().getUsernameEncrypted());
                    jwtUtil.blacklistToken(token);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return username;
            } else {
                throw new IllegalArgumentException("User not found for the provided token.");
            }
        } else {
            throw new IllegalArgumentException("Invalid or expired token.");
        }
    }

    private User createNewUser(String username, String password) throws Exception {
        String hashedPassword = HashUtil.sha256(password);
        String encryptedUsername = EncryptionUtil.encrypt(username);
        String hashedUsername = HashUtil.sha256(username);
        return new User(UUID.randomUUID(), encryptedUsername, hashedUsername, hashedPassword);
    }

}
