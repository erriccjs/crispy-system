package com.myproject.crispysystem.users.model;

import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.common.util.HashUtil;
import com.myproject.crispysystem.common.util.LoggerUtil;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
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

    @Transient
    private String username;

    @Transient
    private EncryptionUtil encryptionUtil;//Plaintext username, not stored in the database

    public User() {

    }
    public User(EncryptionUtil encryptionUtil) {
        if (encryptionUtil == null){
            LoggerUtil.logWarn("EncryptionUtil cannot be null.");
        }
        this.encryptionUtil = encryptionUtil;
    }

    public User(UUID id, String username, String password, EncryptionUtil encryptionUtil) {
        this(encryptionUtil);
        this.id = id;
        setUsername(username);
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
    public String getUsername(){
        if (this.usernameEncrypted == null || this.usernameEncrypted.isEmpty()) {
            LoggerUtil.logWarn("Attempted to decrypt a null or empty encrypted username.");
            throw new IllegalStateException("Encrypted username cannot be null or empty.");
        }
        try {
            return encryptionUtil.decrypt(this.usernameEncrypted);
        } catch (Exception e) {
            LoggerUtil.logError("Error decrypting username", e);
            throw new RuntimeException("Failed to decrypt username.", e);
        }
    }

    public void setUsername(String username){
        if (username == null || username.isEmpty()) {
            LoggerUtil.logWarn("Attempted to set a null or empty username.");
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        try {
            this.username = username;
            this.usernameEncrypted = encryptionUtil.encrypt(username);
            this.usernameHashed = HashUtil.sha256(username);
            LoggerUtil.logInfo("Successfully set encrypted and hashed username.");
        } catch (Exception e) {
            LoggerUtil.logError("Error encrypting or hashing username", e);
            throw new RuntimeException("Failed to encrypt or hash username.", e);
        }
    }

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
