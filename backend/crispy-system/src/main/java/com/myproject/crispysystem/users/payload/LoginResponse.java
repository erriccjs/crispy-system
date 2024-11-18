package com.myproject.crispysystem.users.payload;

import java.math.BigDecimal;
import java.util.List;

public class LoginResponse {
    private String username;
    private BigDecimal balance;
    private String token;
    private List<String> owedDetails;

    public LoginResponse(String username, BigDecimal balance, String token, List<String> owedDetails) {
        this.username = username;
        this.balance = balance;
        this.token = token;
        this.owedDetails = owedDetails;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getOwedDetails() {
        return owedDetails;
    }

    public void setOwedDetails(List<String> owedDetails) {
        this.owedDetails = owedDetails;
    }
}
