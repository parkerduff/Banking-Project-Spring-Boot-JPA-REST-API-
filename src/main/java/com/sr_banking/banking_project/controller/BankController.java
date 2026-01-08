package com.sr_banking.banking_project.controller;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.service.BankService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
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

    @Autowired
    private Tracer tracer;

    @WithSpan("createAccount")
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(
            @SpanAttribute("account.holderName") @RequestParam String accountHolderName,
            @SpanAttribute("account.type") @RequestParam String accountType,
            @SpanAttribute("account.initialDeposit") @RequestParam BigDecimal initialDeposit) {

        Span span = Span.current();
        logger.info("Creating new account for holder: {}, type: {}, initial deposit: {}",
                accountHolderName, accountType, initialDeposit);

        try {
            BankAccount newAccount = bankService.createAccount(accountHolderName, accountType, initialDeposit);
            span.setAttribute("account.number", newAccount.getAccountNumber());
            span.setStatus(StatusCode.OK);
            logger.info("Account created successfully: {}", newAccount.getAccountNumber());
            return ResponseEntity.ok(newAccount);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Error creating account: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating account: " + e.getMessage());
        }
    }

    @WithSpan("getAllAccounts")
    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        Span span = Span.current();
        logger.info("Fetching all bank accounts");

        List<BankAccount> accounts = bankService.getAllAccounts();
        span.setAttribute("accounts.count", accounts.size());
        span.setStatus(StatusCode.OK);
        logger.info("Retrieved {} accounts", accounts.size());
        return ResponseEntity.ok(accounts);
    }

    @WithSpan("getAccount")
    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<?> getAccount(
            @SpanAttribute("account.number") @PathVariable String accountNumber) {
        Span span = Span.current();
        logger.info("Fetching account: {}", accountNumber);

        Optional<BankAccount> account = bankService.getAccountByNumber(accountNumber);

        if (account.isPresent()) {
            span.setAttribute("account.found", true);
            span.setAttribute("account.holder", account.get().getAccountHolderName());
            span.setStatus(StatusCode.OK);
            logger.info("Account found: {}", accountNumber);
            return ResponseEntity.ok(account.get());
        } else {
            span.setAttribute("account.found", false);
            span.setStatus(StatusCode.ERROR, "Account not found");
            logger.warn("Account not found: {}", accountNumber);
            return ResponseEntity.notFound().build();
        }
    }

    @WithSpan("depositMoney")
    @PostMapping("/accounts/deposit")
    public ResponseEntity<?> depositMoney(
            @SpanAttribute("account.number") @RequestParam String accountNumber,
            @SpanAttribute("transaction.amount") @RequestParam BigDecimal amount,
            @SpanAttribute("transaction.description") @RequestParam(defaultValue = "Cash deposit") String description) {

        Span span = Span.current();
        span.setAttribute("transaction.type", "DEPOSIT");
        logger.info("Processing deposit: account={}, amount={}, description={}",
                accountNumber, amount, description);

        try {
            Transaction transaction = bankService.depositMoney(accountNumber, amount, description);
            span.setAttribute("transaction.id", transaction.getTransactionId());
            span.setStatus(StatusCode.OK);
            logger.info("Deposit successful: transactionId={}, account={}, amount={}",
                    transaction.getTransactionId(), accountNumber, amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Deposit failed: account={}, amount={}, error={}",
                    accountNumber, amount, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during deposit: " + e.getMessage());
        }
    }

    @WithSpan("withdrawMoney")
    @PostMapping("/accounts/withdraw")
    public ResponseEntity<?> withdrawMoney(
            @SpanAttribute("account.number") @RequestParam String accountNumber,
            @SpanAttribute("transaction.amount") @RequestParam BigDecimal amount,
            @SpanAttribute("transaction.description") @RequestParam(defaultValue = "Cash withdrawal") String description) {

        Span span = Span.current();
        span.setAttribute("transaction.type", "WITHDRAWAL");
        logger.info("Processing withdrawal: account={}, amount={}, description={}",
                accountNumber, amount, description);

        try {
            Transaction transaction = bankService.withdrawMoney(accountNumber, amount, description);
            span.setAttribute("transaction.id", transaction.getTransactionId());
            span.setStatus(StatusCode.OK);
            logger.info("Withdrawal successful: transactionId={}, account={}, amount={}",
                    transaction.getTransactionId(), accountNumber, amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Withdrawal failed: account={}, amount={}, error={}",
                    accountNumber, amount, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during withdrawal: " + e.getMessage());
        }
    }

    @WithSpan("transferMoney")
    @PostMapping("/accounts/transfer")
    public ResponseEntity<?> transferMoney(
            @SpanAttribute("account.from") @RequestParam String fromAccount,
            @SpanAttribute("account.to") @RequestParam String toAccount,
            @SpanAttribute("transaction.amount") @RequestParam BigDecimal amount,
            @SpanAttribute("transaction.description") @RequestParam(defaultValue = "Fund transfer") String description) {

        Span span = Span.current();
        span.setAttribute("transaction.type", "TRANSFER");
        logger.info("Processing transfer: from={}, to={}, amount={}, description={}",
                fromAccount, toAccount, amount, description);

        try {
            Transaction transaction = bankService.transferMoney(fromAccount, toAccount, amount, description);
            span.setAttribute("transaction.id", transaction.getTransactionId());
            span.setStatus(StatusCode.OK);
            logger.info("Transfer successful: transactionId={}, from={}, to={}, amount={}",
                    transaction.getTransactionId(), fromAccount, toAccount, amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Transfer failed: from={}, to={}, amount={}, error={}",
                    fromAccount, toAccount, amount, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during transfer: " + e.getMessage());
        }
    }

    @WithSpan("getAccountStatement")
    @GetMapping("/accounts/statement/{accountNumber}")
    public ResponseEntity<?> getAccountStatement(
            @SpanAttribute("account.number") @PathVariable String accountNumber) {
        Span span = Span.current();
        logger.info("Fetching account statement: account={}", accountNumber);

        try {
            List<Transaction> statement = bankService.getAccountStatement(accountNumber);
            span.setAttribute("statement.transactionCount", statement.size());
            span.setStatus(StatusCode.OK);
            logger.info("Statement retrieved: account={}, transactionCount={}",
                    accountNumber, statement.size());
            return ResponseEntity.ok(statement);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            logger.error("Failed to fetch statement: account={}, error={}",
                    accountNumber, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching statement: " + e.getMessage());
        }
    }
}
