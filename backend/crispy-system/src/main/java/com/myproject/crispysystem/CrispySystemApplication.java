package com.myproject.crispysystem;

import com.myproject.crispysystem.common.util.EncryptionUtil;
import com.myproject.crispysystem.config.AppConfig;
import com.myproject.crispysystem.users.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.security.NoSuchAlgorithmException;

@SpringBootApplication
public class CrispySystemApplication {
	ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

	public static void main(String[] args) throws NoSuchAlgorithmException {
		SpringApplication.run(CrispySystemApplication.class, args);

	}
}
