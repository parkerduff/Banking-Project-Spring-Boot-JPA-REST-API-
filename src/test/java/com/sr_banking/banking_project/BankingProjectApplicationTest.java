package com.sr_banking.banking_project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false"
})
@DisplayName("BankingProjectApplication")
class BankingProjectApplicationTest {

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
        // Verifies that the Spring application context loads without errors
    }

    @Test
    @DisplayName("main method runs without exception")
    void mainMethodRuns() {
        BankingProjectApplication.main(new String[]{
            "--spring.sql.init.mode=never",
            "--spring.jpa.defer-datasource-initialization=false"
        });
    }
}
