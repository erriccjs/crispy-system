package com.myproject.simpleatmcli;

import com.myproject.simpleatmcli.service.AtmService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimpleAtmCliApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SimpleAtmCliApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        AtmService atmService = new AtmService();
        atmService.start();
    }
}
