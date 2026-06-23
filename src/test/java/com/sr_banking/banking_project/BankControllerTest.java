package com.sr_banking.banking_project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAccount_returns201_andCurrency() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountHolderName\":\"Test User\",\"accountType\":\"SAVINGS\",\"initialDeposit\":100.00,\"currency\":\"SGD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").exists())
                .andExpect(jsonPath("$.currency").value("SGD"));
    }

    @Test
    void createAccount_withInvalidType_returns400_withFieldError() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountHolderName\":\"Test User\",\"accountType\":\"GOLD\",\"initialDeposit\":100.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.accountType").exists());
    }

    @Test
    void deposit_withNonPositiveAmount_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/ACC1001/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    void withdraw_withInsufficientBalance_returns422() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/ACC1001/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":99999999.00}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void getAccount_unknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/DOES_NOT_EXIST"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deposit_thenStatement_reflectsTransaction() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/ACC1002/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":250.00,\"description\":\"unit test deposit\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.currency").value("SGD"));

        mockMvc.perform(get("/api/v1/accounts/ACC1002/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("ACC1002"));
    }
}
