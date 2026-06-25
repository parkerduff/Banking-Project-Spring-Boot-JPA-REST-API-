package com.sr_banking.banking_project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sr_banking.banking_project.exception.AccountNotFoundException;
import com.sr_banking.banking_project.exception.InsufficientFundsException;
import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BankServiceTest {

    @Autowired
    private BankService bankService;

    @Test
    void depositIncreasesBalanceAndRecordsTransaction() {
        BankAccount account = bankService.createAccount("Test User", "SAVINGS", new BigDecimal("100.00"));

        Transaction transaction = bankService.depositMoney(account.getAccountNumber(), new BigDecimal("50.00"), "pay");

        assertThat(transaction.getTransactionType()).isEqualTo("DEPOSIT");
        assertThat(bankService.getAccountByNumber(account.getAccountNumber()).getBalance())
                .isEqualByComparingTo("150.00");
    }

    @Test
    void withdrawWithInsufficientFundsThrows() {
        BankAccount account = bankService.createAccount("Test User", "SAVINGS", new BigDecimal("20.00"));

        assertThatThrownBy(() ->
                bankService.withdrawMoney(account.getAccountNumber(), new BigDecimal("50.00"), "atm"))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void transferMovesFundsBetweenAccounts() {
        BankAccount from = bankService.createAccount("Sender", "CURRENT", new BigDecimal("200.00"));
        BankAccount to = bankService.createAccount("Receiver", "SAVINGS", new BigDecimal("0.00"));

        bankService.transferMoney(from.getAccountNumber(), to.getAccountNumber(), new BigDecimal("75.00"), null);

        assertThat(bankService.getAccountByNumber(from.getAccountNumber()).getBalance())
                .isEqualByComparingTo("125.00");
        assertThat(bankService.getAccountByNumber(to.getAccountNumber()).getBalance())
                .isEqualByComparingTo("75.00");
    }

    @Test
    void statementForUnknownAccountThrows() {
        assertThatThrownBy(() -> bankService.getAccountStatement("ACC_DOES_NOT_EXIST", PageRequest.of(0, 10)))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void listAccountsIsPaginated() {
        Page<BankAccount> page = bankService.getAllAccounts(PageRequest.of(0, 3));
        assertThat(page.getSize()).isEqualTo(3);
        assertThat(page.getContent()).hasSizeLessThanOrEqualTo(3);
    }
}
