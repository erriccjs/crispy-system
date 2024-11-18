package com.myproject.crispysystem.accounts;

import com.myproject.crispysystem.accounts.model.Account;
import com.myproject.crispysystem.accounts.repository.AccountRepository;
import com.myproject.crispysystem.accounts.service.AccountService;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_ACCOUNT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_success() throws Exception{
        User user = new User(VALID_USER_ID, "testUser", HashUtil.sha256("username"), "hashedPassword");

        when(accountRepository.existsByUser(user)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = accountService.createAccount(user);

        assertNotNull(account);
        assertEquals(VALID_USER_ID, account.getUser().getId());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void getAccount_success(){
        User user = new User(VALID_USER_ID, "testUser","hashedUsername", "hashedPassword");

        when(accountRepository.existsByUser(user)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = accountService.createAccount(user);

        when(accountRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(account));

        Account result = accountService.getAccountByUserId(VALID_USER_ID);

        assertNotNull(result);
        assertEquals(VALID_USER_ID, result.getUser().getId());
        verify(accountRepository, times(1)).findByUserId(VALID_USER_ID);
    }

    //Negative case
    @Test
    void createAccount_alreadyExists(){
        User user = new User(VALID_USER_ID, "testUser","hashedUsername", "hashedPassword");

        when(accountRepository.existsByUser(user)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account existingAccount = accountService.createAccount(user);

        when(accountRepository.existsByUser(user)).thenReturn(true);
        when(accountRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(existingAccount));

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(user));
    }

    @Test
    void getAccount_notFound(){
        when(accountRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountByUserId(VALID_USER_ID));

    }
}
