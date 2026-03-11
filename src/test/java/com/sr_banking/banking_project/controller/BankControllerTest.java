package com.sr_banking.banking_project.controller;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.service.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankControllerTest {

    @Mock
    private BankService bankService;

    @InjectMocks
    private BankController bankController;

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
    @DisplayName("createAccount")
    class CreateAccountEndpoint {

        @Test
        @DisplayName("returns 200 with new account on success")
        void returnsOkWithNewAccount() {
            when(bankService.createAccount("Test User", "SAVINGS", new BigDecimal("1000.00")))
                    .thenReturn(testAccount);

            ResponseEntity<?> response = bankController.createAccount("Test User", "SAVINGS", new BigDecimal("1000.00"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(testAccount);
        }

        @Test
        @DisplayName("returns 400 on exception")
        void returnsBadRequestOnException() {
            when(bankService.createAccount(anyString(), anyString(), any(BigDecimal.class)))
                    .thenThrow(new RuntimeException("DB error"));

            ResponseEntity<?> response = bankController.createAccount("Test", "SAVINGS", BigDecimal.TEN);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().toString()).contains("Error creating account");
        }
    }

    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccountsEndpoint {

        @Test
        @DisplayName("returns 200 with all accounts")
        void returnsAllAccounts() {
            when(bankService.getAllAccounts()).thenReturn(List.of(testAccount));

            ResponseEntity<List<BankAccount>> response = bankController.getAllAccounts();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAccount")
    class GetAccountEndpoint {

        @Test
        @DisplayName("returns 200 when account found")
        void returnsOkWhenFound() {
            when(bankService.getAccountByNumber("ACC1001")).thenReturn(Optional.of(testAccount));

            ResponseEntity<?> response = bankController.getAccount("ACC1001");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(testAccount);
        }

        @Test
        @DisplayName("returns 404 when account not found")
        void returnsNotFoundWhenMissing() {
            when(bankService.getAccountByNumber("INVALID")).thenReturn(Optional.empty());

            ResponseEntity<?> response = bankController.getAccount("INVALID");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("depositMoney")
    class DepositMoneyEndpoint {

        @Test
        @DisplayName("returns 200 with transaction on success")
        void returnsOkWithTransaction() {
            Transaction txn = new Transaction("DEPOSIT", new BigDecimal("500.00"), "Cash deposit", testAccount);
            when(bankService.depositMoney("ACC1001", new BigDecimal("500.00"), "Cash deposit"))
                    .thenReturn(txn);

            ResponseEntity<?> response = bankController.depositMoney("ACC1001", new BigDecimal("500.00"), "Cash deposit");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(txn);
        }

        @Test
        @DisplayName("returns 400 on exception")
        void returnsBadRequestOnException() {
            when(bankService.depositMoney(anyString(), any(BigDecimal.class), anyString()))
                    .thenThrow(new RuntimeException("Account not found"));

            ResponseEntity<?> response = bankController.depositMoney("INVALID", BigDecimal.TEN, "Test");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().toString()).contains("Error during deposit");
        }
    }

    @Nested
    @DisplayName("withdrawMoney")
    class WithdrawMoneyEndpoint {

        @Test
        @DisplayName("returns 200 with transaction on success")
        void returnsOkWithTransaction() {
            Transaction txn = new Transaction("WITHDRAWAL", new BigDecimal("200.00"), "ATM withdrawal", testAccount);
            when(bankService.withdrawMoney("ACC1001", new BigDecimal("200.00"), "ATM withdrawal"))
                    .thenReturn(txn);

            ResponseEntity<?> response = bankController.withdrawMoney("ACC1001", new BigDecimal("200.00"), "ATM withdrawal");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(txn);
        }

        @Test
        @DisplayName("returns 400 on exception")
        void returnsBadRequestOnException() {
            when(bankService.withdrawMoney(anyString(), any(BigDecimal.class), anyString()))
                    .thenThrow(new RuntimeException("Insufficient balance"));

            ResponseEntity<?> response = bankController.withdrawMoney("ACC1001", new BigDecimal("99999.00"), "Test");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().toString()).contains("Error during withdrawal");
        }
    }

    @Nested
    @DisplayName("transferMoney")
    class TransferMoneyEndpoint {

        @Test
        @DisplayName("returns 200 with transaction on success")
        void returnsOkWithTransaction() {
            Transaction txn = new Transaction("WITHDRAWAL", new BigDecimal("1000.00"), "Transfer", testAccount);
            when(bankService.transferMoney("ACC1001", "ACC1002", new BigDecimal("1000.00"), "Fund transfer"))
                    .thenReturn(txn);

            ResponseEntity<?> response = bankController.transferMoney("ACC1001", "ACC1002",
                    new BigDecimal("1000.00"), "Fund transfer");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(txn);
        }

        @Test
        @DisplayName("returns 400 on exception")
        void returnsBadRequestOnException() {
            when(bankService.transferMoney(anyString(), anyString(), any(BigDecimal.class), anyString()))
                    .thenThrow(new RuntimeException("Transfer failed"));

            ResponseEntity<?> response = bankController.transferMoney("ACC1001", "INVALID",
                    BigDecimal.TEN, "Test");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().toString()).contains("Error during transfer");
        }
    }

    @Nested
    @DisplayName("getAccountStatement")
    class GetAccountStatementEndpoint {

        @Test
        @DisplayName("returns 200 with statement on success")
        void returnsOkWithStatement() {
            Transaction txn = new Transaction("DEPOSIT", new BigDecimal("1000.00"), "Deposit", testAccount);
            when(bankService.getAccountStatement("ACC1001")).thenReturn(List.of(txn));

            ResponseEntity<?> response = bankController.getAccountStatement("ACC1001");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("returns 400 on exception")
        void returnsBadRequestOnException() {
            when(bankService.getAccountStatement(anyString()))
                    .thenThrow(new RuntimeException("Error fetching"));

            ResponseEntity<?> response = bankController.getAccountStatement("ACC1001");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().toString()).contains("Error fetching statement");
        }
    }
}
