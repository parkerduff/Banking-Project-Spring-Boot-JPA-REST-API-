package com.sr_banking.banking_project.service;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.repository.BankAccountRepository;
import com.sr_banking.banking_project.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BankService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Create new bank account
    public BankAccount createAccount(String accountHolderName, String accountType, BigDecimal initialDeposit) {
        BankAccount newAccount = new BankAccount(accountHolderName, accountType, initialDeposit);
        return bankAccountRepository.save(newAccount);
    }

    // Get all bank accounts
    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();
    }

    // Get account by account number
    public Optional<BankAccount> getAccountByNumber(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber);
    }

    // Deposit money into account
    @Transactional
    public Transaction depositMoney(String accountNumber, BigDecimal amount, String description) {
        Optional<BankAccount> accountOptional = bankAccountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isPresent()) {
            BankAccount account = accountOptional.get();

            // Update account balance
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            bankAccountRepository.save(account);

            // Create transaction record
            Transaction transaction = new Transaction("DEPOSIT", amount, description, account);
            return transactionRepository.save(transaction);

        } else {
            throw new RuntimeException("Account not found with number: " + accountNumber);
        }
    }

    // Withdraw money from account
    @Transactional
    public Transaction withdrawMoney(String accountNumber, BigDecimal amount, String description) {
        Optional<BankAccount> accountOptional = bankAccountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isPresent()) {
            BankAccount account = accountOptional.get();

            // Check sufficient balance
            if (account.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient balance in account: " + accountNumber);
            }

            // Update account balance
            BigDecimal newBalance = account.getBalance().subtract(amount);
            account.setBalance(newBalance);
            bankAccountRepository.save(account);

            // Create transaction record
            Transaction transaction = new Transaction("WITHDRAWAL", amount, description, account);
            return transactionRepository.save(transaction);

        } else {
            throw new RuntimeException("Account not found with number: " + accountNumber);
        }
    }

    // Get account statement
    public List<Transaction> getAccountStatement(String accountNumber) {
        return transactionRepository.findByBankAccountAccountNumber(accountNumber);
    }

    // Transfer money between accounts
    @Transactional
    public Transaction transferMoney(String fromAccountNumber, String toAccountNumber,
                                     BigDecimal amount, String description) {

        // Withdraw from source account
        Transaction withdrawalTransaction = withdrawMoney(fromAccountNumber, amount,
                "Transfer to account: " + toAccountNumber);

        // Deposit to target account
        depositMoney(toAccountNumber, amount,
                "Transfer from account: " + fromAccountNumber);

        return withdrawalTransaction;
    }
}