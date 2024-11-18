package com.myproject.crispysystem.accounts.service;

import com.myproject.crispysystem.accounts.model.Account;
import com.myproject.crispysystem.accounts.model.Owed;
import com.myproject.crispysystem.accounts.model.Transaction;
import com.myproject.crispysystem.accounts.model.TransactionType;
import com.myproject.crispysystem.accounts.repository.AccountRepository;
import com.myproject.crispysystem.accounts.repository.OwedRepository;
import com.myproject.crispysystem.accounts.repository.TransactionRepository;
import com.myproject.crispysystem.common.security.JwtUtil;
import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.users.model.User;
import com.myproject.crispysystem.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OwedRepository owedRepository;

    @Autowired
    public TransactionService(OwedRepository owedRepository, JwtUtil jwtUtil, AccountRepository accountRepository,
                              TransactionRepository transactionRepository, UserRepository userRepository) {
        this.owedRepository = owedRepository;
        this.jwtUtil = jwtUtil;
        this.accountRepository = accountRepository;
        this.transactionRepository=transactionRepository;
        this.userRepository = userRepository;
    }

    public Map<String,Object> deposit(String token, BigDecimal amount) {
        UUID userId = UUID.fromString(jwtUtil.getSubjectFromToken(token));
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Handle owed balance first
        List<Owed> owedList = owedRepository.findByBorrowerAccount(account);
        for (Owed owed : owedList) {
            BigDecimal owedAmount = owed.getOwedAmount();
            Account lenderAccount = owed.getLenderAccount();

            if (amount.compareTo(owedAmount) >= 0) {
                lenderAccount.setBalance(lenderAccount.getBalance().add(owedAmount));
                amount = amount.subtract(owedAmount);
                owedRepository.delete(owed); // Owed fully paid
            } else {
                lenderAccount.setBalance(lenderAccount.getBalance().add(amount));
                owed.setOwedAmount(owedAmount.subtract(amount));
                owedRepository.save(owed);
                amount = BigDecimal.ZERO;
                break;
            }
            accountRepository.save(lenderAccount);
        }

        // Add remaining deposit to account balance
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setAmount(amount);
        transaction.setBalanceAfterTransaction(account.getBalance());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription("Deposit");
        transactionRepository.save(transaction);

        // Retrieve owed information
        List<Owed> asLender = owedRepository.findByLenderAccount(account);
        List<Owed> asBorrower = owedRepository.findByBorrowerAccount(account);

        List<String> owedDescriptions = new ArrayList<>();

        // Add lender information
        for (Owed owed : asLender) {
            if (owed.getOwedAmount().compareTo(BigDecimal.ZERO) > 0) {
                String borrowerName = null;
                try {
                    borrowerName = EncryptionUtil.decrypt(owed.getBorrowerAccount().getUser().getUsernameEncrypted());
                    owedDescriptions.add(String.format("Owed $%.2f from %s", owed.getOwedAmount(), borrowerName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }

        // Add borrower information
        for (Owed owed : asBorrower) {
            if (owed.getOwedAmount().compareTo(BigDecimal.ZERO) > 0) {
                String lenderName = null;
                try {
                    lenderName = EncryptionUtil.decrypt(owed.getLenderAccount().getUser().getUsernameEncrypted());
                    owedDescriptions.add(String.format("Owed $%.2f to %s", owed.getOwedAmount(), lenderName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("transaction", transaction);
        response.put("owed", owedDescriptions);

        return response;
    }


    public Map<String,Object> withdraw(String token, BigDecimal amount) {
        String userId = jwtUtil.getSubjectFromToken(token);
        Account account = accountRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Check if the account has sufficient funds
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal");
        }

        // Update balance
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Create and save transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setAmount(amount);
        transaction.setBalanceAfterTransaction(account.getBalance());
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Retrieve owed information
        List<Owed> asLender = owedRepository.findByLenderAccount(account);
        List<Owed> asBorrower = owedRepository.findByBorrowerAccount(account);

        List<String> owedDescriptions = new ArrayList<>();

        // Add lender information
        for (Owed owed : asLender) {
            if (owed.getOwedAmount().compareTo(BigDecimal.ZERO) > 0) {
                String borrowerName = null;
                try {
                    borrowerName = EncryptionUtil.decrypt(owed.getBorrowerAccount().getUser().getUsernameEncrypted());
                    owedDescriptions.add(String.format("Owed $%.2f from %s", owed.getOwedAmount(), borrowerName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }

        // Add borrower information
        for (Owed owed : asBorrower) {
            if (owed.getOwedAmount().compareTo(BigDecimal.ZERO) > 0) {
                String lenderName = null;
                try {
                    lenderName = EncryptionUtil.decrypt(owed.getLenderAccount().getUser().getUsernameEncrypted());
                    owedDescriptions.add(String.format("Owed $%.2f to %s", owed.getOwedAmount(), lenderName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("transaction", transaction);
        response.put("owed", owedDescriptions);

        return response;
    }

    public Map<String, Object> transfer(String token, String receiverUsername, BigDecimal amount) throws Exception {
        UUID senderUserId = UUID.fromString(jwtUtil.getSubjectFromToken(token));
        Account senderAccount = accountRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

        User receiverUser = userRepository.findByUsernameHashed(HashUtil.sha256(receiverUsername))
                .orElseThrow(() -> new IllegalArgumentException("Receiver user not found"));
        Account receiverAccount = accountRepository.findByUserId(receiverUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

        BigDecimal transferAmount = amount;
        BigDecimal owedAmount = BigDecimal.ZERO;

        // Handle owed balance if sender is a borrower
        Optional<Owed> borrowerOwed = owedRepository.findByBorrowerAccountAndLenderAccount(senderAccount, receiverAccount);
        Optional<Owed> lenderOwed = owedRepository.findByBorrowerAccountAndLenderAccount(receiverAccount,senderAccount);
        if (borrowerOwed.isPresent()) {
            Owed owed = borrowerOwed.get();

            // Add the transfer amount to the owed balance
            owed.setOwedAmount(owed.getOwedAmount().add(transferAmount));
            owedRepository.save(owed);

            // Entire transfer is added to owed balance
            transferAmount = BigDecimal.ZERO;
        } else if (lenderOwed.isPresent()){
            Owed owed = lenderOwed.get();

            //Reduce amount of owed if owed amount > transferAmount
            if (owed.getOwedAmount().compareTo(transferAmount) >0){
                owed.setOwedAmount(owed.getOwedAmount().subtract(transferAmount));
            }
            else {
                transferAmount = transferAmount.subtract(owed.getOwedAmount());
                owed.setOwedAmount(BigDecimal.ZERO);
                // Deduct from sender's balance
                senderAccount.setBalance(senderAccount.getBalance().subtract(transferAmount));
                accountRepository.save(senderAccount);
                receiverAccount.setBalance(receiverAccount.getBalance().add(transferAmount));
                accountRepository.save(receiverAccount);
            }
        } else {
            // If sender is not a borrower, check if sender has enough balance
            if (senderAccount.getBalance().compareTo(transferAmount) < 0) {
                // Sender doesn't have enough balance, calculate transfer and owed
                transferAmount = senderAccount.getBalance(); // Transfer what is available
                owedAmount = amount.subtract(transferAmount); // Remaining is owed

                // Create owed entry
                Owed owed = new Owed(senderAccount, receiverAccount, owedAmount);
                owedRepository.save(owed);
            }
            senderAccount.setBalance(senderAccount.getBalance().subtract(transferAmount));
            accountRepository.save(senderAccount);
            receiverAccount.setBalance(receiverAccount.getBalance().add(transferAmount));
            accountRepository.save(receiverAccount);
        }

        // Save transactions
        saveTransaction(senderAccount, TransactionType.DEBIT, amount.negate(), "Transfer to " + receiverUsername, receiverAccount.getAccountId());
        saveTransaction(receiverAccount, TransactionType.CREDIT, transferAmount, "Received from " + EncryptionUtil.decrypt(senderAccount.getUser().getUsernameEncrypted()), senderAccount.getAccountId());

        // Prepare result map
        Map<String, Object> result = new HashMap<>();
        result.put("balance", senderAccount.getBalance());
        if (owedAmount.compareTo(BigDecimal.ZERO) > 0) {
            result.put("owed", "Owed $" + owedAmount + " to " + receiverUsername);
        }
        return result;
    }


    private void saveTransaction(Account account, TransactionType type, BigDecimal amount, String description,UUID relatedAccountId) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfterTransaction(account.getBalance());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription(description);
        transaction.setRelatedAccountID(relatedAccountId);
        transactionRepository.save(transaction);
    }

    private String formatOwed(Account senderAccount, Account receiverAccount) {
        Optional<Owed> owed = owedRepository.findByBorrowerAccountAndLenderAccount(senderAccount, receiverAccount);
        String receiverUsername;

        try {
            receiverUsername = EncryptionUtil.decrypt(receiverAccount.getUser().getUsernameEncrypted());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (owed.isPresent() && owed.get().getOwedAmount().compareTo(BigDecimal.ZERO) > 0) {
            return String.format("Owed $%.2f to %s", owed.get().getOwedAmount(), receiverUsername);
        }
        return null;
    }
}
