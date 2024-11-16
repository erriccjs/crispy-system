package com.myproject.crispysystem.user;

import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.users.model.User;
import com.myproject.crispysystem.users.repository.UserRepository;
import com.myproject.crispysystem.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    EncryptionUtil encryptionUtil = new EncryptionUtil("ZuW0XhTAJKIq/YyhphrMCJJvag6fF3ykrZ/0X0ZrsCM=");

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp(){

        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, encryptionUtil);
    }

    @Test
    void login_existingUser_success()  throws Exception {
        String username = "Alice";
        String password = "t3stP4ssw0rd";
        String hashedPassword = HashUtil.sha256(password);
        String hashedUsername = HashUtil.sha256(username);

        User existingUser = new User(UUID.randomUUID(), username, hashedPassword, encryptionUtil);

        when(userRepository.findByUsernameHashed(hashedUsername)).thenReturn(Optional.of(existingUser));

        User result = userService.login(username,password);

        // Assert
        assertNotNull(result, "Result user should not null");
        assertEquals(existingUser, result, "Return user should match the existing user");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_newUser_createsEncryptedAndHashedUser() throws Exception {
        String username = "Bob";
        String password = "t3stP4ssw0rd";
        String hashedUsername = HashUtil.sha256(username);
        String hashedPassword = HashUtil.sha256(password);

        when(userRepository.findByUsernameHashed(hashedUsername)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.login(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(hashedUsername, result.getUsernameHashed());
        assertEquals(hashedPassword, result.getPassword());
        assertEquals(username,result.getUsername()); //Verify the decrypted username is same with the plaintext username
        verify(userRepository, times(1)).save(any(User.class));
    }

//    @Test
//    void logout_loggedInUser_success() {
//
//        try {
//            userService.login("Alice", "p4ssw0rd");  // Assume the user is logged in
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        // Act
//        userService.logout();
//
//        // Assert
//        // (Here, you can check session or login status changes depending on how you track logged-in state)
//        assertFalse(userService.isLoggedIn());
//    }

    //Negative Test Case
    @Test
    void login_invalidPassword_throwsException() throws Exception{
        String username = "Alice";
        String password = "wr0ngP4ssw0rd";
        String hashedUsername = HashUtil.sha256(username);
        String correctHashedPassword = HashUtil.sha256("c0rr3ctP4ssw0rd");

        User existingUsers = new User(UUID.randomUUID(), username, correctHashedPassword, encryptionUtil);
        when(userRepository.findByUsernameHashed(hashedUsername)).thenReturn(Optional.of(existingUsers));

        assertThrows(IllegalArgumentException.class, () -> userService.login(username, password), "Invalid username or password");
    }
}






