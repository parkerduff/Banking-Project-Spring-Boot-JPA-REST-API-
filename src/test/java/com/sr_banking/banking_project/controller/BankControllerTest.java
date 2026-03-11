package com.sr_banking.banking_project.controller;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.service.BankService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankController.class)
class BankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankService bankService;

    // ---- Helper factories ----

    private BankAccount buildAccount(String name, String type, BigDecimal balance) {
        BankAccount account = new BankAccount(name, type, balance);
        account.setId(1L);
        return account;
    }

    private Transaction buildTransaction(String txnType, BigDecimal amount, String desc, BankAccount account) {
        Transaction txn = new Transaction(txnType, amount, desc, account);
        txn.setId(1L);
        return txn;
    }

    // =========================================================================
    // POST /api/bank/accounts  (createAccount)
    // =========================================================================

    @Nested
    @DisplayName("POST /api/bank/accounts - createAccount")
    class CreateAccountTests {

        @Test
        @DisplayName("returns 200 and account on successful creation")
        void createAccount_happyPath() throws Exception {
            BankAccount saved = buildAccount("Alice", "SAVINGS", new BigDecimal("1000.00"));
            when(bankService.createAccount(eq("Alice"), eq("SAVINGS"), eq(new BigDecimal("1000.00"))))
                    .thenReturn(saved);

            mockMvc.perform(post("/api/bank/accounts")
                            .param("accountHolderName", "Alice")
                            .param("accountType", "SAVINGS")
                            .param("initialDeposit", "1000.00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountHolderName").value("Alice"))
                    .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                    .andExpect(jsonPath("$.balance").value(1000.00));

            verify(bankService, times(1)).createAccount("Alice", "SAVINGS", new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("returns 400 when service throws exception")
        void createAccount_serviceThrows() throws Exception {
            when(bankService.createAccount(any(), any(), any()))
                    .thenThrow(new RuntimeException("Invalid deposit amount"));

            mockMvc.perform(post("/api/bank/accounts")
                            .param("accountHolderName", "Bob")
                            .param("accountType", "CHECKING")
                            .param("initialDeposit", "-100"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error creating account: Invalid deposit amount")));
        }

        @Test
        @DisplayName("returns 400 when required param accountHolderName is missing")
        void createAccount_missingName() throws Exception {
            mockMvc.perform(post("/api/bank/accounts")
                            .param("accountType", "SAVINGS")
                            .param("initialDeposit", "500"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when required param accountType is missing")
        void createAccount_missingType() throws Exception {
            mockMvc.perform(post("/api/bank/accounts")
                            .param("accountHolderName", "Alice")
                            .param("initialDeposit", "500"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when required param initialDeposit is missing")
        void createAccount_missingDeposit() throws Exception {
            mockMvc.perform(post("/api/bank/accounts")
                            .param("accountHolderName", "Alice")
                            .param("accountType", "SAVINGS"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 200 with zero initial deposit")
        void createAccount_zeroDeposit() throws Exception {
            BankAccount saved = buildAccount("Charlie", "CHECKING", BigDecimal.ZERO);
            when(bankService.createAccount(eq("Charlie"), eq("CHECKING"), eq(BigDecimal.ZERO)))
                    .thenReturn(saved);

            mockMvc.perform(post("/api/bank/accounts")
                            .param("accountHolderName", "Charlie")
                            .param("accountType", "CHECKING")
                            .param("initialDeposit", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountHolderName").value("Charlie"));
        }
    }

    // =========================================================================
    // GET /api/bank/accounts  (getAllAccounts)
    // =========================================================================

    @Nested
    @DisplayName("GET /api/bank/accounts - getAllAccounts")
    class GetAllAccountsTests {

        @Test
        @DisplayName("returns 200 with list of accounts")
        void getAllAccounts_happyPath() throws Exception {
            BankAccount a1 = buildAccount("Alice", "SAVINGS", new BigDecimal("1000"));
            BankAccount a2 = buildAccount("Bob", "CHECKING", new BigDecimal("2000"));
            when(bankService.getAllAccounts()).thenReturn(List.of(a1, a2));

            mockMvc.perform(get("/api/bank/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].accountHolderName").value("Alice"))
                    .andExpect(jsonPath("$[1].accountHolderName").value("Bob"));

            verify(bankService, times(1)).getAllAccounts();
        }

        @Test
        @DisplayName("returns 200 with empty list when no accounts exist")
        void getAllAccounts_emptyList() throws Exception {
            when(bankService.getAllAccounts()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/bank/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // =========================================================================
    // GET /api/bank/accounts/{accountNumber}  (getAccount)
    // =========================================================================

    @Nested
    @DisplayName("GET /api/bank/accounts/{accountNumber} - getAccount")
    class GetAccountTests {

        @Test
        @DisplayName("returns 200 and account when found")
        void getAccount_found() throws Exception {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("5000"));
            account.setAccountNumber("ACC123");
            when(bankService.getAccountByNumber("ACC123")).thenReturn(Optional.of(account));

            mockMvc.perform(get("/api/bank/accounts/ACC123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountHolderName").value("Alice"))
                    .andExpect(jsonPath("$.accountNumber").value("ACC123"))
                    .andExpect(jsonPath("$.balance").value(5000));

            verify(bankService, times(1)).getAccountByNumber("ACC123");
        }

        @Test
        @DisplayName("returns 404 when account not found")
        void getAccount_notFound() throws Exception {
            when(bankService.getAccountByNumber("NONEXISTENT")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/bank/accounts/NONEXISTENT"))
                    .andExpect(status().isNotFound());

            verify(bankService, times(1)).getAccountByNumber("NONEXISTENT");
        }
    }

    // =========================================================================
    // POST /api/bank/accounts/deposit  (depositMoney)
    // =========================================================================

    @Nested
    @DisplayName("POST /api/bank/accounts/deposit - depositMoney")
    class DepositTests {

        @Test
        @DisplayName("returns 200 and transaction on successful deposit")
        void deposit_happyPath() throws Exception {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("1500"));
            Transaction txn = buildTransaction("DEPOSIT", new BigDecimal("500"), "Cash deposit", account);
            when(bankService.depositMoney(eq("ACC123"), eq(new BigDecimal("500")), eq("Cash deposit")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/deposit")
                            .param("accountNumber", "ACC123")
                            .param("amount", "500")
                            .param("description", "Cash deposit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                    .andExpect(jsonPath("$.amount").value(500));

            verify(bankService, times(1)).depositMoney("ACC123", new BigDecimal("500"), "Cash deposit");
        }

        @Test
        @DisplayName("uses default description when not provided")
        void deposit_defaultDescription() throws Exception {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("1500"));
            Transaction txn = buildTransaction("DEPOSIT", new BigDecimal("200"), "Cash deposit", account);
            when(bankService.depositMoney(eq("ACC123"), eq(new BigDecimal("200")), eq("Cash deposit")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/deposit")
                            .param("accountNumber", "ACC123")
                            .param("amount", "200"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));

            verify(bankService, times(1)).depositMoney("ACC123", new BigDecimal("200"), "Cash deposit");
        }

        @Test
        @DisplayName("returns 400 when account not found")
        void deposit_accountNotFound() throws Exception {
            when(bankService.depositMoney(any(), any(), any()))
                    .thenThrow(new RuntimeException("Account not found with number: INVALID"));

            mockMvc.perform(post("/api/bank/accounts/deposit")
                            .param("accountNumber", "INVALID")
                            .param("amount", "100"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error during deposit: Account not found")));
        }

        @Test
        @DisplayName("returns 400 when accountNumber param is missing")
        void deposit_missingAccountNumber() throws Exception {
            mockMvc.perform(post("/api/bank/accounts/deposit")
                            .param("amount", "100"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when amount param is missing")
        void deposit_missingAmount() throws Exception {
            mockMvc.perform(post("/api/bank/accounts/deposit")
                            .param("accountNumber", "ACC123"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 200 with custom description")
        void deposit_customDescription() throws Exception {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("1500"));
            Transaction txn = buildTransaction("DEPOSIT", new BigDecimal("1000"), "Payroll deposit", account);
            when(bankService.depositMoney(eq("ACC123"), eq(new BigDecimal("1000")), eq("Payroll deposit")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/deposit")
                            .param("accountNumber", "ACC123")
                            .param("amount", "1000")
                            .param("description", "Payroll deposit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Payroll deposit"));
        }
    }

    // =========================================================================
    // POST /api/bank/accounts/withdraw  (withdrawMoney)
    // =========================================================================

    @Nested
    @DisplayName("POST /api/bank/accounts/withdraw - withdrawMoney")
    class WithdrawTests {

        @Test
        @DisplayName("returns 200 and transaction on successful withdrawal")
        void withdraw_happyPath() throws Exception {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("800"));
            Transaction txn = buildTransaction("WITHDRAWAL", new BigDecimal("200"), "Cash withdrawal", account);
            when(bankService.withdrawMoney(eq("ACC123"), eq(new BigDecimal("200")), eq("Cash withdrawal")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/withdraw")
                            .param("accountNumber", "ACC123")
                            .param("amount", "200")
                            .param("description", "Cash withdrawal"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"))
                    .andExpect(jsonPath("$.amount").value(200));

            verify(bankService, times(1)).withdrawMoney("ACC123", new BigDecimal("200"), "Cash withdrawal");
        }

        @Test
        @DisplayName("uses default description when not provided")
        void withdraw_defaultDescription() throws Exception {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("800"));
            Transaction txn = buildTransaction("WITHDRAWAL", new BigDecimal("100"), "Cash withdrawal", account);
            when(bankService.withdrawMoney(eq("ACC123"), eq(new BigDecimal("100")), eq("Cash withdrawal")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/withdraw")
                            .param("accountNumber", "ACC123")
                            .param("amount", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"));

            verify(bankService, times(1)).withdrawMoney("ACC123", new BigDecimal("100"), "Cash withdrawal");
        }

        @Test
        @DisplayName("returns 400 when insufficient balance")
        void withdraw_insufficientBalance() throws Exception {
            when(bankService.withdrawMoney(any(), any(), any()))
                    .thenThrow(new RuntimeException("Insufficient balance in account: ACC123"));

            mockMvc.perform(post("/api/bank/accounts/withdraw")
                            .param("accountNumber", "ACC123")
                            .param("amount", "99999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error during withdrawal: Insufficient balance")));
        }

        @Test
        @DisplayName("returns 400 when account not found")
        void withdraw_accountNotFound() throws Exception {
            when(bankService.withdrawMoney(any(), any(), any()))
                    .thenThrow(new RuntimeException("Account not found with number: INVALID"));

            mockMvc.perform(post("/api/bank/accounts/withdraw")
                            .param("accountNumber", "INVALID")
                            .param("amount", "50"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error during withdrawal: Account not found")));
        }

        @Test
        @DisplayName("returns 400 when accountNumber param is missing")
        void withdraw_missingAccountNumber() throws Exception {
            mockMvc.perform(post("/api/bank/accounts/withdraw")
                            .param("amount", "100"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when amount param is missing")
        void withdraw_missingAmount() throws Exception {
            mockMvc.perform(post("/api/bank/accounts/withdraw")
                            .param("accountNumber", "ACC123"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 200 with custom description")
        void withdraw_customDescription() throws Exception {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("800"));
            Transaction txn = buildTransaction("WITHDRAWAL", new BigDecimal("50"), "ATM withdrawal", account);
            when(bankService.withdrawMoney(eq("ACC123"), eq(new BigDecimal("50")), eq("ATM withdrawal")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/withdraw")
                            .param("accountNumber", "ACC123")
                            .param("amount", "50")
                            .param("description", "ATM withdrawal"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("ATM withdrawal"));
        }
    }

    // =========================================================================
    // POST /api/bank/accounts/transfer  (transferMoney)
    // =========================================================================

    @Nested
    @DisplayName("POST /api/bank/accounts/transfer - transferMoney")
    class TransferTests {

        @Test
        @DisplayName("returns 200 and transaction on successful transfer")
        void transfer_happyPath() throws Exception {
            BankAccount from = buildAccount("Alice", "SAVINGS", new BigDecimal("800"));
            Transaction txn = buildTransaction("WITHDRAWAL", new BigDecimal("300"), "Fund transfer", from);
            when(bankService.transferMoney(eq("ACC001"), eq("ACC002"), eq(new BigDecimal("300")), eq("Fund transfer")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "ACC001")
                            .param("toAccount", "ACC002")
                            .param("amount", "300")
                            .param("description", "Fund transfer"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"))
                    .andExpect(jsonPath("$.amount").value(300));

            verify(bankService, times(1)).transferMoney("ACC001", "ACC002", new BigDecimal("300"), "Fund transfer");
        }

        @Test
        @DisplayName("uses default description when not provided")
        void transfer_defaultDescription() throws Exception {
            BankAccount from = buildAccount("Alice", "SAVINGS", new BigDecimal("800"));
            Transaction txn = buildTransaction("WITHDRAWAL", new BigDecimal("100"), "Fund transfer", from);
            when(bankService.transferMoney(eq("ACC001"), eq("ACC002"), eq(new BigDecimal("100")), eq("Fund transfer")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "ACC001")
                            .param("toAccount", "ACC002")
                            .param("amount", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"));

            verify(bankService, times(1)).transferMoney("ACC001", "ACC002", new BigDecimal("100"), "Fund transfer");
        }

        @Test
        @DisplayName("returns 400 when source account not found")
        void transfer_sourceNotFound() throws Exception {
            when(bankService.transferMoney(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Account not found with number: INVALID"));

            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "INVALID")
                            .param("toAccount", "ACC002")
                            .param("amount", "100"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error during transfer: Account not found")));
        }

        @Test
        @DisplayName("returns 400 when destination account not found")
        void transfer_destinationNotFound() throws Exception {
            when(bankService.transferMoney(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Account not found with number: INVALID_DEST"));

            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "ACC001")
                            .param("toAccount", "INVALID_DEST")
                            .param("amount", "100"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error during transfer")));
        }

        @Test
        @DisplayName("returns 400 when insufficient balance for transfer")
        void transfer_insufficientBalance() throws Exception {
            when(bankService.transferMoney(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Insufficient balance in account: ACC001"));

            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "ACC001")
                            .param("toAccount", "ACC002")
                            .param("amount", "999999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error during transfer: Insufficient balance")));
        }

        @Test
        @DisplayName("returns 400 when fromAccount param is missing")
        void transfer_missingFromAccount() throws Exception {
            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("toAccount", "ACC002")
                            .param("amount", "100"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when toAccount param is missing")
        void transfer_missingToAccount() throws Exception {
            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "ACC001")
                            .param("amount", "100"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when amount param is missing")
        void transfer_missingAmount() throws Exception {
            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "ACC001")
                            .param("toAccount", "ACC002"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 200 with custom description")
        void transfer_customDescription() throws Exception {
            BankAccount from = buildAccount("Alice", "SAVINGS", new BigDecimal("5000"));
            Transaction txn = buildTransaction("WITHDRAWAL", new BigDecimal("500"), "Rent payment", from);
            when(bankService.transferMoney(eq("ACC001"), eq("ACC002"), eq(new BigDecimal("500")), eq("Rent payment")))
                    .thenReturn(txn);

            mockMvc.perform(post("/api/bank/accounts/transfer")
                            .param("fromAccount", "ACC001")
                            .param("toAccount", "ACC002")
                            .param("amount", "500")
                            .param("description", "Rent payment"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.amount").value(500));
        }
    }

    // =========================================================================
    // GET /api/bank/accounts/statement/{accountNumber}  (getAccountStatement)
    // =========================================================================

    @Nested
    @DisplayName("GET /api/bank/accounts/statement/{accountNumber} - getAccountStatement")
    class StatementTests {

        @Test
        @DisplayName("returns 200 with list of transactions")
        void getStatement_happyPath() throws Exception {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("5000"));
            Transaction t1 = buildTransaction("DEPOSIT", new BigDecimal("1000"), "Initial", account);
            Transaction t2 = buildTransaction("WITHDRAWAL", new BigDecimal("200"), "ATM", account);
            when(bankService.getAccountStatement("ACC123")).thenReturn(List.of(t1, t2));

            mockMvc.perform(get("/api/bank/accounts/statement/ACC123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].transactionType").value("DEPOSIT"))
                    .andExpect(jsonPath("$[1].transactionType").value("WITHDRAWAL"));

            verify(bankService, times(1)).getAccountStatement("ACC123");
        }

        @Test
        @DisplayName("returns 200 with empty list when no transactions")
        void getStatement_emptyList() throws Exception {
            when(bankService.getAccountStatement("ACC123")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/bank/accounts/statement/ACC123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("returns 400 when service throws exception")
        void getStatement_serviceThrows() throws Exception {
            when(bankService.getAccountStatement(any()))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/api/bank/accounts/statement/ACC_BAD"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Error fetching statement: Database error")));
        }
    }
}
