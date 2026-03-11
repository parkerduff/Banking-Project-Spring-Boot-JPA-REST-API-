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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        void createsAndSavesNewAccount() {
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

            BankAccount result = bankService.createAccount("Test User", "SAVINGS", new BigDecimal("1000.00"));

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
        @DisplayName("returns all accounts from repository")
        void returnsAllAccounts() {
            BankAccount account2 = new BankAccount();
            account2.setAccountNumber("ACC1002");
            when(bankAccountRepository.findAll()).thenReturn(List.of(testAccount, account2));

            List<BankAccount> result = bankService.getAllAccounts();

            assertThat(result).hasSize(2);
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
        }

        @Test
        @DisplayName("returns empty when account not found")
        void returnsEmptyWhenNotFound() {
            when(bankAccountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

            Optional<BankAccount> result = bankService.getAccountByNumber("INVALID");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("depositMoney")
    class DepositMoney {

        @Test
        @DisplayName("deposits money and creates transaction record")
        void depositsMoneyAndCreatesTransaction() {
            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            Transaction result = bankService.depositMoney("ACC1001", new BigDecimal("1000.00"), "Cash deposit");

            assertThat(result.getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(result.getDescription()).isEqualTo("Cash deposit");

            // Verify balance was updated
            ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
            verify(bankAccountRepository).save(accountCaptor.capture());
            assertThat(accountCaptor.getValue().getBalance()).isEqualByComparingTo(new BigDecimal("6000.00"));

            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws exception when account not found")
        void throwsExceptionWhenAccountNotFound() {
            when(bankAccountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.depositMoney("INVALID", new BigDecimal("100.00"), "Test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: INVALID");

            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("withdrawMoney")
    class WithdrawMoney {

        @Test
        @DisplayName("withdraws money and creates transaction record")
        void withdrawsMoneyAndCreatesTransaction() {
            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            Transaction result = bankService.withdrawMoney("ACC1001", new BigDecimal("2000.00"), "ATM withdrawal");

            assertThat(result.getTransactionType()).isEqualTo("WITHDRAWAL");
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));

            ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
            verify(bankAccountRepository).save(accountCaptor.capture());
            assertThat(accountCaptor.getValue().getBalance()).isEqualByComparingTo(new BigDecimal("3000.00"));
        }

        @Test
        @DisplayName("throws exception when insufficient balance")
        void throwsExceptionWhenInsufficientBalance() {
            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));

            assertThatThrownBy(() -> bankService.withdrawMoney("ACC1001", new BigDecimal("10000.00"), "Large withdrawal"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient balance in account: ACC1001");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws exception when account not found")
        void throwsExceptionWhenAccountNotFound() {
            when(bankAccountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.withdrawMoney("INVALID", new BigDecimal("100.00"), "Test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: INVALID");
        }
    }

    @Nested
    @DisplayName("getAccountStatement")
    class GetAccountStatement {

        @Test
        @DisplayName("returns transactions for account")
        void returnsTransactionsForAccount() {
            Transaction txn1 = new Transaction("DEPOSIT", new BigDecimal("1000.00"), "Deposit", testAccount);
            Transaction txn2 = new Transaction("WITHDRAWAL", new BigDecimal("500.00"), "Withdrawal", testAccount);
            when(transactionRepository.findByBankAccountAccountNumber("ACC1001"))
                    .thenReturn(List.of(txn1, txn2));

            List<Transaction> result = bankService.getAccountStatement("ACC1001");

            assertThat(result).hasSize(2);
            verify(transactionRepository, times(1)).findByBankAccountAccountNumber("ACC1001");
        }
    }

    @Nested
    @DisplayName("transferMoney")
    class TransferMoney {

        @Test
        @DisplayName("transfers money between two accounts")
        void transfersMoneyBetweenAccounts() {
            BankAccount toAccount = new BankAccount();
            toAccount.setId(2L);
            toAccount.setAccountNumber("ACC1002");
            toAccount.setAccountHolderName("Priya Patel");
            toAccount.setAccountType("CURRENT");
            toAccount.setBalance(new BigDecimal("10000.00"));

            when(bankAccountRepository.findByAccountNumber("ACC1001")).thenReturn(Optional.of(testAccount));
            when(bankAccountRepository.findByAccountNumber("ACC1002")).thenReturn(Optional.of(toAccount));
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            Transaction result = bankService.transferMoney("ACC1001", "ACC1002",
                    new BigDecimal("1000.00"), "Fund transfer");

            assertThat(result.getTransactionType()).isEqualTo("WITHDRAWAL");
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));

            // Verify both accounts were saved (withdraw + deposit)
            verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
            verify(transactionRepository, times(2)).save(any(Transaction.class));
        }
    }
}
