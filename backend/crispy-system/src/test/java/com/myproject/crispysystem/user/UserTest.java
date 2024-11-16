package com.myproject.crispysystem.user;

import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    EncryptionUtil encryptionUtil = new EncryptionUtil("ZuW0XhTAJKIq/YyhphrMCJJvag6fF3ykrZ/0X0ZrsCM=");
    User user;

    @BeforeEach
    void setUp() {
        //Initialize
        user = new User(encryptionUtil);
    }

    //Positive Testcases
    @Test
    void testSetUsername_EncryptAndHashCorrectly(){
        String plainUsername = "Alice";

        user.setUsername(plainUsername);

        //Assert
        assertNotNull(user.getUsernameEncrypted(), "Encrypted username should not be null.");
        assertNotNull(user.getUsernameHashed(), "Hashed username should not be null.");
        assertNotEquals(plainUsername, user.getUsernameEncrypted(), "Encrypted username should not equal with the plaintext.");
        try {
            assertEquals(HashUtil.sha256(plainUsername), user.getUsernameHashed(), "Hashed username should match the SHA-256 of the plaintext");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetUsername_DecryptsCorrectly() {
        String plainUsername = "Alice";
        user.setUsername(plainUsername);

        String decryptUsername = user.getUsername();

        assertEquals(plainUsername, decryptUsername, "Decrypted username should match the original plaintext");
    }

    //Negative testcases
    @Test
    void testSetUsername_InvalidInput_ThrowsException() {
        String nullUsername = null;
        String emptyUsername = "";

        //Assert
        assertThrows(IllegalArgumentException.class, () -> user.setUsername(nullUsername), "Setting a null username should throw an exception.");
        assertThrows(IllegalArgumentException.class, () -> user.setUsername(emptyUsername), "setting an empty username should throw an exception.");
    }
}
