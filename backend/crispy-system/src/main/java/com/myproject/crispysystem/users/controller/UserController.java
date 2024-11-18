package com.myproject.crispysystem.users.controller;

import com.myproject.crispysystem.common.security.JwtUtil;
import com.myproject.crispysystem.users.payload.ApiResponse;
import com.myproject.crispysystem.users.payload.LoginRequest;
import com.myproject.crispysystem.users.payload.LoginResponse;
import com.myproject.crispysystem.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil){
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", loginResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred during login"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = extractTokenFromHeader(tokenHeader);
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid or missing token"));
            }
            String username = userService.logout(token);
            return ResponseEntity.ok(new ApiResponse<>(true, "Goodbye, " + username + "!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred during logout"));
        }
    }


    private String extractTokenFromHeader(String tokenHeader){
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")){
            return tokenHeader.substring(7);
        }
        return null;
    }

}
