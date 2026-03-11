package com.sr_banking.banking_project.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    @DisplayName("default constructor sets transactionDate")
    void defaultConstructor() {
        Transaction txn = new Transaction();

        assertThat(txn.getTransactionDate()).isNotNull();
        assertThat(txn.getId()).isNull();
        assertThat(txn.getTransactionId()).isNull();
        assertThat(txn.getTransactionType()).isNull();
        assertThat(txn.getAmount()).isNull();
        assertThat(txn.getDescription()).isNull();
        assertThat(txn.getBankAccount()).isNull();
    }

    @Test
    @DisplayName("parameterized constructor sets all fields")
    void parameterizedConstructor() {
        BankAccount account = new BankAccount("Alice", "SAVINGS", new BigDecimal("1000"));
        Transaction txn = new Transaction("DEPOSIT", new BigDecimal("500"), "Test deposit", account);

        assertThat(txn.getTransactionType()).isEqualTo("DEPOSIT");
        assertThat(txn.getAmount()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(txn.getDescription()).isEqualTo("Test deposit");
        assertThat(txn.getBankAccount()).isEqualTo(account);
        assertThat(txn.getTransactionDate()).isNotNull();
        assertThat(txn.getTransactionId()).startsWith("TXN");
    }

    @Test
    @DisplayName("setters and getters work correctly")
    void settersAndGetters() {
        Transaction txn = new Transaction();
        BankAccount account = new BankAccount("Bob", "CHECKING", new BigDecimal("2000"));
        LocalDateTime now = LocalDateTime.now();

        txn.setId(10L);
        txn.setTransactionId("TXN999");
        txn.setTransactionType("WITHDRAWAL");
        txn.setAmount(new BigDecimal("250"));
        txn.setDescription("ATM withdrawal");
        txn.setTransactionDate(now);
        txn.setBankAccount(account);

        assertThat(txn.getId()).isEqualTo(10L);
        assertThat(txn.getTransactionId()).isEqualTo("TXN999");
        assertThat(txn.getTransactionType()).isEqualTo("WITHDRAWAL");
        assertThat(txn.getAmount()).isEqualByComparingTo(new BigDecimal("250"));
        assertThat(txn.getDescription()).isEqualTo("ATM withdrawal");
        assertThat(txn.getTransactionDate()).isEqualTo(now);
        assertThat(txn.getBankAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("toString returns formatted string")
    void toStringFormat() {
        BankAccount account = new BankAccount("Alice", "SAVINGS", new BigDecimal("1000"));
        Transaction txn = new Transaction("DEPOSIT", new BigDecimal("500"), "Cash deposit", account);

        String str = txn.toString();

        assertThat(str).contains("DEPOSIT");
        assertThat(str).contains("500");
        assertThat(str).contains("Cash deposit");
    }
}
