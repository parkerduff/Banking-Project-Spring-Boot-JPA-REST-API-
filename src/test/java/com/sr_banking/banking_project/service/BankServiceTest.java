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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankService Unit Tests")
class BankServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BankService bankService;

    private BankAccount sampleAccount;
    private static final String ACCOUNT_NUMBER = "ACC1001";
    private static final String HOLDER_NAME = "John Doe";
    private static final String ACCOUNT_TYPE = "SAVINGS";

    @BeforeEach
    void setUp() {
        sampleAccount = new BankAccount(HOLDER_NAME, ACCOUNT_TYPE, new BigDecimal("1000.00"));
        sampleAccount.setId(1L);
        sampleAccount.setAccountNumber(ACCOUNT_NUMBER);
    }

    // ========================================================================
    // createAccount tests
    // ========================================================================
    @Nested
    @DisplayName("createAccount")
    class CreateAccountTests {

        @Test
        @DisplayName("creates account with valid inputs and returns saved account")
        void createsAccountWithValidInputs() {
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
                BankAccount saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            BankAccount result = bankService.createAccount(HOLDER_NAME, ACCOUNT_TYPE, new BigDecimal("500.00"));

            assertThat(result).isNotNull();
            assertThat(result.getAccountHolderName()).isEqualTo(HOLDER_NAME);
            assertThat(result.getAccountType()).isEqualTo(ACCOUNT_TYPE);
            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
            verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
        }

        @Test
        @DisplayName("creates account with zero initial deposit")
        void createsAccountWithZeroDeposit() {
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BankAccount result = bankService.createAccount(HOLDER_NAME, ACCOUNT_TYPE, BigDecimal.ZERO);

            assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(bankAccountRepository).save(any(BankAccount.class));
        }

        @Test
        @DisplayName("creates account with large initial deposit")
        void createsAccountWithLargeDeposit() {
            BigDecimal largeAmount = new BigDecimal("999999999.99");
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BankAccount result = bankService.createAccount(HOLDER_NAME, ACCOUNT_TYPE, largeAmount);

            assertThat(result.getBalance()).isEqualByComparingTo(largeAmount);
        }

        @Test
        @DisplayName("passes correct account to repository for saving")
        void passesCorrectAccountToRepository() {
            ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
            when(bankAccountRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

            bankService.createAccount("Jane Smith", "CURRENT", new BigDecimal("2500.00"));

            BankAccount captured = captor.getValue();
            assertThat(captured.getAccountHolderName()).isEqualTo("Jane Smith");
            assertThat(captured.getAccountType()).isEqualTo("CURRENT");
            assertThat(captured.getBalance()).isEqualByComparingTo(new BigDecimal("2500.00"));
            assertThat(captured.getAccountNumber()).isNotNull();
            assertThat(captured.getCreatedAt()).isNotNull();
        }
    }

    // ========================================================================
    // getAllAccounts tests
    // ========================================================================
    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccountsTests {

        @Test
        @DisplayName("returns all accounts from repository")
        void returnsAllAccounts() {
            BankAccount account2 = new BankAccount("Jane Smith", "CURRENT", new BigDecimal("2000.00"));
            List<BankAccount> accounts = List.of(sampleAccount, account2);
            when(bankAccountRepository.findAll()).thenReturn(accounts);

            List<BankAccount> result = bankService.getAllAccounts();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(accounts);
            verify(bankAccountRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("returns empty list when no accounts exist")
        void returnsEmptyListWhenNoAccounts() {
            when(bankAccountRepository.findAll()).thenReturn(Collections.emptyList());

            List<BankAccount> result = bankService.getAllAccounts();

            assertThat(result).isEmpty();
            verify(bankAccountRepository).findAll();
        }
    }

    // ========================================================================
    // getAccountByNumber tests
    // ========================================================================
    @Nested
    @DisplayName("getAccountByNumber")
    class GetAccountByNumberTests {

        @Test
        @DisplayName("returns account when found by account number")
        void returnsAccountWhenFound() {
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));

            Optional<BankAccount> result = bankService.getAccountByNumber(ACCOUNT_NUMBER);

            assertThat(result).isPresent();
            assertThat(result.get().getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
            assertThat(result.get().getAccountHolderName()).isEqualTo(HOLDER_NAME);
            verify(bankAccountRepository).findByAccountNumber(ACCOUNT_NUMBER);
        }

        @Test
        @DisplayName("returns empty optional when account not found")
        void returnsEmptyWhenNotFound() {
            when(bankAccountRepository.findByAccountNumber("NONEXISTENT"))
                    .thenReturn(Optional.empty());

            Optional<BankAccount> result = bankService.getAccountByNumber("NONEXISTENT");

            assertThat(result).isEmpty();
            verify(bankAccountRepository).findByAccountNumber("NONEXISTENT");
        }
    }

    // ========================================================================
    // depositMoney tests
    // ========================================================================
    @Nested
    @DisplayName("depositMoney")
    class DepositMoneyTests {

        @Test
        @DisplayName("deposits money and updates balance correctly")
        void depositsMoneySuccessfully() {
            BigDecimal depositAmount = new BigDecimal("500.00");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Transaction result = bankService.depositMoney(ACCOUNT_NUMBER, depositAmount, "Test deposit");

            assertThat(sampleAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(result.getAmount()).isEqualByComparingTo(depositAmount);
            assertThat(result.getDescription()).isEqualTo("Test deposit");
            verify(bankAccountRepository).save(sampleAccount);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("creates transaction record with correct details")
        void createsTransactionRecord() {
            BigDecimal depositAmount = new BigDecimal("250.00");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
            when(transactionRepository.save(txCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.depositMoney(ACCOUNT_NUMBER, depositAmount, "Salary credit");

            Transaction captured = txCaptor.getValue();
            assertThat(captured.getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(captured.getAmount()).isEqualByComparingTo(depositAmount);
            assertThat(captured.getDescription()).isEqualTo("Salary credit");
            assertThat(captured.getBankAccount()).isEqualTo(sampleAccount);
        }

        @Test
        @DisplayName("throws exception when account not found for deposit")
        void throwsWhenAccountNotFoundForDeposit() {
            when(bankAccountRepository.findByAccountNumber("INVALID"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.depositMoney("INVALID", new BigDecimal("100.00"), "desc"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: INVALID");

            verify(bankAccountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("deposits small fractional amount correctly")
        void depositsSmallFractionalAmount() {
            BigDecimal smallAmount = new BigDecimal("0.01");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.depositMoney(ACCOUNT_NUMBER, smallAmount, "Penny deposit");

            assertThat(sampleAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1000.01"));
        }

        @Test
        @DisplayName("deposits large amount correctly")
        void depositsLargeAmount() {
            BigDecimal largeAmount = new BigDecimal("999999999.99");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.depositMoney(ACCOUNT_NUMBER, largeAmount, "Large deposit");

            assertThat(sampleAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1000000999.99"));
        }
    }

    // ========================================================================
    // withdrawMoney tests
    // ========================================================================
    @Nested
    @DisplayName("withdrawMoney")
    class WithdrawMoneyTests {

        @Test
        @DisplayName("withdraws money and updates balance correctly")
        void withdrawsMoneySuccessfully() {
            BigDecimal withdrawAmount = new BigDecimal("300.00");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Transaction result = bankService.withdrawMoney(ACCOUNT_NUMBER, withdrawAmount, "ATM withdrawal");

            assertThat(sampleAccount.getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo("WITHDRAWAL");
            assertThat(result.getAmount()).isEqualByComparingTo(withdrawAmount);
            assertThat(result.getDescription()).isEqualTo("ATM withdrawal");
            verify(bankAccountRepository).save(sampleAccount);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("creates withdrawal transaction record with correct details")
        void createsWithdrawalTransactionRecord() {
            BigDecimal withdrawAmount = new BigDecimal("200.00");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
            when(transactionRepository.save(txCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.withdrawMoney(ACCOUNT_NUMBER, withdrawAmount, "Bill payment");

            Transaction captured = txCaptor.getValue();
            assertThat(captured.getTransactionType()).isEqualTo("WITHDRAWAL");
            assertThat(captured.getAmount()).isEqualByComparingTo(withdrawAmount);
            assertThat(captured.getDescription()).isEqualTo("Bill payment");
            assertThat(captured.getBankAccount()).isEqualTo(sampleAccount);
        }

        @Test
        @DisplayName("throws exception for insufficient funds")
        void throwsWhenInsufficientFunds() {
            BigDecimal overdrawAmount = new BigDecimal("1500.00");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));

            assertThatThrownBy(() -> bankService.withdrawMoney(ACCOUNT_NUMBER, overdrawAmount, "Too much"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient balance in account: " + ACCOUNT_NUMBER);

            verify(bankAccountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws exception when account not found for withdrawal")
        void throwsWhenAccountNotFoundForWithdrawal() {
            when(bankAccountRepository.findByAccountNumber("NONEXISTENT"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.withdrawMoney("NONEXISTENT", new BigDecimal("100.00"), "desc"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: NONEXISTENT");

            verify(bankAccountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("allows withdrawal of exact balance (zero remaining)")
        void allowsWithdrawalOfExactBalance() {
            BigDecimal exactBalance = new BigDecimal("1000.00");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Transaction result = bankService.withdrawMoney(ACCOUNT_NUMBER, exactBalance, "Full withdrawal");

            assertThat(sampleAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getTransactionType()).isEqualTo("WITHDRAWAL");
        }

        @Test
        @DisplayName("throws exception when withdrawal amount exceeds balance by one cent")
        void throwsWhenExceedsByOneCent() {
            BigDecimal overByPenny = new BigDecimal("1000.01");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));

            assertThatThrownBy(() -> bankService.withdrawMoney(ACCOUNT_NUMBER, overByPenny, "Over"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient balance");
        }

        @Test
        @DisplayName("withdraws small fractional amount correctly")
        void withdrawsSmallFractionalAmount() {
            BigDecimal smallAmount = new BigDecimal("0.01");
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.withdrawMoney(ACCOUNT_NUMBER, smallAmount, "Penny withdrawal");

            assertThat(sampleAccount.getBalance()).isEqualByComparingTo(new BigDecimal("999.99"));
        }
    }

    // ========================================================================
    // getAccountStatement tests
    // ========================================================================
    @Nested
    @DisplayName("getAccountStatement")
    class GetAccountStatementTests {

        @Test
        @DisplayName("returns transactions for given account number")
        void returnsTransactionsForAccount() {
            Transaction tx1 = new Transaction("DEPOSIT", new BigDecimal("500.00"), "Salary", sampleAccount);
            Transaction tx2 = new Transaction("WITHDRAWAL", new BigDecimal("100.00"), "ATM", sampleAccount);
            List<Transaction> transactions = List.of(tx1, tx2);

            when(transactionRepository.findByBankAccountAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(transactions);

            List<Transaction> result = bankService.getAccountStatement(ACCOUNT_NUMBER);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(result.get(1).getTransactionType()).isEqualTo("WITHDRAWAL");
            verify(transactionRepository).findByBankAccountAccountNumber(ACCOUNT_NUMBER);
        }

        @Test
        @DisplayName("returns empty list when no transactions exist")
        void returnsEmptyStatementWhenNoTransactions() {
            when(transactionRepository.findByBankAccountAccountNumber("ACC9999"))
                    .thenReturn(Collections.emptyList());

            List<Transaction> result = bankService.getAccountStatement("ACC9999");

            assertThat(result).isEmpty();
            verify(transactionRepository).findByBankAccountAccountNumber("ACC9999");
        }

        @Test
        @DisplayName("returns single transaction statement")
        void returnsSingleTransactionStatement() {
            Transaction tx = new Transaction("DEPOSIT", new BigDecimal("100.00"), "Initial", sampleAccount);
            when(transactionRepository.findByBankAccountAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(List.of(tx));

            List<Transaction> result = bankService.getAccountStatement(ACCOUNT_NUMBER);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    // ========================================================================
    // transferMoney tests
    // ========================================================================
    @Nested
    @DisplayName("transferMoney")
    class TransferMoneyTests {

        private static final String FROM_ACCOUNT = "ACC1001";
        private static final String TO_ACCOUNT = "ACC1002";

        private BankAccount fromAccount;
        private BankAccount toAccount;

        @BeforeEach
        void setUpTransferAccounts() {
            fromAccount = new BankAccount("Sender", "SAVINGS", new BigDecimal("5000.00"));
            fromAccount.setId(1L);
            fromAccount.setAccountNumber(FROM_ACCOUNT);

            toAccount = new BankAccount("Receiver", "CURRENT", new BigDecimal("2000.00"));
            toAccount.setId(2L);
            toAccount.setAccountNumber(TO_ACCOUNT);
        }

        @Test
        @DisplayName("transfers money between two accounts successfully")
        void transfersMoneySuccessfully() {
            BigDecimal transferAmount = new BigDecimal("1000.00");

            // First call for withdrawal (fromAccount), second for deposit (toAccount)
            when(bankAccountRepository.findByAccountNumber(FROM_ACCOUNT))
                    .thenReturn(Optional.of(fromAccount));
            when(bankAccountRepository.findByAccountNumber(TO_ACCOUNT))
                    .thenReturn(Optional.of(toAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Transaction result = bankService.transferMoney(FROM_ACCOUNT, TO_ACCOUNT, transferAmount, "Transfer");

            assertThat(fromAccount.getBalance()).isEqualByComparingTo(new BigDecimal("4000.00"));
            assertThat(toAccount.getBalance()).isEqualByComparingTo(new BigDecimal("3000.00"));
            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo("WITHDRAWAL");

            // Verify both accounts saved and two transactions created
            verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
            verify(transactionRepository, times(2)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("returns the withdrawal transaction from the transfer")
        void returnsWithdrawalTransaction() {
            BigDecimal transferAmount = new BigDecimal("500.00");
            when(bankAccountRepository.findByAccountNumber(FROM_ACCOUNT))
                    .thenReturn(Optional.of(fromAccount));
            when(bankAccountRepository.findByAccountNumber(TO_ACCOUNT))
                    .thenReturn(Optional.of(toAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Transaction result = bankService.transferMoney(FROM_ACCOUNT, TO_ACCOUNT, transferAmount, "Transfer");

            assertThat(result.getTransactionType()).isEqualTo("WITHDRAWAL");
            assertThat(result.getAmount()).isEqualByComparingTo(transferAmount);
        }

        @Test
        @DisplayName("uses correct descriptions for withdrawal and deposit legs")
        void usesCorrectTransferDescriptions() {
            BigDecimal transferAmount = new BigDecimal("200.00");
            when(bankAccountRepository.findByAccountNumber(FROM_ACCOUNT))
                    .thenReturn(Optional.of(fromAccount));
            when(bankAccountRepository.findByAccountNumber(TO_ACCOUNT))
                    .thenReturn(Optional.of(toAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
            when(transactionRepository.save(txCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.transferMoney(FROM_ACCOUNT, TO_ACCOUNT, transferAmount, "Rent payment");

            List<Transaction> captured = txCaptor.getAllValues();
            assertThat(captured).hasSize(2);

            // First transaction is the withdrawal
            assertThat(captured.get(0).getDescription()).isEqualTo("Transfer to account: " + TO_ACCOUNT);
            assertThat(captured.get(0).getTransactionType()).isEqualTo("WITHDRAWAL");

            // Second transaction is the deposit
            assertThat(captured.get(1).getDescription()).isEqualTo("Transfer from account: " + FROM_ACCOUNT);
            assertThat(captured.get(1).getTransactionType()).isEqualTo("DEPOSIT");
        }

        @Test
        @DisplayName("throws exception when source account not found")
        void throwsWhenSourceAccountNotFound() {
            when(bankAccountRepository.findByAccountNumber("INVALID_FROM"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.transferMoney("INVALID_FROM", TO_ACCOUNT,
                    new BigDecimal("100.00"), "Transfer"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: INVALID_FROM");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws exception when destination account not found")
        void throwsWhenDestinationAccountNotFound() {
            when(bankAccountRepository.findByAccountNumber(FROM_ACCOUNT))
                    .thenReturn(Optional.of(fromAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(bankAccountRepository.findByAccountNumber("INVALID_TO"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.transferMoney(FROM_ACCOUNT, "INVALID_TO",
                    new BigDecimal("100.00"), "Transfer"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found with number: INVALID_TO");
        }

        @Test
        @DisplayName("throws exception when source account has insufficient funds for transfer")
        void throwsWhenInsufficientFundsForTransfer() {
            BigDecimal overdrawAmount = new BigDecimal("10000.00");
            when(bankAccountRepository.findByAccountNumber(FROM_ACCOUNT))
                    .thenReturn(Optional.of(fromAccount));

            assertThatThrownBy(() -> bankService.transferMoney(FROM_ACCOUNT, TO_ACCOUNT,
                    overdrawAmount, "Big transfer"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient balance in account: " + FROM_ACCOUNT);

            verify(bankAccountRepository, never()).save(any());
        }

        @Test
        @DisplayName("transfers exact balance leaving source with zero")
        void transfersExactBalance() {
            BigDecimal exactBalance = new BigDecimal("5000.00");
            when(bankAccountRepository.findByAccountNumber(FROM_ACCOUNT))
                    .thenReturn(Optional.of(fromAccount));
            when(bankAccountRepository.findByAccountNumber(TO_ACCOUNT))
                    .thenReturn(Optional.of(toAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.transferMoney(FROM_ACCOUNT, TO_ACCOUNT, exactBalance, "Full transfer");

            assertThat(fromAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(toAccount.getBalance()).isEqualByComparingTo(new BigDecimal("7000.00"));
        }

        @Test
        @DisplayName("transfers small fractional amount between accounts")
        void transfersSmallFractionalAmount() {
            BigDecimal smallAmount = new BigDecimal("0.01");
            when(bankAccountRepository.findByAccountNumber(FROM_ACCOUNT))
                    .thenReturn(Optional.of(fromAccount));
            when(bankAccountRepository.findByAccountNumber(TO_ACCOUNT))
                    .thenReturn(Optional.of(toAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.transferMoney(FROM_ACCOUNT, TO_ACCOUNT, smallAmount, "Micro transfer");

            assertThat(fromAccount.getBalance()).isEqualByComparingTo(new BigDecimal("4999.99"));
            assertThat(toAccount.getBalance()).isEqualByComparingTo(new BigDecimal("2000.01"));
        }
    }

    // ========================================================================
    // Repository interaction verification tests
    // ========================================================================
    @Nested
    @DisplayName("Repository Interaction Verification")
    class RepositoryInteractionTests {

        @Test
        @DisplayName("deposit calls findByAccountNumber exactly once")
        void depositCallsFindByAccountNumberOnce() {
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.depositMoney(ACCOUNT_NUMBER, new BigDecimal("100.00"), "desc");

            verify(bankAccountRepository, times(1)).findByAccountNumber(ACCOUNT_NUMBER);
            verifyNoMoreInteractions(transactionRepository);
        }

        @Test
        @DisplayName("withdrawal calls findByAccountNumber exactly once")
        void withdrawalCallsFindByAccountNumberOnce() {
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));
            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bankService.withdrawMoney(ACCOUNT_NUMBER, new BigDecimal("100.00"), "desc");

            verify(bankAccountRepository, times(1)).findByAccountNumber(ACCOUNT_NUMBER);
            verifyNoMoreInteractions(transactionRepository);
        }

        @Test
        @DisplayName("failed deposit does not save account or transaction")
        void failedDepositNoSaves() {
            when(bankAccountRepository.findByAccountNumber("BAD"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankService.depositMoney("BAD", new BigDecimal("100.00"), "desc"))
                    .isInstanceOf(RuntimeException.class);

            verify(bankAccountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("failed withdrawal due to insufficient funds does not save")
        void failedWithdrawalInsufficientFundsNoSaves() {
            when(bankAccountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(sampleAccount));

            assertThatThrownBy(() -> bankService.withdrawMoney(ACCOUNT_NUMBER, new BigDecimal("9999.00"), "desc"))
                    .isInstanceOf(RuntimeException.class);

            verify(bankAccountRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }
    }
}
