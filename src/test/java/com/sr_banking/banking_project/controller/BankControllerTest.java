package com.sr_banking.banking_project.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sr_banking.banking_project.dto.CreateAccountRequest;
import com.sr_banking.banking_project.dto.MoneyMovementRequest;
import java.math.BigDecimal;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/bank/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("Jane Doe", "SAVINGS", new BigDecimal("500.00"));
        mockMvc.perform(post("/api/v1/bank/accounts")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber", notNullValue()))
                .andExpect(jsonPath("$.accountHolderName", is("Jane Doe")))
                .andExpect(jsonPath("$.balance", is(500.00)));
    }

    @Test
    void readerRoleCannotCreateAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("Jane Doe", "SAVINGS", new BigDecimal("500.00"));
        mockMvc.perform(post("/api/v1/bank/accounts")
                        .with(user("reader").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidPayloadReturnsStructuredValidationError() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("", "GOLD", new BigDecimal("-1"));
        mockMvc.perform(post("/api/v1/bank/accounts")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors", notNullValue()));
    }

    @Test
    void unknownAccountReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/bank/accounts/ACC_UNKNOWN")
                        .with(user("reader").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void overdraftReturnsUnprocessableEntity() throws Exception {
        MoneyMovementRequest withdrawal = new MoneyMovementRequest(new BigDecimal("999999.00"), "atm");
        mockMvc.perform(post("/api/v1/bank/accounts/ACC1001/withdrawals")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawal)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void malformedAccountNumberReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/bank/accounts/bad!name")
                        .with(user("reader").roles("USER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void fieldProjectionLimitsReturnedProperties() throws Exception {
        mockMvc.perform(get("/api/v1/bank/accounts/ACC1001")
                        .param("fields", "accountNumber")
                        .with(user("reader").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber", is("ACC1001")))
                .andExpect(jsonPath("$.balance").doesNotExist());
    }
}
