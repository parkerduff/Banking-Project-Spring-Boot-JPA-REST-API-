package com.sr_banking.banking_project.dto;

import com.sr_banking.banking_project.model.BankAccount;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Outward-facing account representation. Decouples the API contract from the JPA entity. */
public class AccountResponse {

    private final Long id;
    private final String accountNumber;
    private final String accountHolderName;
    private final String accountType;
    private final BigDecimal balance;
    private final String currency;
    private final LocalDateTime createdAt;

    public AccountResponse(BankAccount account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.accountHolderName = account.getAccountHolderName();
        this.accountType = account.getAccountType();
        this.balance = account.getBalance();
        this.currency = account.getCurrency();
        this.createdAt = account.getCreatedAt();
    }

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
}
