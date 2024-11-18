package com.myproject.crispysystem.accounts;

import com.myproject.crispysystem.accounts.model.Account;
import com.myproject.crispysystem.accounts.model.Transaction;
import com.myproject.crispysystem.accounts.repository.AccountRepository;
import com.myproject.crispysystem.accounts.repository.OwedRepository;
import com.myproject.crispysystem.accounts.repository.TransactionRepository;
import com.myproject.crispysystem.accounts.service.TransactionService;
import com.myproject.crispysystem.common.security.JwtUtil;
import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.users.model.User;
import com.myproject.crispysystem.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionsServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private OwedRepository owedRepository;

    @InjectMocks
    private TransactionService transactionService;


    private static final UUID VALID_ACCOUNT_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deposit_success() throws Exception{
        String token = "mocked.jwt.token";
        User user = new User(VALID_USER_ID,"encryptedUsername",HashUtil.sha256("username"),"hashedPassword");
        Account account = new Account(VALID_ACCOUNT_ID, user, BigDecimal.valueOf(100.0)); // Initial balance
        when(accountRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtUtil.getSubjectFromToken(token)).thenReturn(VALID_USER_ID.toString());

        Map<String,Object> result = transactionService.deposit(token,BigDecimal.valueOf(50.0));
        Transaction transaction = (Transaction) result.get("transaction");
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(150.0),transaction.getBalanceAfterTransaction());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void withdraw_success() throws Exception{
        String token = "mocked.jwt.token";
        User user = new User(VALID_USER_ID,"encryptedUsername",HashUtil.sha256("username"),"hashedPassword");
        Account account = new Account(VALID_ACCOUNT_ID, user, BigDecimal.valueOf(100.0));
        when(accountRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.getSubjectFromToken(token)).thenReturn(VALID_USER_ID.toString());

        Map<String, Object> result = transactionService.withdraw(token,BigDecimal.valueOf(50.0));
        Transaction transaction = (Transaction) result.get("transaction");
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(50.0),transaction.getBalanceAfterTransaction());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void transfer_fund_success() throws Exception {
        String token = "mocked.jwt.token";

        //Prepare sender user
        UUID senderUserId = UUID.randomUUID();
        String senderUsername = "Sender";
        String encryptedSender = EncryptionUtil.encrypt(senderUsername);
        User senderUser = new User(senderUserId, encryptedSender, HashUtil.sha256(senderUsername), "hashedPassword");
        UUID senderAccountId = UUID.randomUUID();

        //Prepare receiver user
        UUID receiverUserId = UUID.randomUUID();
        String receiverUsername = "Receiver";
        String encryptedReceiver = EncryptionUtil.encrypt(receiverUsername);
        User receiverUser = new User(receiverUserId, "encryptedReceiver", HashUtil.sha256("receiverUsername"), "hashedPassword");
        UUID receiverAccountId = UUID.randomUUID();

        // Mock sender and receiver accounts
        Account senderAccount = new Account(senderAccountId, senderUser, BigDecimal.valueOf(150.0)); // Initial balance is 150
        Account receiverAccount = new Account(receiverAccountId, receiverUser, BigDecimal.valueOf(250.0)); // Initial balance is 250

        // Mock JwtUtil to return the sender's user ID
        when(jwtUtil.getSubjectFromToken(token)).thenReturn(senderUserId.toString());
        when(accountRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(userRepository.findByUsernameHashed(HashUtil.sha256(receiverUsername))).thenReturn(Optional.of(receiverUser));
        when(accountRepository.findByUserId(receiverUserId)).thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Perform transfer
        Map<String, Object> result = transactionService.transfer(token, receiverUsername, BigDecimal.valueOf(100.0));

        // Assert sender account
        assertNotNull(result, "Result map should not be null");
        assertEquals(BigDecimal.valueOf(50.0), senderAccount.getBalance(), "Sender balance should decrease to 50");

        // Assert receiver account
        assertEquals(BigDecimal.valueOf(350.0), receiverAccount.getBalance(), "Receiver balance should increase to 350");

        // Verify repository interactions
        verify(accountRepository, times(1)).save(senderAccount);
        verify(accountRepository, times(1)).save(receiverAccount);
        verify(transactionRepository, times(2)).save(any(Transaction.class)); // One transaction for sender, one for receiver

        // Assert result map
        assertEquals(BigDecimal.valueOf(50.0), result.get("balance"), "Result map should include the updated sender balance");
        assertNull(result.get("owed"), "No owed amount should be present for a successful transfer with sufficient balance");
    }

    @Test
    void transfer_insufficientFunds_allowsOwedBalance() throws Exception {
        String token = "mocked.jwt.token";

        //Prepare Sender User
        UUID senderUserId = UUID.randomUUID();
        String senderUsername = "Sender";
        String encryptedSender = EncryptionUtil.encrypt(senderUsername);
        User senderUser = new User(senderUserId, encryptedSender, HashUtil.sha256(senderUsername), "hashedPassword");
        UUID senderAccountId = UUID.randomUUID();

        //Prepare Receiver User
        UUID receiverUserId = UUID.randomUUID();
        String receiverUsername = "Receiver";
        String encryptedReceiver = EncryptionUtil.encrypt(receiverUsername);
        User receiverUser = new User(receiverUserId, encryptedReceiver, HashUtil.sha256(receiverUsername), "hashedPassword");
        UUID receiverAccountId = UUID.randomUUID();

        // Mock sender and receiver accounts
        Account senderAccount = new Account(senderAccountId, senderUser, BigDecimal.valueOf(50.0)); // Initial balance is 50
        Account receiverAccount = new Account(receiverAccountId, receiverUser, BigDecimal.valueOf(100.0));

        // Mock JwtUtil to return the sender's user ID
        when(jwtUtil.getSubjectFromToken(token)).thenReturn(senderUserId.toString());
        when(accountRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(userRepository.findByUsernameHashed(HashUtil.sha256(receiverUsername))).thenReturn(Optional.of(receiverUser));
        when(accountRepository.findByUserId(receiverUserId)).thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Perform transfer
        Map<String, Object> result = transactionService.transfer(token, receiverUsername, BigDecimal.valueOf(200.0));

        // Assert sender account
        assertNotNull(result, "Result map should not be null");
        assertEquals(BigDecimal.valueOf(0.0), senderAccount.getBalance(), "Sender balance should be 0");
        assertEquals(senderAccountId, senderAccount.getAccountId(), "Sender account ID should match");

        // Assert receiver account
        assertEquals(BigDecimal.valueOf(150.0), receiverAccount.getBalance(), "Receiver balance should increase by 100");
        assertEquals(receiverAccountId, receiverAccount.getAccountId(), "Receiver account ID should match");

        // Verify owed balance result
        assertTrue(result.containsKey("owed"), "Result map should include owed balance information");
        assertEquals("Owed $150.0 to Receiver", result.get("owed"), "Owed balance information should match");

        // Verify repository interactions
        verify(accountRepository, times(1)).save(senderAccount);
        verify(accountRepository, times(1)).save(receiverAccount);
        verify(transactionRepository, times(2)).save(any(Transaction.class)); // One for sender, one for receiver
    }

    //Negative case
    @Test
    void withdraw_insufficientFunds() throws Exception{
        String token = "mocked.jwt.token";
        User user = new User(VALID_USER_ID,"encryptedUsername",HashUtil.sha256("username"),"hashedPassword");
        Account account = new Account(VALID_ACCOUNT_ID, user, BigDecimal.valueOf(100.0));
        when(accountRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(account));
        when(jwtUtil.getSubjectFromToken(token)).thenReturn(VALID_USER_ID.toString());

        assertThrows(IllegalArgumentException.class, () -> transactionService.withdraw(token,BigDecimal.valueOf(200.0)));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(account);
    }

}
