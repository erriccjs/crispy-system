package com.myproject.crispysystem.user;

import com.myproject.crispysystem.common.security.JwtUtil;
import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.constants.Constants;
import com.myproject.crispysystem.users.model.User;
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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @InjectMocks
    private UserService userService;

    private static final String VALID_USERNAME = "Alice";
    private static final String VALID_PASSWORD = "p4ssw0rd";
    private static final String MOCK_JWT_TOKEN = "mocked.jwt.token";
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, jwtUtil);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void login_existingUser_success()  throws Exception {
        String encryptedUsername = EncryptionUtil.encrypt(VALID_USERNAME);
        String hashedUsername = HashUtil.sha256(VALID_USERNAME);
        String hashedPassword = HashUtil.sha256(VALID_PASSWORD);

        User existingUser = new User(UUID.randomUUID(), encryptedUsername,hashedUsername, hashedPassword);

        when(userRepository.findByUsernameHashed(hashedUsername)).thenReturn(Optional.of(existingUser));
        when(jwtUtil.generateToken(existingUser.getId().toString())).thenReturn(MOCK_JWT_TOKEN);
        String result = userService.login(VALID_USERNAME, VALID_PASSWORD);

        // Assert
        assertNotNull(result, "Result JWT Token should not null");
        assertEquals(MOCK_JWT_TOKEN, result, "Generated JWT token should match mocked token");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_newUser_createsEncryptedAndHashedUser() throws Exception {
        String hashedUsername = HashUtil.sha256(VALID_USERNAME);
        String hashedPassword = HashUtil.sha256(VALID_PASSWORD);

        when(userRepository.findByUsernameHashed(hashedUsername)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(anyString())).thenReturn(MOCK_JWT_TOKEN);
        String result = userService.login(VALID_USERNAME, VALID_PASSWORD);

        // Capture the saved user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // Assert
        assertNotNull(result, "Result JWT token should not null");
        assertNotNull(savedUser, "Saved user should not null");
        assertNotEquals(VALID_USERNAME,savedUser.getUsernameEncrypted(), "Saved encrypted username should not match with the plaintext username ");
        assertEquals(hashedUsername,savedUser.getUsernameHashed(), "Saved hashed username should exactly match with the hashedUsername");
        assertEquals(hashedPassword, savedUser.getPassword(), "Saved hashed password should exactly match with the hashedPassword");
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

            // Act: Call login
            String resultToken = userService.login(VALID_USERNAME, VALID_PASSWORD);

            // Assert: Token is valid before logout
            assertEquals(MOCK_JWT_TOKEN, resultToken, "Token should match the mocked token");

            // Mock the RedisTemplate for logout
            when(jwtUtil.validateToken(resultToken)).thenReturn(true);
            doNothing().when(jwtUtil).blacklistToken(resultToken);

            // Call logout
            userService.logout(resultToken);

            verify(jwtUtil, times(1)).validateToken(resultToken);
            verify(jwtUtil, times(1)).blacklistToken(resultToken);
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






