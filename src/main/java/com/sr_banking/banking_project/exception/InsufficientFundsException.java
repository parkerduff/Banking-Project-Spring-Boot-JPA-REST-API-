package com.sr_banking.banking_project.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountNumber) {
        super("Insufficient balance in account: " + accountNumber);
    }
}
