package com.myproject.crispysystem.accounts.controller;

import com.myproject.crispysystem.accounts.service.TransactionService;
import com.myproject.crispysystem.users.payload.ApiResponse;
import com.myproject.crispysystem.users.payload.DepositRequest;
import com.myproject.crispysystem.users.payload.TransferRequest;
import com.myproject.crispysystem.users.payload.WithdrawRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestHeader("Authorization") String token,
                                     @RequestBody DepositRequest depositRequest) {
        try {
            // Validate input
            if (depositRequest.getAmount() == null || depositRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Deposit amount must be greater than zero"));
            }

            // Process deposit
            Map<String,Object> result = transactionService.deposit(extractTokenFromHeader(token), depositRequest.getAmount());
            return ResponseEntity.ok(new ApiResponse<>(true, "Deposit successful", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String token,
                                      @RequestBody WithdrawRequest withdrawRequest) {
        try {
            // Validate input parameters
            if (withdrawRequest.getAmount() == null || withdrawRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Withdraw amount must be greater than zero"));
            }

            // Perform the withdrawal
            Map<String,Object> result = transactionService.withdraw(extractTokenFromHeader(token), withdrawRequest.getAmount());

            // Return success response
            return ResponseEntity.ok(new ApiResponse<>(true, "Withdrawal successful", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestHeader("Authorization") String tokenHeader,
                                      @RequestBody TransferRequest transferRequest) {
        try {
            // Validate input parameters
            if (transferRequest.getReceiverUsername() == null || transferRequest.getReceiverUsername().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Receiver username is required"));
            }
            if (transferRequest.getAmount() == null || transferRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Transfer amount must be greater than zero"));
            }

            // Extract token
            String token = extractTokenFromHeader(tokenHeader);
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid or missing token"));
            }

            // Perform the transfer
            Map<String, Object> result = transactionService.transfer(token, transferRequest.getReceiverUsername(), transferRequest.getAmount());

            String message = String.format("Transferred $%.2f to %s", transferRequest.getAmount(), transferRequest.getReceiverUsername());
            // Return success response
            return ResponseEntity.ok(new ApiResponse<>(true, message, result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    private String extractTokenFromHeader(String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        }
        return null;
    }
}

