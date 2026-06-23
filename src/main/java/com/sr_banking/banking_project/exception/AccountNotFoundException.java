package com.sr_banking.banking_project.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountNumber) {
        super("Account not found with number: " + accountNumber);
    }
}
