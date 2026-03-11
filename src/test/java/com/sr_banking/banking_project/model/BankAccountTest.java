package com.sr_banking.banking_project.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BankAccountTest {

    @Test
    @DisplayName("default constructor sets zero balance and createdAt")
    void defaultConstructor() {
        BankAccount account = new BankAccount();

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getId()).isNull();
        assertThat(account.getAccountNumber()).isNull();
        assertThat(account.getAccountHolderName()).isNull();
        assertThat(account.getAccountType()).isNull();
    }

    @Test
    @DisplayName("parameterized constructor sets all fields")
    void parameterizedConstructor() {
        BankAccount account = new BankAccount("Alice", "SAVINGS", new BigDecimal("1000"));

        assertThat(account.getAccountHolderName()).isEqualTo("Alice");
        assertThat(account.getAccountType()).isEqualTo("SAVINGS");
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getAccountNumber()).startsWith("ACC");
    }

    @Test
    @DisplayName("setters and getters work correctly")
    void settersAndGetters() {
        BankAccount account = new BankAccount();
        LocalDateTime now = LocalDateTime.now();

        account.setId(42L);
        account.setAccountNumber("ACC999");
        account.setAccountHolderName("Bob");
        account.setAccountType("CHECKING");
        account.setBalance(new BigDecimal("5000"));
        account.setCreatedAt(now);

        assertThat(account.getId()).isEqualTo(42L);
        assertThat(account.getAccountNumber()).isEqualTo("ACC999");
        assertThat(account.getAccountHolderName()).isEqualTo("Bob");
        assertThat(account.getAccountType()).isEqualTo("CHECKING");
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(account.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toString returns formatted string")
    void toStringFormat() {
        BankAccount account = new BankAccount("Alice", "SAVINGS", new BigDecimal("1000"));
        account.setAccountNumber("ACC123");

        String str = account.toString();

        assertThat(str).contains("ACC123");
        assertThat(str).contains("Alice");
        assertThat(str).contains("1000");
    }
}
