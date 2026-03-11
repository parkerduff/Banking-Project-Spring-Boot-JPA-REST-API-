package com.sr_banking.banking_project.service;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.repository.BankAccountRepository;
import com.sr_banking.banking_project.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankService")
class BankServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BankService bankService;

    private BankAccount testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new BankAccount();
        testAccount.setId(1L);
        testAccount.setAccountNumber("ACC1001");
        testAccount.setAccountHolderName("Rahul Sharma");
        testAccount.setAccountType("SAVINGS");
        testAccount.setBalance(new BigDecimal("5000.00"));
    }

    @Nested
    @DisplayName("createAccount")
    class CreateAccount {

        @Test
        @DisplayName("creates and saves a new bank account")
        void createsAndSavesNewBankAccount() {
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
                BankAccount account = invocation.getArgument(0);
                account.setId(1L);
                return account;
            });

            BankAccount result = bankService.createAccount("Test User", "SAVINGS", new BigDecimal("1000.00"));

            assertThat(result).isNotNull();
            assertThat(result.getAccountHolderName()).isEqualTo("Test User");
            assertThat(result.getAccountType()).isEqualTo("SAVINGS");
            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
            verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
        }
    }

    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccounts {

        @Test
        @DisplayName("returns all accounts")
        void returnsAllAccounts() {
            BankAccount account2 = new BankAccount();
            account2.setId(2L);
            account2.setAccountNumber("ACC1002");
            when(bankAccountRepository.findAll()).thenReturn(Arrays.asList(testAccount, account2));

            List<BankAccount> result = bankService.getAllAccounts();

            assertThat(result).hasSize(2);
            verify(bankAccountRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("returns empty list when no accounts exist")
        void returnsEmptyListWhenNoAccounts() {
            when(bankAccountRepository.findAll()).thenReturn(Collections.emptyList());

            List<BankAccount> result = bankService.getAllAccounts();

            assertThat(result).isEmpty();
            verify(bankAccountRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getAccountByNumber")
    class GetAccountByNumber {

        @Test
        @DisplayName("returns account when found")
        void returnsAccountWhenFound() {
            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));

            Optional<BankAccount> result = bankService.getAccountByNumber("ACC1001");

            assertThat(result).isPresent();
            assertThat(result.get().getAccountNumber()).isEqualTo("ACC1001");
            verify(bankAccountRepository, times(1)).findByAccountNumber("ACC1001");
        }

        @Test
        @DisplayName("returns empty when not found")
        void returnsEmptyWhenNotFound() {
            when(bankAccountRepository.findByAccountNumber("ACC9999")).thenReturn(Optional.empty());

            Optional<BankAccount> result = bankService.getAccountByNumber("ACC9999");

            assertThat(result).isEmpty();
            verify(bankAccountRepository, times(1)).findByAccountNumber("ACC9999");
        }
    }

    @Nested
    @DisplayName("depositMoney")
    class DepositMoney {

        @Test
        @DisplayName("deposits money successfully and updates balance")
        void depositsMoneySuccessfully() {
            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testAccount);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Transaction result = bankService.depositMoney("ACC1001", new BigDecimal("1000.00"), "Cash deposit");

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("6000.00"));
            verify(bankAccountRepository, times(1)).save(testAccount);
            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws exception when account not found")
        void throwsExceptionWhenAccountNotFound() {
            when(bankAccountRepository.findByAccountNumber("ACC9999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.depositMoney("ACC9999", new BigDecimal("1000.00"), "Cash deposit"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: ACC9999");

            verify(bankAccountRepository, never()).save(any(BankAccount.class));
            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("withdrawMoney")
    class WithdrawMoney {

        @Test
        @DisplayName("withdraws money successfully and updates balance")
        void withdrawsMoneySuccessfully() {
            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testAccount);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Transaction result = bankService.withdrawMoney("ACC1001", new BigDecimal("2000.00"), "ATM withdrawal");

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo("WITHDRAWAL");
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
            assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("3000.00"));
            verify(bankAccountRepository, times(1)).save(testAccount);
            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws exception when insufficient balance")
        void throwsExceptionWhenInsufficientBalance() {
            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));

            assertThatThrownBy(() -> bankService.withdrawMoney("ACC1001", new BigDecimal("10000.00"), "Large withdrawal"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient balance in account: ACC1001");

            verify(bankAccountRepository, never()).save(any(BankAccount.class));
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws exception when account not found")
        void throwsExceptionWhenAccountNotFound() {
            when(bankAccountRepository.findByAccountNumber("ACC9999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.withdrawMoney("ACC9999", new BigDecimal("100.00"), "Withdrawal"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: ACC9999");

            verify(bankAccountRepository, never()).save(any(BankAccount.class));
        }

        @Test
        @DisplayName("allows withdrawal of exact balance amount")
        void allowsWithdrawalOfExactBalance() {
            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(testAccount);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Transaction result = bankService.withdrawMoney("ACC1001", new BigDecimal("5000.00"), "Full withdrawal");

            assertThat(result).isNotNull();
            assertThat(testAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getAccountStatement")
    class GetAccountStatement {

        @Test
        @DisplayName("returns list of transactions for account")
        void returnsTransactionsForAccount() {
            Transaction txn1 = new Transaction("DEPOSIT", new BigDecimal("1000.00"), "Deposit 1", testAccount);
            Transaction txn2 = new Transaction("WITHDRAWAL", new BigDecimal("500.00"), "Withdrawal 1", testAccount);
            when(transactionRepository.findByBankAccountAccountNumber("ACC1001")).thenReturn(Arrays.asList(txn1, txn2));

            List<Transaction> result = bankService.getAccountStatement("ACC1001");

            assertThat(result).hasSize(2);
            verify(transactionRepository, times(1)).findByBankAccountAccountNumber("ACC1001");
        }

        @Test
        @DisplayName("returns empty list when no transactions exist")
        void returnsEmptyListWhenNoTransactions() {
            when(transactionRepository.findByBankAccountAccountNumber("ACC1001")).thenReturn(Collections.emptyList());

            List<Transaction> result = bankService.getAccountStatement("ACC1001");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("transferMoney")
    class TransferMoney {

        @Test
        @DisplayName("transfers money between two accounts successfully")
        void transfersMoneySuccessfully() {
            BankAccount targetAccount = new BankAccount();
            targetAccount.setId(2L);
            targetAccount.setAccountNumber("ACC1002");
            targetAccount.setAccountHolderName("Priya Patel");
            targetAccount.setAccountType("CURRENT");
            targetAccount.setBalance(new BigDecimal("10000.00"));

            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));
            when(bankAccountRepository.findByAccountNumber("ACC1002")).thenReturn(Optional.of(targetAccount));
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Transaction result = bankService.transferMoney("ACC1001", "ACC1002", new BigDecimal("2000.00"), "Transfer");

            assertThat(result).isNotNull();
            assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("3000.00"));
            assertThat(targetAccount.getBalance()).isEqualByComparingTo(new BigDecimal("12000.00"));
            verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
            verify(transactionRepository, times(2)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws exception when source account not found")
        void throwsExceptionWhenSourceNotFound() {
            when(bankAccountRepository.findByAccountNumber("ACC9999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.transferMoney("ACC9999", "ACC1002", new BigDecimal("1000.00"), "Transfer"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: ACC9999");
        }
    }
}
