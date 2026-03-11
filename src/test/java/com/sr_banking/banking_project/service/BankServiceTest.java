package com.sr_banking.banking_project.service;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.repository.BankAccountRepository;
import com.sr_banking.banking_project.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BankService bankService;

    private BankAccount buildAccount(String name, String type, BigDecimal balance) {
        BankAccount account = new BankAccount(name, type, balance);
        account.setId(1L);
        return account;
    }

    // =========================================================================
    // createAccount
    // =========================================================================

    @Nested
    @DisplayName("createAccount")
    class CreateAccountTests {

        @Test
        @DisplayName("saves and returns new account")
        void createAccount_happyPath() {
            BankAccount saved = buildAccount("Alice", "SAVINGS", new BigDecimal("1000"));
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(saved);

            BankAccount result = bankService.createAccount("Alice", "SAVINGS", new BigDecimal("1000"));

            assertThat(result.getAccountHolderName()).isEqualTo("Alice");
            assertThat(result.getAccountType()).isEqualTo("SAVINGS");
            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1000"));
            verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
        }
    }

    // =========================================================================
    // getAllAccounts
    // =========================================================================

    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccountsTests {

        @Test
        @DisplayName("returns all accounts")
        void getAllAccounts_returnsList() {
            BankAccount a1 = buildAccount("Alice", "SAVINGS", new BigDecimal("1000"));
            BankAccount a2 = buildAccount("Bob", "CHECKING", new BigDecimal("2000"));
            when(bankAccountRepository.findAll()).thenReturn(List.of(a1, a2));

            List<BankAccount> result = bankService.getAllAccounts();

            assertThat(result).hasSize(2);
            verify(bankAccountRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("returns empty list when no accounts")
        void getAllAccounts_empty() {
            when(bankAccountRepository.findAll()).thenReturn(Collections.emptyList());

            List<BankAccount> result = bankService.getAllAccounts();

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // getAccountByNumber
    // =========================================================================

    @Nested
    @DisplayName("getAccountByNumber")
    class GetAccountByNumberTests {

        @Test
        @DisplayName("returns account when found")
        void getAccountByNumber_found() {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("1000"));
            when(bankAccountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(account));

            Optional<BankAccount> result = bankService.getAccountByNumber("ACC123");

            assertThat(result).isPresent();
            assertThat(result.get().getAccountHolderName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("returns empty when not found")
        void getAccountByNumber_notFound() {
            when(bankAccountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

            Optional<BankAccount> result = bankService.getAccountByNumber("INVALID");

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // depositMoney
    // =========================================================================

    @Nested
    @DisplayName("depositMoney")
    class DepositMoneyTests {

        @Test
        @DisplayName("deposits money and updates balance")
        void deposit_happyPath() {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("1000"));
            when(bankAccountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(account));
            when(bankAccountRepository.save(any())).thenReturn(account);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            Transaction result = bankService.depositMoney("ACC123", new BigDecimal("500"), "Test deposit");

            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1500"));
            assertThat(result.getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500"));
            verify(bankAccountRepository).save(account);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws when account not found")
        void deposit_accountNotFound() {
            when(bankAccountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.depositMoney("INVALID", new BigDecimal("100"), "desc"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: INVALID");
        }
    }

    // =========================================================================
    // withdrawMoney
    // =========================================================================

    @Nested
    @DisplayName("withdrawMoney")
    class WithdrawMoneyTests {

        @Test
        @DisplayName("withdraws money and updates balance")
        void withdraw_happyPath() {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("1000"));
            when(bankAccountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(account));
            when(bankAccountRepository.save(any())).thenReturn(account);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            Transaction result = bankService.withdrawMoney("ACC123", new BigDecimal("300"), "Test withdrawal");

            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("700"));
            assertThat(result.getTransactionType()).isEqualTo("WITHDRAWAL");
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("300"));
            verify(bankAccountRepository).save(account);
        }

        @Test
        @DisplayName("throws when insufficient balance")
        void withdraw_insufficientBalance() {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("100"));
            when(bankAccountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> bankService.withdrawMoney("ACC123", new BigDecimal("500"), "desc"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient balance in account: ACC123");
        }

        @Test
        @DisplayName("throws when account not found")
        void withdraw_accountNotFound() {
            when(bankAccountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.withdrawMoney("INVALID", new BigDecimal("100"), "desc"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: INVALID");
        }

        @Test
        @DisplayName("allows withdrawal of exact balance")
        void withdraw_exactBalance() {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("500"));
            when(bankAccountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(account));
            when(bankAccountRepository.save(any())).thenReturn(account);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            Transaction result = bankService.withdrawMoney("ACC123", new BigDecimal("500"), "Exact withdrawal");

            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // =========================================================================
    // getAccountStatement
    // =========================================================================

    @Nested
    @DisplayName("getAccountStatement")
    class GetAccountStatementTests {

        @Test
        @DisplayName("returns transactions for account")
        void getStatement_happyPath() {
            BankAccount account = buildAccount("Alice", "SAVINGS", new BigDecimal("1000"));
            Transaction t1 = new Transaction("DEPOSIT", new BigDecimal("500"), "Deposit", account);
            Transaction t2 = new Transaction("WITHDRAWAL", new BigDecimal("100"), "Withdrawal", account);
            when(transactionRepository.findByBankAccountAccountNumber("ACC123")).thenReturn(List.of(t1, t2));

            List<Transaction> result = bankService.getAccountStatement("ACC123");

            assertThat(result).hasSize(2);
            verify(transactionRepository).findByBankAccountAccountNumber("ACC123");
        }

        @Test
        @DisplayName("returns empty list for no transactions")
        void getStatement_empty() {
            when(transactionRepository.findByBankAccountAccountNumber("ACC123")).thenReturn(Collections.emptyList());

            List<Transaction> result = bankService.getAccountStatement("ACC123");

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // transferMoney
    // =========================================================================

    @Nested
    @DisplayName("transferMoney")
    class TransferMoneyTests {

        @Test
        @DisplayName("transfers money between accounts")
        void transfer_happyPath() {
            BankAccount from = buildAccount("Alice", "SAVINGS", new BigDecimal("1000"));
            from.setAccountNumber("ACC001");
            BankAccount to = buildAccount("Bob", "CHECKING", new BigDecimal("500"));
            to.setId(2L);
            to.setAccountNumber("ACC002");

            when(bankAccountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(from));
            when(bankAccountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(to));
            when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            Transaction result = bankService.transferMoney("ACC001", "ACC002", new BigDecimal("300"), "Transfer");

            assertThat(from.getBalance()).isEqualByComparingTo(new BigDecimal("700"));
            assertThat(to.getBalance()).isEqualByComparingTo(new BigDecimal("800"));
            assertThat(result.getTransactionType()).isEqualTo("WITHDRAWAL");
        }

        @Test
        @DisplayName("throws when source account not found")
        void transfer_sourceNotFound() {
            when(bankAccountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.transferMoney("INVALID", "ACC002", new BigDecimal("100"), "desc"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        @DisplayName("throws when insufficient balance in source")
        void transfer_insufficientBalance() {
            BankAccount from = buildAccount("Alice", "SAVINGS", new BigDecimal("50"));
            from.setAccountNumber("ACC001");
            when(bankAccountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(from));

            assertThatThrownBy(() -> bankService.transferMoney("ACC001", "ACC002", new BigDecimal("100"), "desc"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient balance");
        }
    }
}
