package com.sr_banking.banking_project.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private com.sr_banking.banking_project.model.BankAccount bankAccount;

    // Default Constructor
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
    }

    // Parameterized Constructor
    public Transaction(String transactionType, BigDecimal amount, String description, com.sr_banking.banking_project.model.BankAccount bankAccount) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.bankAccount = bankAccount;
        this.transactionDate = LocalDateTime.now();
        this.transactionId = "TXN" + System.currentTimeMillis();
    }

    // Getter Methods
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

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public com.sr_banking.banking_project.model.BankAccount getBankAccount() {
        return bankAccount;
    }

    // Setter Methods
    public void setId(Long id) {
        this.id = id;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setBankAccount(com.sr_banking.banking_project.model.BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Override
    public String toString() {
        return transactionType + " | Amount: ₹" + amount + " | Desc: " + description + " | Date: " + transactionDate;
    }
}