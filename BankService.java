package com.sr_banking.banking_project.service;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.repository.BankAccountRepository;
import com.sr_banking.banking_project.repository.TransactionRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
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

    @Autowired
    private Tracer tracer;

    @WithSpan("BankService.createAccount")
    public BankAccount createAccount(
            @SpanAttribute("account.holderName") String accountHolderName,
            @SpanAttribute("account.type") String accountType,
            @SpanAttribute("account.initialDeposit") BigDecimal initialDeposit) {
        Span span = Span.current();
        logger.info("Creating new bank account: holder={}, type={}, initialDeposit={}",
                accountHolderName, accountType, initialDeposit);

        BankAccount newAccount = new BankAccount(accountHolderName, accountType, initialDeposit);
        BankAccount savedAccount = bankAccountRepository.save(newAccount);

        span.setAttribute("account.number", savedAccount.getAccountNumber());
        span.setAttribute("account.id", savedAccount.getId());
        span.setStatus(StatusCode.OK);
        logger.info("Bank account created successfully: accountNumber={}, holder={}",
                savedAccount.getAccountNumber(), accountHolderName);

        return savedAccount;
    }

    @WithSpan("BankService.getAllAccounts")
    public List<BankAccount> getAllAccounts() {
        Span span = Span.current();
        logger.info("Fetching all bank accounts");

        List<BankAccount> accounts = bankAccountRepository.findAll();

        span.setAttribute("accounts.count", accounts.size());
        span.setStatus(StatusCode.OK);
        logger.info("Retrieved {} bank accounts", accounts.size());

        return accounts;
    }

    @WithSpan("BankService.getAccountByNumber")
    public Optional<BankAccount> getAccountByNumber(
            @SpanAttribute("account.number") String accountNumber) {
        Span span = Span.current();
        logger.info("Looking up account: {}", accountNumber);

        Optional<BankAccount> account = bankAccountRepository.findByAccountNumber(accountNumber);

        span.setAttribute("account.found", account.isPresent());
        if (account.isPresent()) {
            span.setAttribute("account.holder", account.get().getAccountHolderName());
            span.setStatus(StatusCode.OK);
            logger.info("Account found: {}", accountNumber);
        } else {
            span.setStatus(StatusCode.OK);
            logger.info("Account not found: {}", accountNumber);
        }

        return account;
    }

    @WithSpan("BankService.depositMoney")
    @Transactional
    public Transaction depositMoney(
            @SpanAttribute("account.number") String accountNumber,
            @SpanAttribute("transaction.amount") BigDecimal amount,
            @SpanAttribute("transaction.description") String description) {
        Span span = Span.current();
        span.setAttribute("transaction.type", "DEPOSIT");
        logger.info("Processing deposit: account={}, amount={}, description={}",
                accountNumber, amount, description);

        Optional<BankAccount> accountOptional = bankAccountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isPresent()) {
            BankAccount account = accountOptional.get();
            BigDecimal previousBalance = account.getBalance();

            span.addEvent("Account found");
            span.setAttribute("account.holder", account.getAccountHolderName());
            span.setAttribute("account.previousBalance", previousBalance.doubleValue());
            logger.debug("Account found: holder={}, previousBalance={}",
                    account.getAccountHolderName(), previousBalance);

            BigDecimal newBalance = previousBalance.add(amount);
            account.setBalance(newBalance);
            bankAccountRepository.save(account);

            span.addEvent("Balance updated");
            span.setAttribute("account.newBalance", newBalance.doubleValue());
            logger.debug("Balance updated: previousBalance={}, newBalance={}",
                    previousBalance, newBalance);

            Transaction transaction = new Transaction("DEPOSIT", amount, description, account);
            Transaction savedTransaction = transactionRepository.save(transaction);

            span.setAttribute("transaction.id", savedTransaction.getTransactionId());
            span.setStatus(StatusCode.OK);
            logger.info("Deposit completed: transactionId={}, account={}, amount={}, newBalance={}",
                    savedTransaction.getTransactionId(), accountNumber, amount, newBalance);

            return savedTransaction;

        } else {
            span.addEvent("Account not found");
            span.setStatus(StatusCode.ERROR, "Account not found");
            logger.error("Deposit failed - account not found: {}", accountNumber);
            throw new RuntimeException("Account not found with number: " + accountNumber);
        }
    }

    @WithSpan("BankService.withdrawMoney")
    @Transactional
    public Transaction withdrawMoney(
            @SpanAttribute("account.number") String accountNumber,
            @SpanAttribute("transaction.amount") BigDecimal amount,
            @SpanAttribute("transaction.description") String description) {
        Span span = Span.current();
        span.setAttribute("transaction.type", "WITHDRAWAL");
        logger.info("Processing withdrawal: account={}, amount={}, description={}",
                accountNumber, amount, description);

        Optional<BankAccount> accountOptional = bankAccountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isPresent()) {
            BankAccount account = accountOptional.get();
            BigDecimal currentBalance = account.getBalance();

            span.addEvent("Account found");
            span.setAttribute("account.holder", account.getAccountHolderName());
            span.setAttribute("account.currentBalance", currentBalance.doubleValue());
            logger.debug("Account found: holder={}, currentBalance={}",
                    account.getAccountHolderName(), currentBalance);

            if (currentBalance.compareTo(amount) < 0) {
                span.addEvent("Insufficient balance");
                span.setAttribute("withdrawal.rejected", true);
                span.setAttribute("withdrawal.rejectionReason", "Insufficient balance");
                span.setStatus(StatusCode.ERROR, "Insufficient balance");
                logger.warn("Withdrawal rejected - insufficient balance: account={}, requested={}, available={}",
                        accountNumber, amount, currentBalance);
                throw new RuntimeException("Insufficient balance in account: " + accountNumber);
            }

            span.addEvent("Balance validation passed");
            logger.debug("Balance validation passed: requested={}, available={}",
                    amount, currentBalance);

            BigDecimal newBalance = currentBalance.subtract(amount);
            account.setBalance(newBalance);
            bankAccountRepository.save(account);

            span.addEvent("Balance updated");
            span.setAttribute("account.newBalance", newBalance.doubleValue());
            logger.debug("Balance updated: previousBalance={}, newBalance={}",
                    currentBalance, newBalance);

            Transaction transaction = new Transaction("WITHDRAWAL", amount, description, account);
            Transaction savedTransaction = transactionRepository.save(transaction);

            span.setAttribute("transaction.id", savedTransaction.getTransactionId());
            span.setStatus(StatusCode.OK);
            logger.info("Withdrawal completed: transactionId={}, account={}, amount={}, newBalance={}",
                    savedTransaction.getTransactionId(), accountNumber, amount, newBalance);

            return savedTransaction;

        } else {
            span.addEvent("Account not found");
            span.setStatus(StatusCode.ERROR, "Account not found");
            logger.error("Withdrawal failed - account not found: {}", accountNumber);
            throw new RuntimeException("Account not found with number: " + accountNumber);
        }
    }

    @WithSpan("BankService.getAccountStatement")
    public List<Transaction> getAccountStatement(
            @SpanAttribute("account.number") String accountNumber) {
        Span span = Span.current();
        logger.info("Fetching account statement: account={}", accountNumber);

        List<Transaction> transactions = transactionRepository.findByBankAccountAccountNumber(accountNumber);

        span.setAttribute("statement.transactionCount", transactions.size());
        span.setStatus(StatusCode.OK);
        logger.info("Account statement retrieved: account={}, transactionCount={}",
                accountNumber, transactions.size());

        return transactions;
    }

    @WithSpan("BankService.transferMoney")
    @Transactional
    public Transaction transferMoney(
            @SpanAttribute("account.from") String fromAccountNumber,
            @SpanAttribute("account.to") String toAccountNumber,
            @SpanAttribute("transaction.amount") BigDecimal amount,
            @SpanAttribute("transaction.description") String description) {
        Span span = Span.current();
        span.setAttribute("transaction.type", "TRANSFER");
        logger.info("Processing transfer: from={}, to={}, amount={}, description={}",
                fromAccountNumber, toAccountNumber, amount, description);

        span.addEvent("Starting withdrawal from source account");
        logger.debug("Initiating withdrawal from source account: {}", fromAccountNumber);

        Transaction withdrawalTransaction = withdrawMoney(fromAccountNumber, amount,
                "Transfer to account: " + toAccountNumber);

        span.addEvent("Withdrawal completed, starting deposit to target account");
        span.setAttribute("withdrawal.transactionId", withdrawalTransaction.getTransactionId());
        logger.debug("Withdrawal completed: transactionId={}, initiating deposit to target account: {}",
                withdrawalTransaction.getTransactionId(), toAccountNumber);

        depositMoney(toAccountNumber, amount,
                "Transfer from account: " + fromAccountNumber);

        span.addEvent("Transfer completed");
        span.setStatus(StatusCode.OK);
        logger.info("Transfer completed: from={}, to={}, amount={}, withdrawalTransactionId={}",
                fromAccountNumber, toAccountNumber, amount, withdrawalTransaction.getTransactionId());

        return withdrawalTransaction;
    }
}
