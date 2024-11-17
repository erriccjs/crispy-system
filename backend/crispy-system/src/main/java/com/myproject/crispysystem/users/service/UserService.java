package com.myproject.crispysystem.users.service;

import com.myproject.crispysystem.common.security.JwtUtil;
import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.users.model.User;
import com.myproject.crispysystem.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String password) throws Exception {
        String hashedUsername = HashUtil.sha256(username); // Hash the username for lookup
        Optional<User> existingUser = userRepository.findByUsernameHashed(hashedUsername);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            //Validate password
            if (!HashUtil.sha256(password).equals(user.getPassword())){
                throw new IllegalArgumentException("Invalid username or password");
            }
            return jwtUtil.generateToken(user.getId().toString());
        } else {
            User newUser = createNewUser(username,password);
            userRepository.save(newUser);
            return jwtUtil.generateToken(newUser.getId().toString());
        }
    }

    public void logout(String token){
        if (jwtUtil.validateToken(token)){
            jwtUtil.blacklistToken(token);
        }
    }

    private User createNewUser(String username, String password) throws Exception {
        String hashedPassword = HashUtil.sha256(password);
        String encryptedUsername = EncryptionUtil.encrypt(username);
        String hashedUsername = HashUtil.sha256(username);
        return new User(UUID.randomUUID(), encryptedUsername, hashedUsername, hashedPassword);
    }

}
