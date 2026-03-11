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
class BankingProjectApplicationTest {

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
        // Verifies Spring context loads without errors
    }

    @Test
    @DisplayName("main method runs without error")
    void mainMethodRuns() {
        BankingProjectApplication.main(new String[]{
                "--spring.sql.init.mode=never",
                "--spring.jpa.defer-datasource-initialization=false"
        });
    }
}
