package com.myproject.crispysystem.users.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name="username", nullable = false, unique = true)
    private String usernameEncrypted; //Encrypted username

    @Column(name="username_hashed", nullable = false, unique = true)
    private String usernameHashed; //SHA-256 hashed username

    @Column(nullable = false)
    private String password; //SHA-256 hashed password

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public User() {

    }

    public User(UUID id, String encryptedUsername, String hashedUsername, String password) {
        this.id = id;
        this.usernameEncrypted = encryptedUsername;
        this.usernameHashed = hashedUsername;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getter and Setter for plaintext username

    public UUID getId() {
        return id;
    }

    public void setId(UUID id){
        this.id = id;
    }

    public String getUsernameEncrypted() {
        return usernameEncrypted;
    }

    public void setUsernameEncrypted(String usernameEncrypted) {
        this.usernameEncrypted = usernameEncrypted;
    }

    public String getUsernameHashed() {
        return usernameHashed;
    }

    public void setUsernameHashed(String usernameHashed) {
        this.usernameHashed = usernameHashed;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt(){
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt){
        this.deletedAt = deletedAt;
    }
}
