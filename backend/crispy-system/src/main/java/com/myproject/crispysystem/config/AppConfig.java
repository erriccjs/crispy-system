package com.myproject.crispysystem.config;

import com.myproject.crispysystem.common.util.EncryptionUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${encryption.secret}")
    private String secretKey;

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("The encryption.secretKey property is not set or is empty.");
        }
        System.out.println("PostConstruct secretKey: " + secretKey);
    }

    @Bean
    public EncryptionUtil encryptionUtil() {
        return new EncryptionUtil("ZuW0XhTAJKIq/YyhphrMCJJvag6fF3ykrZ/0X0ZrsCM=");
    }
}

