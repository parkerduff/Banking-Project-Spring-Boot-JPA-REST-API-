package com.sr_banking.banking_project.service;

import com.sr_banking.banking_project.exception.AccountNotFoundException;
import com.sr_banking.banking_project.exception.InsufficientFundsException;
import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.repository.BankAccountRepository;
import com.sr_banking.banking_project.repository.TransactionRepository;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public BankService(BankAccountRepository bankAccountRepository, TransactionRepository transactionRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public BankAccount createAccount(String accountHolderName, String accountType, BigDecimal initialDeposit) {
        BankAccount newAccount = new BankAccount(accountHolderName, accountType, initialDeposit);
        return bankAccountRepository.save(newAccount);
    }

    public Page<BankAccount> getAllAccounts(Pageable pageable) {
        return bankAccountRepository.findAll(pageable);
    }

    public BankAccount getAccountByNumber(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    @Transactional
    public Transaction depositMoney(String accountNumber, BigDecimal amount, String description) {
        BankAccount account = getAccountByNumber(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        bankAccountRepository.save(account);

        Transaction transaction = new Transaction("DEPOSIT", amount, description, account);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdrawMoney(String accountNumber, BigDecimal amount, String description) {
        BankAccount account = getAccountByNumber(accountNumber);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountNumber);
        }
        account.setBalance(account.getBalance().subtract(amount));
        bankAccountRepository.save(account);

        Transaction transaction = new Transaction("WITHDRAWAL", amount, description, account);
        return transactionRepository.save(transaction);
    }

    public Page<Transaction> getAccountStatement(String accountNumber, Pageable pageable) {
        // Validate the account exists so callers get a 404 rather than an empty page.
        getAccountByNumber(accountNumber);
        return transactionRepository.findByBankAccountAccountNumberOrderByTransactionDateDesc(accountNumber, pageable);
    }

    @Transactional
    public Transaction transferMoney(
            String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("fromAccount and toAccount must be different");
        }
        String note = description == null || description.isBlank() ? "Fund transfer" : description;
        Transaction withdrawal = withdrawMoney(
                fromAccountNumber, amount, note + " | Transfer to account: " + toAccountNumber);
        depositMoney(toAccountNumber, amount, note + " | Transfer from account: " + fromAccountNumber);
        return withdrawal;
    }
}
