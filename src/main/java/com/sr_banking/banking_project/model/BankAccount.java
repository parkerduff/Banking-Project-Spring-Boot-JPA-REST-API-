package com.sr_banking.banking_project.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String accountNumber;

    private String accountHolderName;
    private String accountType;
    private BigDecimal balance;

    // ISO 4217 currency code (e.g. SGD, USD)
    private String currency;

    private LocalDateTime createdAt;

    // Default Constructor
    public BankAccount() {
        this.balance = BigDecimal.ZERO;
        this.currency = "SGD";
        this.createdAt = LocalDateTime.now();
    }

    // Parameterized Constructor
    public BankAccount(String accountHolderName, String accountType, BigDecimal balance, String currency) {
        this.accountHolderName = accountHolderName;
        this.accountType = accountType;
        this.balance = balance;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
        this.accountNumber = "ACC" + System.currentTimeMillis();
    }

    // Getter Methods
    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setter Methods
    public void setId(Long id) {
        this.id = id;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Account: " + accountNumber + " | Holder: " + accountHolderName
                + " | Balance: " + balance + " " + currency;
    }
}