package com.sr_banking.banking_project.dto;

import com.sr_banking.banking_project.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Outward-facing transaction representation. */
public class TransactionResponse {

    private final Long id;
    private final String transactionId;
    private final String transactionType;
    private final BigDecimal amount;
    private final String currency;
    private final String description;
    private final LocalDateTime transactionDate;
    private final String accountNumber;

    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.transactionId = transaction.getTransactionId();
        this.transactionType = transaction.getTransactionType();
        this.amount = transaction.getAmount();
        this.currency = transaction.getBankAccount() != null
                ? transaction.getBankAccount().getCurrency() : null;
        this.description = transaction.getDescription();
        this.transactionDate = transaction.getTransactionDate();
        this.accountNumber = transaction.getBankAccount() != null
                ? transaction.getBankAccount().getAccountNumber() : null;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
