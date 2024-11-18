package com.myproject.crispysystem.users;

import com.myproject.crispysystem.accounts.model.Account;
import com.myproject.crispysystem.accounts.repository.AccountRepository;
import com.myproject.crispysystem.accounts.repository.OwedRepository;
import com.myproject.crispysystem.accounts.service.AccountService;
import com.myproject.crispysystem.common.security.JwtUtil;
import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.users.model.User;
import com.myproject.crispysystem.users.payload.LoginResponse;
import com.myproject.crispysystem.users.repository.UserRepository;
import com.myproject.crispysystem.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private RedisTemplate<String,Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OwedRepository owedRepository;

    @InjectMocks
    private UserService userService;



    private static final String VALID_USERNAME = "Alice";
    private static final String VALID_PASSWORD = "p4ssw0rd";
    private static final String MOCK_JWT_TOKEN = "mocked.jwt.token";
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, jwtUtil, accountService,accountRepository, owedRepository);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void login_existingUser_success() throws Exception {
        String encryptedUsername = EncryptionUtil.encrypt(VALID_USERNAME);
        String hashedUsername = HashUtil.sha256(VALID_USERNAME);
        String hashedPassword = HashUtil.sha256(VALID_PASSWORD);
        UUID userId = UUID.randomUUID();
        BigDecimal mockBalance = BigDecimal.valueOf(100.0);

        User existingUser = new User(userId, encryptedUsername, hashedUsername, hashedPassword);
        Account mockAccount = new Account(UUID.randomUUID(), existingUser, mockBalance);

        // Mock repository and utility behavior
        when(userRepository.findByUsernameHashed(hashedUsername)).thenReturn(Optional.of(existingUser));
        when(jwtUtil.generateToken(userId.toString())).thenReturn(MOCK_JWT_TOKEN);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(mockAccount));

        // Act
        LoginResponse result = userService.login(VALID_USERNAME, VALID_PASSWORD);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(MOCK_JWT_TOKEN, result.getToken(), "Generated JWT token should match mocked token");
        assertEquals(VALID_USERNAME, result.getUsername(), "Username should match the provided username");
        assertEquals(mockBalance, result.getBalance(), "Balance should match the mocked account balance");
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void login_newUser_createsEncryptedAndHashedUser() throws Exception {
        String hashedUsername = HashUtil.sha256(VALID_USERNAME);
        String hashedPassword = HashUtil.sha256(VALID_PASSWORD);

        // Mock user repository to indicate no existing user
        when(userRepository.findByUsernameHashed(hashedUsername)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // Mock AccountService behavior for creating a new account
        when(accountService.createAccount(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new Account(UUID.randomUUID(), user, BigDecimal.ZERO);
        });

        // Mock JWT token generation
        when(jwtUtil.generateToken(anyString())).thenReturn(MOCK_JWT_TOKEN);

        // Mock account repository
        Account mockAccount = new Account(UUID.randomUUID(), new User(), BigDecimal.ZERO);
        when(accountRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(mockAccount));


        // Act: Call the login method for a new user
        LoginResponse result = userService.login(VALID_USERNAME, VALID_PASSWORD);

        // Capture the saved user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // Assertions for the result
        assertNotNull(result, "Result should not be null");
        assertNotNull(savedUser, "Saved user should not be null");
        assertEquals(MOCK_JWT_TOKEN, result.getToken(), "Generated token should match mocked token");
        assertEquals(VALID_USERNAME, result.getUsername(), "Result username should match with the new username");
        assertEquals(BigDecimal.ZERO, result.getBalance(), "Result balance should be zero for a new account");

        // Assertions for the saved user
        assertNotEquals(VALID_USERNAME, savedUser.getUsernameEncrypted(), "Saved encrypted username should be different from plaintext username");
        assertEquals(hashedUsername, savedUser.getUsernameHashed(), "Saved hashed username should exactly match hashedUsername");
        assertEquals(hashedPassword, savedUser.getPassword(), "Saved hashed password should exactly match hashedPassword");
    }


    @Test
    void logout_loggedInUser_success() {
        try {
            // Mock the behavior of login and token generation
            String encryptedUsername = EncryptionUtil.encrypt(VALID_USERNAME);
            String hashedUsername = HashUtil.sha256(VALID_USERNAME);
            String hashedPassword = HashUtil.sha256(VALID_PASSWORD);

            User user = new User(UUID.randomUUID(), encryptedUsername, hashedUsername, hashedPassword);
            when(userRepository.findByUsernameHashed(hashedUsername)).thenReturn(Optional.of(user));

            when(jwtUtil.generateToken(user.getId().toString())).thenReturn(MOCK_JWT_TOKEN);

            // Mock account repository
            Account mockAccount = new Account(UUID.randomUUID(),user, BigDecimal.ZERO);
            when(accountRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(mockAccount));

            LoginResponse loginResponse = userService.login(VALID_USERNAME, VALID_PASSWORD);
            String token = loginResponse.getToken();

            // Assert: Token is valid before logout
            assertEquals(MOCK_JWT_TOKEN, token, "Token should match the mocked token");

            // Mock the RedisTemplate for logout
            when(jwtUtil.validateToken(token)).thenReturn(true);
            when(jwtUtil.getSubjectFromToken(token)).thenReturn(String.valueOf(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            doNothing().when(jwtUtil).blacklistToken(token);

            // Call logout
            String username = userService.logout(token);

            assertEquals(VALID_USERNAME,username,"Response username should be match with the token owner username");
            verify(jwtUtil, times(1)).validateToken(token);
            verify(jwtUtil, times(1)).blacklistToken(token);
        } catch (Exception e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }

    //Negative Test Case
    @Test
    void login_invalidPassword_throwsException() throws Exception{
        String wrongPassword = "wr0ngP4ssw0rd";
        String encryptedUsername = EncryptionUtil.encrypt(VALID_USERNAME);
        String hashedUsername = HashUtil.sha256(VALID_USERNAME);
        String correctHashedPassword = HashUtil.sha256("c0rr3ctP4ssw0rd");

        User existingUsers = new User(UUID.randomUUID(), encryptedUsername, hashedUsername, correctHashedPassword);
        when(userRepository.findByUsernameHashed(hashedUsername)).thenReturn(Optional.of(existingUsers));

        assertThrows(IllegalArgumentException.class, () -> userService.login(VALID_USERNAME, wrongPassword), "Invalid username or password");
    }
}






