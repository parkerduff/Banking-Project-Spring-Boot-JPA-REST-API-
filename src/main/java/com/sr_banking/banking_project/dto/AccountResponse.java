package com.sr_banking.banking_project.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.sr_banking.banking_project.model.BankAccount;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Outbound representation of an account; decouples the API contract from the JPA entity. */
@JsonFilter("fieldFilter")
public record AccountResponse(
        String accountNumber,
        String accountHolderName,
        String accountType,
        BigDecimal balance,
        LocalDateTime createdAt) {

    public static AccountResponse from(BankAccount account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getAccountHolderName(),
                account.getAccountType(),
                account.getBalance(),
                account.getCreatedAt());
    }
}
