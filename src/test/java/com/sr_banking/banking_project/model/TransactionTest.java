package com.sr_banking.banking_project.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transaction Entity")
class TransactionTest {

    @Test
    @DisplayName("default constructor initializes transactionDate")
    void defaultConstructorInitializesTransactionDate() {
        Transaction transaction = new Transaction();

        assertThat(transaction.getTransactionDate()).isNotNull();
    }

    @Test
    @DisplayName("parameterized constructor sets all fields correctly")
    void parameterizedConstructorSetsAllFields() {
        BankAccount account = new BankAccount();
        account.setAccountNumber("ACC1001");

        Transaction transaction = new Transaction("DEPOSIT", new BigDecimal("1000.00"), "Cash deposit", account);

        assertThat(transaction.getTransactionType()).isEqualTo("DEPOSIT");
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(transaction.getDescription()).isEqualTo("Cash deposit");
        assertThat(transaction.getBankAccount()).isEqualTo(account);
        assertThat(transaction.getTransactionId()).startsWith("TXN");
        assertThat(transaction.getTransactionDate()).isNotNull();
    }

    @Test
    @DisplayName("all getters and setters work correctly")
    void allGettersAndSettersWork() {
        Transaction transaction = new Transaction();
        BankAccount account = new BankAccount();
        account.setAccountNumber("ACC2001");
        LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 30, 0);

        transaction.setId(42L);
        transaction.setTransactionId("TXN12345");
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setDescription("ATM withdrawal");
        transaction.setTransactionDate(now);
        transaction.setBankAccount(account);

        assertThat(transaction.getId()).isEqualTo(42L);
        assertThat(transaction.getTransactionId()).isEqualTo("TXN12345");
        assertThat(transaction.getTransactionType()).isEqualTo("WITHDRAWAL");
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(transaction.getDescription()).isEqualTo("ATM withdrawal");
        assertThat(transaction.getTransactionDate()).isEqualTo(now);
        assertThat(transaction.getBankAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("toString returns expected format")
    void toStringReturnsExpectedFormat() {
        Transaction transaction = new Transaction();
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(new BigDecimal("1234.56"));
        transaction.setDescription("Test deposit");
        transaction.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0));

        String result = transaction.toString();

        assertThat(result).contains("DEPOSIT");
        assertThat(result).contains("1234.56");
        assertThat(result).contains("Test deposit");
        assertThat(result).contains("2024-01-15");
    }
}
