package com.sr_banking.banking_project.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.sr_banking.banking_project.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonFilter("fieldFilter")
public record TransactionResponse(
        String transactionId,
        String transactionType,
        BigDecimal amount,
        String description,
        LocalDateTime transactionDate,
        String accountNumber) {

    public static TransactionResponse from(Transaction transaction) {
        String accountNumber = transaction.getBankAccount() != null
                ? transaction.getBankAccount().getAccountNumber()
                : null;
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                accountNumber);
    }
}
