package com.myproject.crispysystem.accounts.model;

import com.myproject.crispysystem.users.model.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID accountId;
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
    private BigDecimal balance;

}
