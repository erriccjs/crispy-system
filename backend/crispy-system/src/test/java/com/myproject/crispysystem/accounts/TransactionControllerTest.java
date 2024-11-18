package com.myproject.crispysystem.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.crispysystem.accounts.controller.TransactionController;
import com.myproject.crispysystem.accounts.model.Transaction;
import com.myproject.crispysystem.accounts.model.TransactionType;
import com.myproject.crispysystem.accounts.service.TransactionService;
import com.myproject.crispysystem.users.payload.ApiResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

//    @Test
//    void deposit_success() throws Exception {
//        String token = "mock.jwt.token";
//        BigDecimal amount = BigDecimal.valueOf(100.0);
//
//        Transaction mockTransaction = new Transaction();
//        mockTransaction.setAmount(amount);
//        mockTransaction.setTransactionType(TransactionType.CREDIT);
//
//        when(transactionService.deposit(token, amount)).thenReturn(mockTransaction);
//
//        mockMvc.perform(post("/api/transactions/deposit")
//                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                        .param("amount", amount.toString())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.amount").value(amount.toString()))
//                .andExpect(jsonPath("$.transactionType").value("CREDIT"));
//
//        verify(transactionService, times(1)).deposit(token, amount);
//    }
}
