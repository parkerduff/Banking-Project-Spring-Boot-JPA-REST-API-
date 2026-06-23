package com.sr_banking.banking_project.controller;

import com.sr_banking.banking_project.dto.AccountResponse;
import com.sr_banking.banking_project.dto.AmountRequest;
import com.sr_banking.banking_project.dto.CreateAccountRequest;
import com.sr_banking.banking_project.dto.TransactionResponse;
import com.sr_banking.banking_project.dto.TransferRequest;
import com.sr_banking.banking_project.exception.AccountNotFoundException;
import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.service.BankService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BankController {

    @Autowired
    private BankService bankService;

    // Create a new account
    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        String currency = request.getCurrency() != null ? request.getCurrency() : "SGD";
        BankAccount account = bankService.createAccount(
                request.getAccountHolderName(), request.getAccountType(),
                request.getInitialDeposit(), currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AccountResponse(account));
    }

    // List all accounts
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts = bankService.getAllAccounts().stream()
                .map(AccountResponse::new)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    // Get a single account
    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        BankAccount account = bankService.getAccountByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return ResponseEntity.ok(new AccountResponse(account));
    }

    // Deposit into an account (deposits are a sub-resource of the account)
    @PostMapping("/accounts/{accountNumber}/deposits")
    public ResponseEntity<TransactionResponse> deposit(@PathVariable String accountNumber,
                                                       @Valid @RequestBody AmountRequest request) {
        String description = request.getDescription() != null ? request.getDescription() : "Cash deposit";
        Transaction transaction = bankService.depositMoney(accountNumber, request.getAmount(), description);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TransactionResponse(transaction));
    }

    // Withdraw from an account
    @PostMapping("/accounts/{accountNumber}/withdrawals")
    public ResponseEntity<TransactionResponse> withdraw(@PathVariable String accountNumber,
                                                        @Valid @RequestBody AmountRequest request) {
        String description = request.getDescription() != null ? request.getDescription() : "Cash withdrawal";
        Transaction transaction = bankService.withdrawMoney(accountNumber, request.getAmount(), description);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TransactionResponse(transaction));
    }

    // Transfer funds between accounts
    @PostMapping("/transfers")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        String description = request.getDescription() != null ? request.getDescription() : "Fund transfer";
        Transaction transaction = bankService.transferMoney(
                request.getFromAccount(), request.getToAccount(), request.getAmount(), description);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TransactionResponse(transaction));
    }

    // Account statement (transactions are a sub-resource of the account)
    @GetMapping("/accounts/{accountNumber}/transactions")
    public ResponseEntity<List<TransactionResponse>> getStatement(@PathVariable String accountNumber) {
        List<TransactionResponse> statement = bankService.getAccountStatement(accountNumber).stream()
                .map(TransactionResponse::new)
                .toList();
        return ResponseEntity.ok(statement);
    }
}
