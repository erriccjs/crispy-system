package com.myproject.crispysystem.users;


import com.myproject.crispysystem.users.controller.UserController;
import com.myproject.crispysystem.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@WebMvcTest(UserController.class)
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp(){
        Mockito.reset(userService);
    }

//    @Test
//    void login_success() throws Exception {
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setUsername("username");
//        loginRequest.setPassword("password");
//        String jwtToken = "mocked.jwt.token";
//
//        when(userService.login(loginRequest.getUsername(), loginRequest.getPassword())).thenReturn(jwtToken);
//
//        mockMvc.perform(post("/api/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Login successful"))
//                .andExpect(jsonPath("$.data").value(jwtToken));
//
//        verify(userService, times(1)).login(loginRequest.getUsername(), loginRequest.getPassword());
//    }
}
