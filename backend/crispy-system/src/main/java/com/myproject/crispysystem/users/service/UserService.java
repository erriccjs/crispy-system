package com.myproject.crispysystem.users.service;

import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.common.util.LoggerUtil;
import com.myproject.crispysystem.users.model.User;
import com.myproject.crispysystem.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Autowired
    public UserService(UserRepository userRepository, EncryptionUtil encryptionUtil) {
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public User login(String username, String password) throws Exception {
        String hashedUsername = HashUtil.sha256(username); // Hash the username for lookup
        Optional<User> existingUser = userRepository.findByUsernameHashed(hashedUsername);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            //Validate password
            if (!HashUtil.sha256(password).equals(user.getPassword())){
                throw new IllegalArgumentException("Invalid username or password");
            }
            return user;
        } else {
            User newUser = new User(UUID.randomUUID(), username, HashUtil.sha256(password), encryptionUtil);
            userRepository.save(newUser);
            return newUser;
        }
    }

}
