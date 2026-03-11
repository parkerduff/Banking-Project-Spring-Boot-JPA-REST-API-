package com.sr_banking.banking_project.controller;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    @Autowired
    private BankService bankService;

    // Create new account endpoint
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(
            @RequestParam String accountHolderName,
            @RequestParam String accountType,
            @RequestParam BigDecimal initialDeposit) {

        try {
            BankAccount newAccount = bankService.createAccount(accountHolderName, accountType, initialDeposit);
            return ResponseEntity.ok(newAccount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating account: " + e.getMessage());
        }
    }

    // Get all accounts endpoint
    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        List<BankAccount> accounts = bankService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    // Get account by number endpoint
    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber) {
        Optional<BankAccount> account = bankService.getAccountByNumber(accountNumber);

        if (account.isPresent()) {
            return ResponseEntity.ok(account.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Deposit money endpoint
    @PostMapping("/accounts/deposit")
    public ResponseEntity<?> depositMoney(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "Cash deposit") String description) {

        try {
            Transaction transaction = bankService.depositMoney(accountNumber, amount, description);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error during deposit: " + e.getMessage());
        }
    }

    // Withdraw money endpoint
    @PostMapping("/accounts/withdraw")
    public ResponseEntity<?> withdrawMoney(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "Cash withdrawal") String description) {

        try {
            Transaction transaction = bankService.withdrawMoney(accountNumber, amount, description);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error during withdrawal: " + e.getMessage());
        }
    }

    // Transfer money endpoint
    @PostMapping("/accounts/transfer")
    public ResponseEntity<?> transferMoney(
            @RequestParam String fromAccount,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "Fund transfer") String description) {

        try {
            Transaction transaction = bankService.transferMoney(fromAccount, toAccount, amount, description);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error during transfer: " + e.getMessage());
        }
    }

    // Get account statement endpoint
    @GetMapping("/accounts/statement/{accountNumber}")
    public ResponseEntity<?> getAccountStatement(@PathVariable String accountNumber) {
        try {
            List<Transaction> statement = bankService.getAccountStatement(accountNumber);
            return ResponseEntity.ok(statement);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching statement: " + e.getMessage());
        }
    }
}