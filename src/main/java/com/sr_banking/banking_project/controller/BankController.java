package com.sr_banking.banking_project.controller;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.service.BankService;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    private static final Logger logger = LoggerFactory.getLogger(BankController.class);

    @Autowired
    private BankService bankService;

    @PostMapping("/accounts")
    @WithSpan("controller.createAccount")
    public ResponseEntity<?> createAccount(
            @SpanAttribute("account.holderName") @RequestParam String accountHolderName,
            @SpanAttribute("account.type") @RequestParam String accountType,
            @SpanAttribute("account.initialDeposit") @RequestParam BigDecimal initialDeposit) {

        logger.info("Creating new account: holderName={}, type={}, initialDeposit={}",
                accountHolderName, accountType, initialDeposit);

        try {
            BankAccount newAccount = bankService.createAccount(accountHolderName, accountType, initialDeposit);
            logger.info("Account created successfully: accountNumber={}", newAccount.getAccountNumber());
            return ResponseEntity.ok(newAccount);
        } catch (Exception e) {
            logger.error("Failed to create account: holderName={}, error={}", accountHolderName, e.getMessage());
            return ResponseEntity.badRequest().body("Error creating account: " + e.getMessage());
        }
    }

    @GetMapping("/accounts")
    @WithSpan("controller.getAllAccounts")
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        logger.info("Fetching all accounts");

        List<BankAccount> accounts = bankService.getAllAccounts();
        logger.info("Retrieved {} accounts", accounts.size());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/{accountNumber}")
    @WithSpan("controller.getAccount")
    public ResponseEntity<?> getAccount(
            @SpanAttribute("account.number") @PathVariable String accountNumber) {

        logger.info("Fetching account: accountNumber={}", accountNumber);

        Optional<BankAccount> account = bankService.getAccountByNumber(accountNumber);

        if (account.isPresent()) {
            logger.info("Account found: accountNumber={}", accountNumber);
            return ResponseEntity.ok(account.get());
        } else {
            logger.warn("Account not found: accountNumber={}", accountNumber);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/accounts/deposit")
    @WithSpan("controller.depositMoney")
    public ResponseEntity<?> depositMoney(
            @SpanAttribute("account.number") @RequestParam String accountNumber,
            @SpanAttribute("transaction.amount") @RequestParam BigDecimal amount,
            @SpanAttribute("transaction.description") @RequestParam(defaultValue = "Cash deposit") String description) {

        logger.info("Processing deposit: accountNumber={}, amount={}", accountNumber, amount);

        try {
            Transaction transaction = bankService.depositMoney(accountNumber, amount, description);
            logger.info("Deposit successful: accountNumber={}, transactionId={}, amount={}",
                    accountNumber, transaction.getTransactionId(), amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            logger.error("Deposit failed: accountNumber={}, amount={}, error={}",
                    accountNumber, amount, e.getMessage());
            return ResponseEntity.badRequest().body("Error during deposit: " + e.getMessage());
        }
    }

    @PostMapping("/accounts/withdraw")
    @WithSpan("controller.withdrawMoney")
    public ResponseEntity<?> withdrawMoney(
            @SpanAttribute("account.number") @RequestParam String accountNumber,
            @SpanAttribute("transaction.amount") @RequestParam BigDecimal amount,
            @SpanAttribute("transaction.description") @RequestParam(defaultValue = "Cash withdrawal") String description) {

        logger.info("Processing withdrawal: accountNumber={}, amount={}", accountNumber, amount);

        try {
            Transaction transaction = bankService.withdrawMoney(accountNumber, amount, description);
            logger.info("Withdrawal successful: accountNumber={}, transactionId={}, amount={}",
                    accountNumber, transaction.getTransactionId(), amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            logger.error("Withdrawal failed: accountNumber={}, amount={}, error={}",
                    accountNumber, amount, e.getMessage());
            return ResponseEntity.badRequest().body("Error during withdrawal: " + e.getMessage());
        }
    }

    @PostMapping("/accounts/transfer")
    @WithSpan("controller.transferMoney")
    public ResponseEntity<?> transferMoney(
            @SpanAttribute("transfer.fromAccount") @RequestParam String fromAccount,
            @SpanAttribute("transfer.toAccount") @RequestParam String toAccount,
            @SpanAttribute("transaction.amount") @RequestParam BigDecimal amount,
            @SpanAttribute("transaction.description") @RequestParam(defaultValue = "Fund transfer") String description) {

        logger.info("Processing transfer: fromAccount={}, toAccount={}, amount={}",
                fromAccount, toAccount, amount);

        try {
            Transaction transaction = bankService.transferMoney(fromAccount, toAccount, amount, description);
            logger.info("Transfer successful: fromAccount={}, toAccount={}, transactionId={}, amount={}",
                    fromAccount, toAccount, transaction.getTransactionId(), amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            logger.error("Transfer failed: fromAccount={}, toAccount={}, amount={}, error={}",
                    fromAccount, toAccount, amount, e.getMessage());
            return ResponseEntity.badRequest().body("Error during transfer: " + e.getMessage());
        }
    }

    @GetMapping("/accounts/statement/{accountNumber}")
    @WithSpan("controller.getAccountStatement")
    public ResponseEntity<?> getAccountStatement(
            @SpanAttribute("account.number") @PathVariable String accountNumber) {

        logger.info("Fetching account statement: accountNumber={}", accountNumber);

        try {
            List<Transaction> statement = bankService.getAccountStatement(accountNumber);
            logger.info("Statement retrieved: accountNumber={}, transactionCount={}",
                    accountNumber, statement.size());
            return ResponseEntity.ok(statement);
        } catch (Exception e) {
            logger.error("Failed to fetch statement: accountNumber={}, error={}",
                    accountNumber, e.getMessage());
            return ResponseEntity.badRequest().body("Error fetching statement: " + e.getMessage());
        }
    }
}
