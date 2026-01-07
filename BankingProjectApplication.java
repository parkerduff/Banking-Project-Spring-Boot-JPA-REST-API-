package com.sr_banking.banking_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class BankingProjectApplication {

    private static final Logger logger = LoggerFactory.getLogger(BankingProjectApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Banking Application with OpenTelemetry instrumentation...");

        SpringApplication.run(BankingProjectApplication.class, args);

        logger.info("Banking Application Started Successfully!");
        logger.info("Server running on: http://localhost:8080");
        logger.info("H2 Console: http://localhost:8080/h2-console");
        logger.info("Actuator endpoints: http://localhost:8080/actuator");
        logger.info("OpenTelemetry tracing is enabled - traces will be exported to configured OTLP endpoint");
    }
}
