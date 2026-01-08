package com.sr_banking.banking_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankingProjectApplication {

    private static final Logger logger = LoggerFactory.getLogger(BankingProjectApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BankingProjectApplication.class, args);
        logger.info("Banking Application Started Successfully!");
        logger.info("Server running on: http://localhost:8080");
        logger.info("H2 Console: http://localhost:8080/h2-console");
        logger.info("OpenTelemetry tracing is enabled - traces will be correlated in logs");
        logger.info("Actuator endpoints available at: http://localhost:8080/actuator");
    }
}
