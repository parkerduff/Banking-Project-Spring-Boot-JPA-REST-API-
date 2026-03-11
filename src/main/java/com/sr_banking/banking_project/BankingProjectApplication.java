package com.sr_banking.banking_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankingProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingProjectApplication.class, args);
        System.out.println("✅ Banking Application Started Successfully!");
        System.out.println("📍 Server running on: http://localhost:8080");
        System.out.println("📊 H2 Console: http://localhost:8080/h2-console");
    }
}