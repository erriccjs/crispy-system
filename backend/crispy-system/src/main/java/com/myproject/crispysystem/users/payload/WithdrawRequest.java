package com.myproject.crispysystem.users.payload;

import java.math.BigDecimal;

public class WithdrawRequest {
    private BigDecimal amount;

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
