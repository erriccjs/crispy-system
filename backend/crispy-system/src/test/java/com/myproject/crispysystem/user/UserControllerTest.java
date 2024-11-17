package com.myproject.crispysystem.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.users.controller.UserController;
import com.myproject.crispysystem.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        Mockito.reset(userService);
    }

//    @Test
//    void login_success() throws Exception {
//        String username = "Alice";
//        String password = "p4ssw0rd";
//        String token = "mockedJwtToken";
//
//        when(userService.login(username, password, encryptionUtil)).thenReturn(token);
//
//        mockMvc.perform(post("/login")
//                .param("username", username)
//                .param("password", password))
//                .andExpect(status().isOk())
//                .andExpect(content().string(token));
//    }
}
