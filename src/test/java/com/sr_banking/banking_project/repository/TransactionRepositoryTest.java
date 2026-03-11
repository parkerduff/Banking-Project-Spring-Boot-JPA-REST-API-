package com.sr_banking.banking_project.repository;

import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private BankAccount accountWithTransactions;
    private BankAccount accountWithNoTransactions;
    private BankAccount accountWithSingleTransaction;

    @BeforeEach
    void setUp() {
        // Account with multiple transactions
        accountWithTransactions = new BankAccount();
        accountWithTransactions.setAccountNumber("ACC1001");
        accountWithTransactions.setAccountHolderName("Rahul Sharma");
        accountWithTransactions.setAccountType("SAVINGS");
        accountWithTransactions.setBalance(new BigDecimal("5000.00"));
        entityManager.persistAndFlush(accountWithTransactions);

        // Account with no transactions
        accountWithNoTransactions = new BankAccount();
        accountWithNoTransactions.setAccountNumber("ACC1002");
        accountWithNoTransactions.setAccountHolderName("Priya Patel");
        accountWithNoTransactions.setAccountType("CURRENT");
        accountWithNoTransactions.setBalance(new BigDecimal("10000.00"));
        entityManager.persistAndFlush(accountWithNoTransactions);

        // Account with a single transaction
        accountWithSingleTransaction = new BankAccount();
        accountWithSingleTransaction.setAccountNumber("ACC1003");
        accountWithSingleTransaction.setAccountHolderName("Amit Verma");
        accountWithSingleTransaction.setAccountType("SAVINGS");
        accountWithSingleTransaction.setBalance(new BigDecimal("7500.00"));
        entityManager.persistAndFlush(accountWithSingleTransaction);

        // Transactions for accountWithTransactions (ACC1001)
        Transaction txn1 = new Transaction();
        txn1.setTransactionId("TXN1001");
        txn1.setTransactionType("DEPOSIT");
        txn1.setAmount(new BigDecimal("2000.00"));
        txn1.setDescription("Initial deposit");
        txn1.setBankAccount(accountWithTransactions);
        entityManager.persist(txn1);

        Transaction txn2 = new Transaction();
        txn2.setTransactionId("TXN1002");
        txn2.setTransactionType("DEPOSIT");
        txn2.setAmount(new BigDecimal("3000.00"));
        txn2.setDescription("Salary credit");
        txn2.setBankAccount(accountWithTransactions);
        entityManager.persist(txn2);

        Transaction txn3 = new Transaction();
        txn3.setTransactionId("TXN1003");
        txn3.setTransactionType("WITHDRAWAL");
        txn3.setAmount(new BigDecimal("1000.00"));
        txn3.setDescription("ATM withdrawal");
        txn3.setBankAccount(accountWithTransactions);
        entityManager.persist(txn3);

        // Single transaction for accountWithSingleTransaction (ACC1003)
        Transaction txn4 = new Transaction();
        txn4.setTransactionId("TXN1004");
        txn4.setTransactionType("DEPOSIT");
        txn4.setAmount(new BigDecimal("4500.00"));
        txn4.setDescription("Freelancing payment");
        txn4.setBankAccount(accountWithSingleTransaction);
        entityManager.persist(txn4);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findByBankAccountAccountNumber")
    class FindByBankAccountAccountNumber {

        @Test
        @DisplayName("returns all transactions for a valid account with multiple transactions")
        void returnsMultipleTransactionsForValidAccount() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");

            assertThat(transactions)
                    .hasSize(3)
                    .extracting(Transaction::getTransactionId)
                    .containsExactlyInAnyOrder("TXN1001", "TXN1002", "TXN1003");
        }

        @Test
        @DisplayName("returns correct transaction types for account with mixed transactions")
        void returnsCorrectTransactionTypes() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");

            assertThat(transactions)
                    .extracting(Transaction::getTransactionType)
                    .containsExactlyInAnyOrder("DEPOSIT", "DEPOSIT", "WITHDRAWAL");
        }

        @Test
        @DisplayName("returns correct amounts for each transaction")
        void returnsCorrectAmounts() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");

            assertThat(transactions)
                    .extracting(Transaction::getAmount)
                    .containsExactlyInAnyOrder(
                            new BigDecimal("2000.00"),
                            new BigDecimal("3000.00"),
                            new BigDecimal("1000.00")
                    );
        }

        @Test
        @DisplayName("returns correct descriptions for each transaction")
        void returnsCorrectDescriptions() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");

            assertThat(transactions)
                    .extracting(Transaction::getDescription)
                    .containsExactlyInAnyOrder(
                            "Initial deposit",
                            "Salary credit",
                            "ATM withdrawal"
                    );
        }

        @Test
        @DisplayName("returns empty list for account with no transactions")
        void returnsEmptyListForAccountWithNoTransactions() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1002");

            assertThat(transactions).isEmpty();
        }

        @Test
        @DisplayName("returns single transaction for account with one transaction")
        void returnsSingleTransaction() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1003");

            assertThat(transactions).hasSize(1);
            Transaction txn = transactions.get(0);
            assertThat(txn.getTransactionId()).isEqualTo("TXN1004");
            assertThat(txn.getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(txn.getAmount()).isEqualByComparingTo(new BigDecimal("4500.00"));
            assertThat(txn.getDescription()).isEqualTo("Freelancing payment");
        }

        @Test
        @DisplayName("returns empty list for non-existent account number")
        void returnsEmptyListForNonExistentAccount() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("NONEXISTENT");

            assertThat(transactions).isEmpty();
        }

        @Test
        @DisplayName("returns empty list for empty string account number")
        void returnsEmptyListForEmptyAccountNumber() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("");

            assertThat(transactions).isEmpty();
        }

        @Test
        @DisplayName("returns empty list for null account number")
        void returnsEmptyListForNullAccountNumber() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber(null);

            assertThat(transactions).isEmpty();
        }

        @Test
        @DisplayName("each returned transaction has correct bank account association")
        void transactionsHaveCorrectBankAccountAssociation() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");

            assertThat(transactions)
                    .allSatisfy(txn -> {
                        assertThat(txn.getBankAccount()).isNotNull();
                        assertThat(txn.getBankAccount().getAccountNumber()).isEqualTo("ACC1001");
                        assertThat(txn.getBankAccount().getAccountHolderName()).isEqualTo("Rahul Sharma");
                    });
        }

        @Test
        @DisplayName("does not return transactions from other accounts")
        void doesNotReturnTransactionsFromOtherAccounts() {
            List<Transaction> transactionsAcc1 =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");
            List<Transaction> transactionsAcc3 =
                    transactionRepository.findByBankAccountAccountNumber("ACC1003");

            // ACC1001 has 3 transactions, ACC1003 has 1
            assertThat(transactionsAcc1).hasSize(3);
            assertThat(transactionsAcc3).hasSize(1);

            // No overlap in transaction IDs
            List<String> acc1TxnIds = transactionsAcc1.stream()
                    .map(Transaction::getTransactionId)
                    .toList();
            List<String> acc3TxnIds = transactionsAcc3.stream()
                    .map(Transaction::getTransactionId)
                    .toList();

            assertThat(acc1TxnIds).doesNotContainAnyElementsOf(acc3TxnIds);
        }

        @Test
        @DisplayName("account number search is case-sensitive")
        void accountNumberSearchIsCaseSensitive() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("acc1001");

            assertThat(transactions).isEmpty();
        }

        @Test
        @DisplayName("returns transactions with non-null transaction dates")
        void transactionsHaveNonNullDates() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");

            assertThat(transactions)
                    .allSatisfy(txn ->
                            assertThat(txn.getTransactionDate()).isNotNull()
                    );
        }

        @Test
        @DisplayName("returns transactions with non-null IDs after persistence")
        void transactionsHaveNonNullIds() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");

            assertThat(transactions)
                    .allSatisfy(txn ->
                            assertThat(txn.getId()).isNotNull()
                    );
        }
    }

    @Nested
    @DisplayName("JpaRepository inherited methods")
    class JpaRepositoryMethods {

        @Test
        @DisplayName("findAll returns all transactions across all accounts")
        void findAllReturnsAllTransactions() {
            List<Transaction> allTransactions = transactionRepository.findAll();

            assertThat(allTransactions).hasSize(4);
        }

        @Test
        @DisplayName("findById returns correct transaction")
        void findByIdReturnsCorrectTransaction() {
            // First get all and pick one
            List<Transaction> allTransactions = transactionRepository.findAll();
            Transaction expected = allTransactions.stream()
                    .filter(t -> "TXN1001".equals(t.getTransactionId()))
                    .findFirst()
                    .orElseThrow();

            Optional<Transaction> found = transactionRepository.findById(expected.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTransactionId()).isEqualTo("TXN1001");
            assertThat(found.get().getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        }

        @Test
        @DisplayName("findById returns empty for non-existent ID")
        void findByIdReturnsEmptyForNonExistentId() {
            Optional<Transaction> found = transactionRepository.findById(99999L);

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("save persists a new transaction")
        void savePersistsNewTransaction() {
            Transaction newTxn = new Transaction();
            newTxn.setTransactionId("TXN9999");
            newTxn.setTransactionType("DEPOSIT");
            newTxn.setAmount(new BigDecimal("500.00"));
            newTxn.setDescription("Test deposit");
            newTxn.setBankAccount(accountWithNoTransactions);

            Transaction saved = transactionRepository.save(newTxn);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTransactionId()).isEqualTo("TXN9999");

            // Verify it appears in the account's transactions
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1002");
            assertThat(transactions).hasSize(1);
            assertThat(transactions.get(0).getTransactionId()).isEqualTo("TXN9999");
        }

        @Test
        @DisplayName("delete removes a transaction")
        void deleteRemovesTransaction() {
            List<Transaction> before =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");
            assertThat(before).hasSize(3);

            Transaction toDelete = before.get(0);
            transactionRepository.delete(toDelete);
            entityManager.flush();

            List<Transaction> after =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");
            assertThat(after).hasSize(2);
            assertThat(after).extracting(Transaction::getTransactionId)
                    .doesNotContain(toDelete.getTransactionId());
        }

        @Test
        @DisplayName("count returns total number of transactions")
        void countReturnsCorrectTotal() {
            long count = transactionRepository.count();

            assertThat(count).isEqualTo(4);
        }

        @Test
        @DisplayName("existsById returns true for existing transaction")
        void existsByIdReturnsTrueForExisting() {
            List<Transaction> all = transactionRepository.findAll();
            Long existingId = all.get(0).getId();

            assertThat(transactionRepository.existsById(existingId)).isTrue();
        }

        @Test
        @DisplayName("existsById returns false for non-existent transaction")
        void existsByIdReturnsFalseForNonExistent() {
            assertThat(transactionRepository.existsById(99999L)).isFalse();
        }

        @Test
        @DisplayName("deleteById removes a transaction by ID")
        void deleteByIdRemovesTransaction() {
            List<Transaction> all = transactionRepository.findAll();
            Long idToDelete = all.get(0).getId();

            transactionRepository.deleteById(idToDelete);
            entityManager.flush();

            assertThat(transactionRepository.findById(idToDelete)).isEmpty();
            assertThat(transactionRepository.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("saveAll persists multiple transactions")
        void saveAllPersistsMultipleTransactions() {
            Transaction txnA = new Transaction();
            txnA.setTransactionId("TXN_A");
            txnA.setTransactionType("DEPOSIT");
            txnA.setAmount(new BigDecimal("100.00"));
            txnA.setDescription("Batch deposit A");
            txnA.setBankAccount(accountWithNoTransactions);

            Transaction txnB = new Transaction();
            txnB.setTransactionId("TXN_B");
            txnB.setTransactionType("WITHDRAWAL");
            txnB.setAmount(new BigDecimal("50.00"));
            txnB.setDescription("Batch withdrawal B");
            txnB.setBankAccount(accountWithNoTransactions);

            List<Transaction> saved = transactionRepository.saveAll(List.of(txnA, txnB));

            assertThat(saved).hasSize(2);
            assertThat(saved).allSatisfy(t -> assertThat(t.getId()).isNotNull());

            List<Transaction> acc2Txns =
                    transactionRepository.findByBankAccountAccountNumber("ACC1002");
            assertThat(acc2Txns).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Transaction entity field validation")
    class TransactionEntityTests {

        @Test
        @DisplayName("transaction toString contains expected fields")
        void toStringContainsExpectedFields() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");
            Transaction txn = transactions.stream()
                    .filter(t -> "TXN1001".equals(t.getTransactionId()))
                    .findFirst()
                    .orElseThrow();

            String str = txn.toString();
            assertThat(str).contains("DEPOSIT");
            assertThat(str).contains("2000.00");
            assertThat(str).contains("Initial deposit");
        }

        @Test
        @DisplayName("bank account toString contains expected fields")
        void bankAccountToStringContainsExpectedFields() {
            List<Transaction> transactions =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");
            BankAccount account = transactions.get(0).getBankAccount();

            String str = account.toString();
            assertThat(str).contains("ACC1001");
            assertThat(str).contains("Rahul Sharma");
            assertThat(str).contains("5000.00");
        }

        @Test
        @DisplayName("transaction default constructor sets transactionDate")
        void defaultConstructorSetsDate() {
            Transaction txn = new Transaction();
            assertThat(txn.getTransactionDate()).isNotNull();
        }

        @Test
        @DisplayName("transaction parameterized constructor sets all fields")
        void parameterizedConstructorSetsAllFields() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC_TEST");
            account.setAccountHolderName("Test User");
            account.setAccountType("SAVINGS");
            account.setBalance(BigDecimal.ZERO);

            Transaction txn = new Transaction("DEPOSIT", new BigDecimal("100.00"),
                    "Test deposit", account);

            assertThat(txn.getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(txn.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(txn.getDescription()).isEqualTo("Test deposit");
            assertThat(txn.getBankAccount()).isEqualTo(account);
            assertThat(txn.getTransactionDate()).isNotNull();
            assertThat(txn.getTransactionId()).startsWith("TXN");
        }

        @Test
        @DisplayName("bank account default constructor sets balance to zero and createdAt")
        void bankAccountDefaultConstructor() {
            BankAccount account = new BankAccount();
            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(account.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("bank account parameterized constructor sets all fields")
        void bankAccountParameterizedConstructor() {
            BankAccount account = new BankAccount("Test User", "CURRENT",
                    new BigDecimal("5000.00"));

            assertThat(account.getAccountHolderName()).isEqualTo("Test User");
            assertThat(account.getAccountType()).isEqualTo("CURRENT");
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(account.getCreatedAt()).isNotNull();
            assertThat(account.getAccountNumber()).startsWith("ACC");
        }

        @Test
        @DisplayName("transaction setter methods update fields correctly")
        void transactionSettersWork() {
            Transaction txn = new Transaction();
            txn.setId(1L);
            txn.setTransactionId("TXN_SET");
            txn.setTransactionType("WITHDRAWAL");
            txn.setAmount(new BigDecimal("250.00"));
            txn.setDescription("Setter test");

            assertThat(txn.getId()).isEqualTo(1L);
            assertThat(txn.getTransactionId()).isEqualTo("TXN_SET");
            assertThat(txn.getTransactionType()).isEqualTo("WITHDRAWAL");
            assertThat(txn.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
            assertThat(txn.getDescription()).isEqualTo("Setter test");
        }

        @Test
        @DisplayName("bank account setter methods update fields correctly")
        void bankAccountSettersWork() {
            BankAccount account = new BankAccount();
            account.setId(1L);
            account.setAccountNumber("ACC_SET");
            account.setAccountHolderName("Setter User");
            account.setAccountType("SAVINGS");
            account.setBalance(new BigDecimal("999.99"));

            assertThat(account.getId()).isEqualTo(1L);
            assertThat(account.getAccountNumber()).isEqualTo("ACC_SET");
            assertThat(account.getAccountHolderName()).isEqualTo("Setter User");
            assertThat(account.getAccountType()).isEqualTo("SAVINGS");
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("999.99"));
        }

        @Test
        @DisplayName("transaction setTransactionDate updates the date")
        void setTransactionDateWorks() {
            Transaction txn = new Transaction();
            java.time.LocalDateTime customDate =
                    java.time.LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            txn.setTransactionDate(customDate);

            assertThat(txn.getTransactionDate()).isEqualTo(customDate);
        }

        @Test
        @DisplayName("bank account setCreatedAt updates the date")
        void setCreatedAtWorks() {
            BankAccount account = new BankAccount();
            java.time.LocalDateTime customDate =
                    java.time.LocalDateTime.of(2024, 1, 1, 0, 0, 0);
            account.setCreatedAt(customDate);

            assertThat(account.getCreatedAt()).isEqualTo(customDate);
        }
    }

    @Nested
    @DisplayName("Edge cases and special characters")
    class EdgeCases {

        @Test
        @DisplayName("handles account number with special characters")
        void handlesSpecialCharactersInAccountNumber() {
            BankAccount specialAccount = new BankAccount();
            specialAccount.setAccountNumber("ACC-1001/TEST");
            specialAccount.setAccountHolderName("Special Chars");
            specialAccount.setAccountType("SAVINGS");
            specialAccount.setBalance(BigDecimal.ZERO);
            entityManager.persistAndFlush(specialAccount);

            Transaction txn = new Transaction();
            txn.setTransactionId("TXN_SPECIAL");
            txn.setTransactionType("DEPOSIT");
            txn.setAmount(new BigDecimal("100.00"));
            txn.setDescription("Special account deposit");
            txn.setBankAccount(specialAccount);
            entityManager.persistAndFlush(txn);
            entityManager.clear();

            List<Transaction> result =
                    transactionRepository.findByBankAccountAccountNumber("ACC-1001/TEST");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTransactionId()).isEqualTo("TXN_SPECIAL");
        }

        @Test
        @DisplayName("handles unicode characters in account number")
        void handlesUnicodeInAccountNumber() {
            BankAccount unicodeAccount = new BankAccount();
            unicodeAccount.setAccountNumber("ACC\u00E9\u00E0\u00FC");
            unicodeAccount.setAccountHolderName("Unicode User");
            unicodeAccount.setAccountType("SAVINGS");
            unicodeAccount.setBalance(BigDecimal.ZERO);
            entityManager.persistAndFlush(unicodeAccount);

            Transaction txn = new Transaction();
            txn.setTransactionId("TXN_UNICODE");
            txn.setTransactionType("DEPOSIT");
            txn.setAmount(new BigDecimal("200.00"));
            txn.setDescription("Unicode deposit");
            txn.setBankAccount(unicodeAccount);
            entityManager.persistAndFlush(txn);
            entityManager.clear();

            List<Transaction> result =
                    transactionRepository.findByBankAccountAccountNumber("ACC\u00E9\u00E0\u00FC");
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("handles very long account number")
        void handlesLongAccountNumber() {
            String longAccountNum = "ACC" + "X".repeat(200);
            BankAccount longAccount = new BankAccount();
            longAccount.setAccountNumber(longAccountNum);
            longAccount.setAccountHolderName("Long Number User");
            longAccount.setAccountType("SAVINGS");
            longAccount.setBalance(BigDecimal.ZERO);
            entityManager.persistAndFlush(longAccount);

            Transaction txn = new Transaction();
            txn.setTransactionId("TXN_LONG");
            txn.setTransactionType("DEPOSIT");
            txn.setAmount(new BigDecimal("300.00"));
            txn.setDescription("Long account number deposit");
            txn.setBankAccount(longAccount);
            entityManager.persistAndFlush(txn);
            entityManager.clear();

            List<Transaction> result =
                    transactionRepository.findByBankAccountAccountNumber(longAccountNum);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("handles whitespace-only account number returning empty results")
        void handlesWhitespaceAccountNumber() {
            List<Transaction> result =
                    transactionRepository.findByBankAccountAccountNumber("   ");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("transaction with zero amount is persisted and retrievable")
        void handlesZeroAmount() {
            Transaction txn = new Transaction();
            txn.setTransactionId("TXN_ZERO");
            txn.setTransactionType("DEPOSIT");
            txn.setAmount(BigDecimal.ZERO);
            txn.setDescription("Zero amount");
            txn.setBankAccount(accountWithNoTransactions);
            entityManager.persistAndFlush(txn);
            entityManager.clear();

            List<Transaction> result =
                    transactionRepository.findByBankAccountAccountNumber("ACC1002");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("transaction with large amount is persisted and retrievable")
        void handlesLargeAmount() {
            Transaction txn = new Transaction();
            txn.setTransactionId("TXN_LARGE");
            txn.setTransactionType("DEPOSIT");
            txn.setAmount(new BigDecimal("99999999999.99"));
            txn.setDescription("Large amount");
            txn.setBankAccount(accountWithNoTransactions);
            entityManager.persistAndFlush(txn);
            entityManager.clear();

            List<Transaction> result =
                    transactionRepository.findByBankAccountAccountNumber("ACC1002");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAmount())
                    .isEqualByComparingTo(new BigDecimal("99999999999.99"));
        }

        @Test
        @DisplayName("multiple queries return consistent results")
        void multipleQueriesReturnConsistentResults() {
            List<Transaction> first =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");
            List<Transaction> second =
                    transactionRepository.findByBankAccountAccountNumber("ACC1001");

            assertThat(first).hasSameSizeAs(second);
            assertThat(first).extracting(Transaction::getTransactionId)
                    .containsExactlyInAnyOrderElementsOf(
                            second.stream().map(Transaction::getTransactionId).toList()
                    );
        }
    }
}
