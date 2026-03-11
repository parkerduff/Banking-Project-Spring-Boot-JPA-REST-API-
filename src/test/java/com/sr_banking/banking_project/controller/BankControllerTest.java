package com.sr_banking.banking_project.controller;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.service.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankController.class)
@DisplayName("BankController")
class BankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankService bankService;

    private BankAccount testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new BankAccount();
        testAccount.setId(1L);
        testAccount.setAccountNumber("ACC1001");
        testAccount.setAccountHolderName("Rahul Sharma");
        testAccount.setAccountType("SAVINGS");
        testAccount.setBalance(new BigDecimal("5000.00"));
    }

    @Nested
    @DisplayName("POST /api/bank/accounts")
    class CreateAccount {

        @Test
        @DisplayName("creates account successfully")
        void createsAccountSuccessfully() throws Exception {
            when(bankService.createAccount(eq("Test User"), eq("SAVINGS"), any(BigDecimal.class)))
                    .thenReturn(testAccount);

            mockMvc.perform(post("/api/bank/accounts")
                            .param("accountHolderName", "Test User")
                            .param("accountType", "SAVINGS")
                            .param("initialDeposit", "5000.00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNumber", is("ACC1001")));
        }

        @Test
        @DisplayName("returns bad request on error")
        void returnsBadRequestOnError() throws Exception {
            when(bankService.createAccount(anyString(), anyString(), any(BigDecimal.class)))
                    .thenThrow(new RuntimeException("Creation error"));

            mockMvc.perform(post("/api/bank/accounts")
                            .param("accountHolderName", "Test User")
                            .param("accountType", "SAVINGS")
                            .param("initialDeposit", "5000.00"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error creating account")));
        }
    }

    @Nested
    @DisplayName("GET /api/bank/accounts")
    class GetAllAccounts {

        @Test
        @DisplayName("returns all accounts")
        void returnsAllAccounts() throws Exception {
            when(bankService.getAllAccounts()).thenReturn(Arrays.asList(testAccount));

            mockMvc.perform(get("/api/bank/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].accountNumber", is("ACC1001")));
        }

        @Test
        @DisplayName("returns empty list when no accounts")
        void returnsEmptyList() throws Exception {
            when(bankService.getAllAccounts()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/bank/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/bank/accounts/{accountNumber}")
    class GetAccount {

        @Test
        @DisplayName("returns account when found")
        void returnsAccountWhenFound() throws Exception {
            when(bankService.getAccountByNumber("ACC1001")).thenReturn(Optional.of(testAccount));

            mockMvc.perform(get("/api/bank/accounts/ACC1001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNumber", is("ACC1001")))
                    .andExpect(jsonPath("$.accountHolderName", is("Rahul Sharma")));
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() throws Exception {
            when(bankService.getAccountByNumber("ACC9999")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/bank/accounts/ACC9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/bank/accounts/deposit")
    class DepositMoney {

        @Test
        @DisplayName("deposits money successfully")
        void depositsMoneySuccessfully() throws Exception {
            Transaction txn = new Transaction("DEPOSIT", new BigDecimal("1000.00"), "Cash deposit", testAccount);
            when(bankService.depositMoney(eq("ACC1001"), any(BigDecimal.class), eq("Cash deposit")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/deposit")
                            .param("accountNumber", "ACC1001")
                            .param("amount", "1000.00")
                            .param("description", "Cash deposit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType", is("DEPOSIT")));
        }

        @Test
        @DisplayName("returns bad request on deposit error")
        void returnsBadRequestOnError() throws Exception {
            when(bankService.depositMoney(anyString(), any(BigDecimal.class), anyString()))
                    .thenThrow(new RuntimeException("Account not found"));

            mockMvc.perform(post("/api/bank/accounts/deposit")
                            .param("accountNumber", "ACC9999")
                            .param("amount", "1000.00")
                            .param("description", "Cash deposit"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error during deposit")));
        }
    }

    @Nested
    @DisplayName("POST /api/bank/accounts/withdraw")
    class WithdrawMoney {

        @Test
        @DisplayName("withdraws money successfully")
        void withdrawsMoneySuccessfully() throws Exception {
            Transaction txn = new Transaction("WITHDRAWAL", new BigDecimal("500.00"), "Cash withdrawal", testAccount);
            when(bankService.withdrawMoney(eq("ACC1001"), any(BigDecimal.class), eq("Cash withdrawal")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/withdraw")
                            .param("accountNumber", "ACC1001")
                            .param("amount", "500.00")
                            .param("description", "Cash withdrawal"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType", is("WITHDRAWAL")));
        }

        @Test
        @DisplayName("returns bad request on withdrawal error")
        void returnsBadRequestOnError() throws Exception {
            when(bankService.withdrawMoney(anyString(), any(BigDecimal.class), anyString()))
                    .thenThrow(new RuntimeException("Insufficient balance"));

            mockMvc.perform(post("/api/bank/accounts/withdraw")
                            .param("accountNumber", "ACC1001")
                            .param("amount", "100000.00")
                            .param("description", "Cash withdrawal"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error during withdrawal")));
        }
    }

    @Nested
    @DisplayName("POST /api/bank/accounts/transfer")
    class TransferMoney {

        @Test
        @DisplayName("transfers money successfully")
        void transfersMoneySuccessfully() throws Exception {
            Transaction txn = new Transaction("WITHDRAWAL", new BigDecimal("2000.00"), "Transfer", testAccount);
            when(bankService.transferMoney(eq("ACC1001"), eq("ACC1002"), any(BigDecimal.class), eq("Fund transfer")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "ACC1001")
                            .param("toAccount", "ACC1002")
                            .param("amount", "2000.00")
                            .param("description", "Fund transfer"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType", is("WITHDRAWAL")));
        }

        @Test
        @DisplayName("returns bad request on transfer error")
        void returnsBadRequestOnError() throws Exception {
            when(bankService.transferMoney(anyString(), anyString(), any(BigDecimal.class), anyString()))
                    .thenThrow(new RuntimeException("Transfer failed"));

            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "ACC1001")
                            .param("toAccount", "ACC9999")
                            .param("amount", "2000.00")
                            .param("description", "Fund transfer"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error during transfer")));
        }
    }

    @Nested
    @DisplayName("GET /api/bank/accounts/statement/{accountNumber}")
    class GetAccountStatement {

        @Test
        @DisplayName("returns statement successfully")
        void returnsStatementSuccessfully() throws Exception {
            Transaction txn1 = new Transaction("DEPOSIT", new BigDecimal("1000.00"), "Deposit", testAccount);
            Transaction txn2 = new Transaction("WITHDRAWAL", new BigDecimal("500.00"), "Withdrawal", testAccount);
            when(bankService.getAccountStatement("ACC1001")).thenReturn(Arrays.asList(txn1, txn2));

            mockMvc.perform(get("/api/bank/accounts/statement/ACC1001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("returns bad request on statement error")
        void returnsBadRequestOnError() throws Exception {
            when(bankService.getAccountStatement(anyString()))
                    .thenThrow(new RuntimeException("Statement error"));

            mockMvc.perform(get("/api/bank/accounts/statement/ACC9999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error fetching statement")));
        }
    }
}
