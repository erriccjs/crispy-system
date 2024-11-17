package com.myproject.crispysystem.users.controller;

import com.myproject.crispysystem.common.security.JwtUtil;
import com.myproject.crispysystem.users.payload.ApiResponse;
import com.myproject.crispysystem.users.payload.LoginRequest;
import com.myproject.crispysystem.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> authenticateUser( @RequestBody LoginRequest loginRequest){
        try {
            String jwt = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(new ApiResponse<>("Login successful", jwt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("An error occurred during login"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String tokenHeader) {
        String token = extractTokenHeader(tokenHeader);
        if (token == null || !jwtUtil.validateToken(token)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("Invalid or missing token"));
        }

        userService.logout(token);
        return ResponseEntity.ok(new ApiResponse<>("Logout successful"));
    }

    private String extractTokenHeader(String tokenHeader){
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")){
            return tokenHeader.substring(7);
        }
        return null;
    }

}
