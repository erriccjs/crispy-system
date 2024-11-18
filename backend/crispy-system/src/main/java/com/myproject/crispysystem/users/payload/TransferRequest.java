package com.myproject.crispysystem.users.payload;

import java.math.BigDecimal;

public class TransferRequest {
    private String receiverUsername;
    private BigDecimal amount;

    // Getters and Setters
    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername){
        this.receiverUsername = receiverUsername;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
