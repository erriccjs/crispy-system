package com.myproject.crispysystem.common.security;

import com.myproject.crispysystem.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static reactor.core.publisher.Mono.when;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        jwtUtil = new JwtUtil(redisTemplate);
    }
    private User user;
    @Test
    void generateAndValidateToken(){
        String subject = "testUser";
        String token = jwtUtil.generateToken(subject);

        assertNotNull(token, "Generated token should not null");
        assertTrue(jwtUtil.validateToken(token), "Token should be valid");
        assertEquals(subject, jwtUtil.getSubjectFromToken(token), "Subject should match with the original");
    }

    @Test
    void validateInvalidToken(){
        String invalidToken = "wrong.invalid.token";
        assertFalse(jwtUtil.validateToken(invalidToken), "Invalid token should not valid");
    }
}
