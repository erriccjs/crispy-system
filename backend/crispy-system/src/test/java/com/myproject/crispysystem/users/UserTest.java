package com.myproject.crispysystem.users;

import com.myproject.crispysystem.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        //Initialize
        user = new User();
    }

    @Test
    void testSetAndGetId() {
        UUID id = UUID.randomUUID();
        user.setId(id);

        assertEquals(id, user.getId());
    }

    @Test
    void testConstructor(){
        UUID id = UUID.randomUUID();
        String encryptedUsername = "encryptedUsername";
        String hashedUsername = "hashedUsername";
        String password = "hashedPassword";

        User constructedUser = new User(id,encryptedUsername,hashedUsername, password);

        assertEquals(id, constructedUser.getId());
        assertEquals(encryptedUsername, constructedUser.getUsernameEncrypted());
        assertEquals(hashedUsername, constructedUser.getUsernameHashed());
        assertEquals(password, constructedUser.getPassword());
        assertNotNull(constructedUser.getCreatedAt());
        assertNotNull(constructedUser.getUpdatedAt());
    }

    @Test
    void testPrePersistSetsCreatedAtAndUpdatedAt() {
        user.prePersist();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void testPreUpdateSetsUpdatedAt() {
        user.preUpdate();

        assertNotNull(user.getUpdatedAt());
    }
}
