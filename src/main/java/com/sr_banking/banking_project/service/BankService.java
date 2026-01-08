package com.sr_banking.banking_project.service;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.repository.BankAccountRepository;
import com.sr_banking.banking_project.repository.TransactionRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BankService {

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @WithSpan("service.createAccount")
    public BankAccount createAccount(
            @SpanAttribute("account.holderName") String accountHolderName,
            @SpanAttribute("account.type") String accountType,
            @SpanAttribute("account.initialDeposit") BigDecimal initialDeposit) {

        Span span = Span.current();
        logger.info("Creating account: holderName={}, type={}, initialDeposit={}",
                accountHolderName, accountType, initialDeposit);

        BankAccount newAccount = new BankAccount(accountHolderName, accountType, initialDeposit);
        span.addEvent("Account object created");

        BankAccount savedAccount = bankAccountRepository.save(newAccount);
        span.setAttribute("account.number", savedAccount.getAccountNumber());
        span.addEvent("Account persisted to database");
        span.setStatus(StatusCode.OK);

        logger.info("Account created: accountNumber={}, holderName={}",
                savedAccount.getAccountNumber(), accountHolderName);
        return savedAccount;
    }

    @WithSpan("service.getAllAccounts")
    public List<BankAccount> getAllAccounts() {
        logger.info("Fetching all accounts");

        List<BankAccount> accounts = bankAccountRepository.findAll();
        Span.current().setAttribute("accounts.count", accounts.size());
        Span.current().setStatus(StatusCode.OK);

        logger.info("Retrieved {} accounts", accounts.size());
        return accounts;
    }

    @WithSpan("service.getAccountByNumber")
    public Optional<BankAccount> getAccountByNumber(
            @SpanAttribute("account.number") String accountNumber) {

        logger.info("Fetching account: accountNumber={}", accountNumber);

        Optional<BankAccount> account = bankAccountRepository.findByAccountNumber(accountNumber);
        Span span = Span.current();

        if (account.isPresent()) {
            span.setAttribute("account.found", true);
            span.setStatus(StatusCode.OK);
            logger.info("Account found: accountNumber={}", accountNumber);
        } else {
            span.setAttribute("account.found", false);
            span.setStatus(StatusCode.OK);
            logger.warn("Account not found: accountNumber={}", accountNumber);
        }

        return account;
    }

    @Transactional
    @WithSpan("service.depositMoney")
    public Transaction depositMoney(
            @SpanAttribute("account.number") String accountNumber,
            @SpanAttribute("transaction.amount") BigDecimal amount,
            @SpanAttribute("transaction.description") String description) {

        Span span = Span.current();
        span.setAttribute("transaction.type", "DEPOSIT");
        logger.info("Processing deposit: accountNumber={}, amount={}", accountNumber, amount);

        Optional<BankAccount> accountOptional = bankAccountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isPresent()) {
            BankAccount account = accountOptional.get();
            span.addEvent("Account found");
            span.setAttribute("account.previousBalance", account.getBalance().doubleValue());

            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            bankAccountRepository.save(account);
            span.addEvent("Balance updated");
            span.setAttribute("account.newBalance", newBalance.doubleValue());

            Transaction transaction = new Transaction("DEPOSIT", amount, description, account);
            Transaction savedTransaction = transactionRepository.save(transaction);
            span.addEvent("Transaction record created");
            span.setAttribute("transaction.id", savedTransaction.getTransactionId());
            span.setStatus(StatusCode.OK);

            logger.info("Deposit completed: accountNumber={}, transactionId={}, newBalance={}",
                    accountNumber, savedTransaction.getTransactionId(), newBalance);
            return savedTransaction;

        } else {
            span.addEvent("Account not found");
            span.setStatus(StatusCode.ERROR, "Account not found");
            logger.error("Deposit failed - account not found: accountNumber={}", accountNumber);
            throw new RuntimeException("Account not found with number: " + accountNumber);
        }
    }

    @Transactional
    @WithSpan("service.withdrawMoney")
    public Transaction withdrawMoney(
            @SpanAttribute("account.number") String accountNumber,
            @SpanAttribute("transaction.amount") BigDecimal amount,
            @SpanAttribute("transaction.description") String description) {

        Span span = Span.current();
        span.setAttribute("transaction.type", "WITHDRAWAL");
        logger.info("Processing withdrawal: accountNumber={}, amount={}", accountNumber, amount);

        Optional<BankAccount> accountOptional = bankAccountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isPresent()) {
            BankAccount account = accountOptional.get();
            span.addEvent("Account found");
            span.setAttribute("account.previousBalance", account.getBalance().doubleValue());

            if (account.getBalance().compareTo(amount) < 0) {
                span.addEvent("Insufficient balance");
                span.setAttribute("balance.insufficient", true);
                span.setStatus(StatusCode.ERROR, "Insufficient balance");
                logger.error("Withdrawal failed - insufficient balance: accountNumber={}, balance={}, requestedAmount={}",
                        accountNumber, account.getBalance(), amount);
                throw new RuntimeException("Insufficient balance in account: " + accountNumber);
            }
            span.addEvent("Balance validated");
            span.setAttribute("balance.insufficient", false);

            BigDecimal newBalance = account.getBalance().subtract(amount);
            account.setBalance(newBalance);
            bankAccountRepository.save(account);
            span.addEvent("Balance updated");
            span.setAttribute("account.newBalance", newBalance.doubleValue());

            Transaction transaction = new Transaction("WITHDRAWAL", amount, description, account);
            Transaction savedTransaction = transactionRepository.save(transaction);
            span.addEvent("Transaction record created");
            span.setAttribute("transaction.id", savedTransaction.getTransactionId());
            span.setStatus(StatusCode.OK);

            logger.info("Withdrawal completed: accountNumber={}, transactionId={}, newBalance={}",
                    accountNumber, savedTransaction.getTransactionId(), newBalance);
            return savedTransaction;

        } else {
            span.addEvent("Account not found");
            span.setStatus(StatusCode.ERROR, "Account not found");
            logger.error("Withdrawal failed - account not found: accountNumber={}", accountNumber);
            throw new RuntimeException("Account not found with number: " + accountNumber);
        }
    }

    @WithSpan("service.getAccountStatement")
    public List<Transaction> getAccountStatement(
            @SpanAttribute("account.number") String accountNumber) {

        logger.info("Fetching statement: accountNumber={}", accountNumber);

        List<Transaction> transactions = transactionRepository.findByBankAccountAccountNumber(accountNumber);
        Span span = Span.current();
        span.setAttribute("statement.transactionCount", transactions.size());
        span.setStatus(StatusCode.OK);

        logger.info("Statement retrieved: accountNumber={}, transactionCount={}",
                accountNumber, transactions.size());
        return transactions;
    }

    @Transactional
    @WithSpan("service.transferMoney")
    public Transaction transferMoney(
            @SpanAttribute("transfer.fromAccount") String fromAccountNumber,
            @SpanAttribute("transfer.toAccount") String toAccountNumber,
            @SpanAttribute("transaction.amount") BigDecimal amount,
            @SpanAttribute("transaction.description") String description) {

        Span span = Span.current();
        span.setAttribute("transaction.type", "TRANSFER");
        logger.info("Processing transfer: fromAccount={}, toAccount={}, amount={}",
                fromAccountNumber, toAccountNumber, amount);

        span.addEvent("Initiating withdrawal from source account");
        Transaction withdrawalTransaction = withdrawMoney(fromAccountNumber, amount,
                "Transfer to account: " + toAccountNumber);
        span.addEvent("Withdrawal completed");

        span.addEvent("Initiating deposit to target account");
        depositMoney(toAccountNumber, amount,
                "Transfer from account: " + fromAccountNumber);
        span.addEvent("Deposit completed");

        span.setAttribute("transaction.id", withdrawalTransaction.getTransactionId());
        span.setStatus(StatusCode.OK);

        logger.info("Transfer completed: fromAccount={}, toAccount={}, transactionId={}, amount={}",
                fromAccountNumber, toAccountNumber, withdrawalTransaction.getTransactionId(), amount);
        return withdrawalTransaction;
    }
}
