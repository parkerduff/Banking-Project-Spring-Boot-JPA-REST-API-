package com.sr_banking.banking_project.repository;

import com.sr_banking.banking_project.model.BankAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false"
})
@DisplayName("BankAccountRepository")
class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private BankAccount savedAccount;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        BankAccount account = new BankAccount();
        account.setAccountNumber("ACC1001");
        account.setAccountHolderName("Rahul Sharma");
        account.setAccountType("SAVINGS");
        account.setBalance(new BigDecimal("5000.00"));
        account.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        savedAccount = entityManager.persistAndFlush(account);
        entityManager.clear();
    }

    @Nested
    @DisplayName("findByAccountNumber")
    class FindByAccountNumber {

        @Test
        @DisplayName("returns account when valid account number exists")
        void returnsAccountWhenValidAccountNumberExists() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber("ACC1001");

            assertThat(result).isPresent();
            assertThat(result.get().getAccountNumber()).isEqualTo("ACC1001");
            assertThat(result.get().getAccountHolderName()).isEqualTo("Rahul Sharma");
            assertThat(result.get().getAccountType()).isEqualTo("SAVINGS");
            assertThat(result.get().getBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
        }

        @Test
        @DisplayName("returns empty optional for non-existent account number")
        void returnsEmptyForNonExistentAccountNumber() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber("ACC9999");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty optional for null account number")
        void returnsEmptyForNullAccountNumber() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty optional for empty string account number")
        void returnsEmptyForEmptyStringAccountNumber() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber("");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns correct account among multiple accounts")
        void returnsCorrectAccountAmongMultiple() {
            BankAccount account2 = new BankAccount();
            account2.setAccountNumber("ACC1002");
            account2.setAccountHolderName("Priya Patel");
            account2.setAccountType("CURRENT");
            account2.setBalance(new BigDecimal("10000.00"));
            account2.setCreatedAt(LocalDateTime.of(2024, 1, 2, 11, 30, 0));
            entityManager.persistAndFlush(account2);

            BankAccount account3 = new BankAccount();
            account3.setAccountNumber("ACC1003");
            account3.setAccountHolderName("Amit Verma");
            account3.setAccountType("SAVINGS");
            account3.setBalance(new BigDecimal("7500.00"));
            account3.setCreatedAt(LocalDateTime.of(2024, 1, 3, 9, 15, 0));
            entityManager.persistAndFlush(account3);
            entityManager.clear();

            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber("ACC1002");

            assertThat(result).isPresent();
            assertThat(result.get().getAccountHolderName()).isEqualTo("Priya Patel");
            assertThat(result.get().getAccountType()).isEqualTo("CURRENT");
        }

        @Test
        @DisplayName("is case-sensitive for account number lookup")
        void isCaseSensitiveForAccountNumber() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber("acc1001");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty for account number with leading/trailing whitespace")
        void returnsEmptyForAccountNumberWithWhitespace() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber(" ACC1001 ");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("handles special characters in account number search")
        void handlesSpecialCharactersInAccountNumber() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber("ACC!@#$");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("handles unicode characters in account number search")
        void handlesUnicodeCharactersInAccountNumber() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber("ACC\u00E9\u00E8");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("handles very long account number search")
        void handlesVeryLongAccountNumber() {
            String longNumber = "ACC" + "X".repeat(500);
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber(longNumber);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("CRUD Operations - save")
    class SaveOperations {

        @Test
        @DisplayName("saves a new bank account with all fields")
        void savesNewBankAccountWithAllFields() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC2001");
            account.setAccountHolderName("Test User");
            account.setAccountType("SAVINGS");
            account.setBalance(new BigDecimal("1000.00"));
            account.setCreatedAt(LocalDateTime.of(2024, 6, 1, 12, 0, 0));

            BankAccount saved = bankAccountRepository.save(account);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getAccountNumber()).isEqualTo("ACC2001");
            assertThat(saved.getAccountHolderName()).isEqualTo("Test User");
            assertThat(saved.getAccountType()).isEqualTo("SAVINGS");
            assertThat(saved.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(saved.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 6, 1, 12, 0, 0));
        }

        @Test
        @DisplayName("saves bank account using parameterized constructor")
        void savesBankAccountUsingParameterizedConstructor() {
            BankAccount account = new BankAccount("Constructor User", "CURRENT", new BigDecimal("2500.00"));

            BankAccount saved = bankAccountRepository.save(account);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getAccountHolderName()).isEqualTo("Constructor User");
            assertThat(saved.getAccountType()).isEqualTo("CURRENT");
            assertThat(saved.getBalance()).isEqualByComparingTo(new BigDecimal("2500.00"));
            assertThat(saved.getAccountNumber()).startsWith("ACC");
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("saves bank account with zero balance")
        void savesBankAccountWithZeroBalance() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC2002");
            account.setAccountHolderName("Zero Balance User");
            account.setAccountType("SAVINGS");
            account.setBalance(BigDecimal.ZERO);
            account.setCreatedAt(LocalDateTime.now());

            BankAccount saved = bankAccountRepository.save(account);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("saves bank account with large balance")
        void savesBankAccountWithLargeBalance() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC2003");
            account.setAccountHolderName("Rich User");
            account.setAccountType("SAVINGS");
            account.setBalance(new BigDecimal("9999999999.99"));
            account.setCreatedAt(LocalDateTime.now());

            BankAccount saved = bankAccountRepository.save(account);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getBalance()).isEqualByComparingTo(new BigDecimal("9999999999.99"));
        }

        @Test
        @DisplayName("enforces unique constraint on account number")
        void enforcesUniqueConstraintOnAccountNumber() {
            BankAccount duplicate = new BankAccount();
            duplicate.setAccountNumber("ACC1001");
            duplicate.setAccountHolderName("Duplicate User");
            duplicate.setAccountType("SAVINGS");
            duplicate.setBalance(new BigDecimal("100.00"));
            duplicate.setCreatedAt(LocalDateTime.now());

            assertThatThrownBy(() -> {
                bankAccountRepository.saveAndFlush(duplicate);
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("saves bank account with default constructor defaults")
        void savesBankAccountWithDefaultConstructorDefaults() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC2004");

            BankAccount saved = bankAccountRepository.save(account);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(saved.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("CRUD Operations - findById")
    class FindByIdOperations {

        @Test
        @DisplayName("finds account by existing ID")
        void findsAccountByExistingId() {
            Optional<BankAccount> result = bankAccountRepository.findById(savedAccount.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getAccountNumber()).isEqualTo("ACC1001");
        }

        @Test
        @DisplayName("returns empty for non-existent ID")
        void returnsEmptyForNonExistentId() {
            Optional<BankAccount> result = bankAccountRepository.findById(99999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("CRUD Operations - findAll")
    class FindAllOperations {

        @Test
        @DisplayName("returns all accounts")
        void returnsAllAccounts() {
            BankAccount account2 = new BankAccount();
            account2.setAccountNumber("ACC1002");
            account2.setAccountHolderName("Priya Patel");
            account2.setAccountType("CURRENT");
            account2.setBalance(new BigDecimal("10000.00"));
            account2.setCreatedAt(LocalDateTime.now());
            entityManager.persistAndFlush(account2);
            entityManager.clear();

            List<BankAccount> accounts = bankAccountRepository.findAll();

            assertThat(accounts).hasSize(2);
        }

        @Test
        @DisplayName("returns empty list when no accounts exist")
        void returnsEmptyListWhenNoAccountsExist() {
            bankAccountRepository.deleteAll();
            entityManager.flush();
            entityManager.clear();

            List<BankAccount> accounts = bankAccountRepository.findAll();

            assertThat(accounts).isEmpty();
        }
    }

    @Nested
    @DisplayName("CRUD Operations - update")
    class UpdateOperations {

        @Test
        @DisplayName("updates account holder name")
        void updatesAccountHolderName() {
            savedAccount = bankAccountRepository.findById(savedAccount.getId()).orElseThrow();
            savedAccount.setAccountHolderName("Updated Name");
            bankAccountRepository.saveAndFlush(savedAccount);
            entityManager.clear();

            BankAccount updated = bankAccountRepository.findById(savedAccount.getId()).orElseThrow();

            assertThat(updated.getAccountHolderName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("updates account balance")
        void updatesAccountBalance() {
            savedAccount = bankAccountRepository.findById(savedAccount.getId()).orElseThrow();
            savedAccount.setBalance(new BigDecimal("7500.00"));
            bankAccountRepository.saveAndFlush(savedAccount);
            entityManager.clear();

            BankAccount updated = bankAccountRepository.findById(savedAccount.getId()).orElseThrow();

            assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("7500.00"));
        }

        @Test
        @DisplayName("updates account type")
        void updatesAccountType() {
            savedAccount = bankAccountRepository.findById(savedAccount.getId()).orElseThrow();
            savedAccount.setAccountType("CURRENT");
            bankAccountRepository.saveAndFlush(savedAccount);
            entityManager.clear();

            BankAccount updated = bankAccountRepository.findById(savedAccount.getId()).orElseThrow();

            assertThat(updated.getAccountType()).isEqualTo("CURRENT");
        }
    }

    @Nested
    @DisplayName("CRUD Operations - delete")
    class DeleteOperations {

        @Test
        @DisplayName("deletes account by entity")
        void deletesAccountByEntity() {
            savedAccount = bankAccountRepository.findById(savedAccount.getId()).orElseThrow();
            bankAccountRepository.delete(savedAccount);
            entityManager.flush();
            entityManager.clear();

            Optional<BankAccount> result = bankAccountRepository.findById(savedAccount.getId());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deletes account by ID")
        void deletesAccountById() {
            Long id = savedAccount.getId();
            bankAccountRepository.deleteById(id);
            entityManager.flush();
            entityManager.clear();

            Optional<BankAccount> result = bankAccountRepository.findById(id);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deleteAll removes all accounts")
        void deleteAllRemovesAllAccounts() {
            BankAccount account2 = new BankAccount();
            account2.setAccountNumber("ACC1002");
            account2.setAccountHolderName("Another User");
            account2.setAccountType("CURRENT");
            account2.setBalance(new BigDecimal("2000.00"));
            account2.setCreatedAt(LocalDateTime.now());
            entityManager.persistAndFlush(account2);

            bankAccountRepository.deleteAll();
            entityManager.flush();
            entityManager.clear();

            List<BankAccount> accounts = bankAccountRepository.findAll();

            assertThat(accounts).isEmpty();
        }
    }

    @Nested
    @DisplayName("CRUD Operations - count and exists")
    class CountAndExistsOperations {

        @Test
        @DisplayName("count returns correct number of accounts")
        void countReturnsCorrectNumber() {
            long count = bankAccountRepository.count();

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("existsById returns true for existing account")
        void existsByIdReturnsTrueForExistingAccount() {
            boolean exists = bankAccountRepository.existsById(savedAccount.getId());

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("existsById returns false for non-existent account")
        void existsByIdReturnsFalseForNonExistent() {
            boolean exists = bankAccountRepository.existsById(99999L);

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("BankAccount Entity")
    class BankAccountEntityTests {

        @Test
        @DisplayName("default constructor initializes balance to zero and sets createdAt")
        void defaultConstructorInitializesDefaults() {
            BankAccount account = new BankAccount();

            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(account.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("parameterized constructor sets all fields correctly")
        void parameterizedConstructorSetsFields() {
            BankAccount account = new BankAccount("Test Holder", "CURRENT", new BigDecimal("3000.00"));

            assertThat(account.getAccountHolderName()).isEqualTo("Test Holder");
            assertThat(account.getAccountType()).isEqualTo("CURRENT");
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("3000.00"));
            assertThat(account.getAccountNumber()).startsWith("ACC");
            assertThat(account.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("toString returns expected format")
        void toStringReturnsExpectedFormat() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC5001");
            account.setAccountHolderName("Test Name");
            account.setBalance(new BigDecimal("1234.56"));

            String result = account.toString();

            assertThat(result).contains("ACC5001");
            assertThat(result).contains("Test Name");
            assertThat(result).contains("1234.56");
        }

        @Test
        @DisplayName("all getters and setters work correctly")
        void allGettersAndSettersWork() {
            BankAccount account = new BankAccount();
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 30, 0);

            account.setId(42L);
            account.setAccountNumber("ACC7777");
            account.setAccountHolderName("Getter Setter User");
            account.setAccountType("SAVINGS");
            account.setBalance(new BigDecimal("9999.99"));
            account.setCreatedAt(now);

            assertThat(account.getId()).isEqualTo(42L);
            assertThat(account.getAccountNumber()).isEqualTo("ACC7777");
            assertThat(account.getAccountHolderName()).isEqualTo("Getter Setter User");
            assertThat(account.getAccountType()).isEqualTo("SAVINGS");
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("9999.99"));
            assertThat(account.getCreatedAt()).isEqualTo(now);
        }
    }
}
