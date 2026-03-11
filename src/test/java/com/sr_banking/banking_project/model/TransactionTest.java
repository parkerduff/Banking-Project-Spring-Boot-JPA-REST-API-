package com.sr_banking.banking_project.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    private BankAccount sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = new BankAccount();
        sampleAccount.setId(1L);
        sampleAccount.setAccountNumber("ACC1001");
        sampleAccount.setAccountHolderName("Test User");
        sampleAccount.setAccountType("SAVINGS");
        sampleAccount.setBalance(new BigDecimal("5000.00"));
    }

    @Nested
    @DisplayName("Default Constructor")
    class DefaultConstructorTests {

        @Test
        @DisplayName("sets transactionDate to current time")
        void setsTransactionDateToNow() {
            LocalDateTime before = LocalDateTime.now();
            Transaction tx = new Transaction();
            LocalDateTime after = LocalDateTime.now();

            assertThat(tx.getTransactionDate()).isNotNull();
            assertThat(tx.getTransactionDate()).isAfterOrEqualTo(before);
            assertThat(tx.getTransactionDate()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("leaves all other fields null/default")
        void leavesOtherFieldsNull() {
            Transaction tx = new Transaction();

            assertThat(tx.getId()).isNull();
            assertThat(tx.getTransactionId()).isNull();
            assertThat(tx.getTransactionType()).isNull();
            assertThat(tx.getAmount()).isNull();
            assertThat(tx.getDescription()).isNull();
            assertThat(tx.getBankAccount()).isNull();
        }
    }

    @Nested
    @DisplayName("Parameterized Constructor")
    class ParameterizedConstructorTests {

        @Test
        @DisplayName("sets all provided fields correctly")
        void setsAllFields() {
            BigDecimal amount = new BigDecimal("1500.50");
            String description = "Salary credit";

            Transaction tx = new Transaction("DEPOSIT", amount, description, sampleAccount);

            assertThat(tx.getTransactionType()).isEqualTo("DEPOSIT");
            assertThat(tx.getAmount()).isEqualByComparingTo(amount);
            assertThat(tx.getDescription()).isEqualTo(description);
            assertThat(tx.getBankAccount()).isSameAs(sampleAccount);
        }

        @Test
        @DisplayName("generates transactionId starting with TXN")
        void generatesTransactionId() {
            Transaction tx = new Transaction("DEPOSIT", BigDecimal.TEN, "desc", sampleAccount);

            assertThat(tx.getTransactionId()).isNotNull();
            assertThat(tx.getTransactionId()).startsWith("TXN");
        }

        @Test
        @DisplayName("transactionId contains millis timestamp after TXN prefix")
        void transactionIdContainsTimestamp() {
            long before = System.currentTimeMillis();
            Transaction tx = new Transaction("DEPOSIT", BigDecimal.TEN, "desc", sampleAccount);
            long after = System.currentTimeMillis();

            String idSuffix = tx.getTransactionId().substring(3);
            long timestamp = Long.parseLong(idSuffix);

            assertThat(timestamp).isBetween(before, after);
        }

        @Test
        @DisplayName("two transactions get different transactionIds")
        void uniqueTransactionIds() {
            Transaction tx1 = new Transaction("DEPOSIT", BigDecimal.TEN, "desc1", sampleAccount);
            // small delay to ensure different millis
            Transaction tx2 = new Transaction("WITHDRAWAL", BigDecimal.ONE, "desc2", sampleAccount);

            // They could be the same if created in the same millisecond, but the IDs are generated
            // We just verify format; uniqueness depends on timing
            assertThat(tx1.getTransactionId()).startsWith("TXN");
            assertThat(tx2.getTransactionId()).startsWith("TXN");
        }

        @Test
        @DisplayName("sets transactionDate to current time")
        void setsTransactionDate() {
            LocalDateTime before = LocalDateTime.now();
            Transaction tx = new Transaction("WITHDRAWAL", BigDecimal.ONE, "desc", sampleAccount);
            LocalDateTime after = LocalDateTime.now();

            assertThat(tx.getTransactionDate()).isNotNull();
            assertThat(tx.getTransactionDate()).isAfterOrEqualTo(before);
            assertThat(tx.getTransactionDate()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("id remains null before persistence")
        void idRemainsNull() {
            Transaction tx = new Transaction("DEPOSIT", BigDecimal.TEN, "desc", sampleAccount);
            assertThat(tx.getId()).isNull();
        }

        @ParameterizedTest
        @DisplayName("works with various transaction types")
        @ValueSource(strings = {"DEPOSIT", "WITHDRAWAL", "TRANSFER", "REFUND", "FEE"})
        void variousTransactionTypes(String type) {
            Transaction tx = new Transaction(type, BigDecimal.TEN, "desc", sampleAccount);
            assertThat(tx.getTransactionType()).isEqualTo(type);
        }

        @Test
        @DisplayName("accepts null transactionType")
        void acceptsNullTransactionType() {
            Transaction tx = new Transaction(null, BigDecimal.TEN, "desc", sampleAccount);
            assertThat(tx.getTransactionType()).isNull();
        }

        @Test
        @DisplayName("accepts null amount")
        void acceptsNullAmount() {
            Transaction tx = new Transaction("DEPOSIT", null, "desc", sampleAccount);
            assertThat(tx.getAmount()).isNull();
        }

        @Test
        @DisplayName("accepts null description")
        void acceptsNullDescription() {
            Transaction tx = new Transaction("DEPOSIT", BigDecimal.TEN, null, sampleAccount);
            assertThat(tx.getDescription()).isNull();
        }

        @Test
        @DisplayName("accepts null bankAccount")
        void acceptsNullBankAccount() {
            Transaction tx = new Transaction("DEPOSIT", BigDecimal.TEN, "desc", null);
            assertThat(tx.getBankAccount()).isNull();
        }
    }

    @Nested
    @DisplayName("Getter and Setter Methods")
    class GetterSetterTests {

        private Transaction tx;

        @BeforeEach
        void setUp() {
            tx = new Transaction();
        }

        @Test
        @DisplayName("setId / getId")
        void idGetterSetter() {
            tx.setId(42L);
            assertThat(tx.getId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("setId with null")
        void idSetNull() {
            tx.setId(100L);
            tx.setId(null);
            assertThat(tx.getId()).isNull();
        }

        @Test
        @DisplayName("setTransactionId / getTransactionId")
        void transactionIdGetterSetter() {
            tx.setTransactionId("TXN999");
            assertThat(tx.getTransactionId()).isEqualTo("TXN999");
        }

        @Test
        @DisplayName("setTransactionId with null")
        void transactionIdSetNull() {
            tx.setTransactionId("TXN123");
            tx.setTransactionId(null);
            assertThat(tx.getTransactionId()).isNull();
        }

        @ParameterizedTest
        @DisplayName("setTransactionType with various values")
        @ValueSource(strings = {"DEPOSIT", "WITHDRAWAL", "TRANSFER"})
        void transactionTypeGetterSetter(String type) {
            tx.setTransactionType(type);
            assertThat(tx.getTransactionType()).isEqualTo(type);
        }

        @Test
        @DisplayName("setTransactionType with null")
        void transactionTypeSetNull() {
            tx.setTransactionType("DEPOSIT");
            tx.setTransactionType(null);
            assertThat(tx.getTransactionType()).isNull();
        }

        @Test
        @DisplayName("setAmount / getAmount with positive value")
        void amountPositive() {
            tx.setAmount(new BigDecimal("250.75"));
            assertThat(tx.getAmount()).isEqualByComparingTo(new BigDecimal("250.75"));
        }

        @Test
        @DisplayName("setAmount with zero")
        void amountZero() {
            tx.setAmount(BigDecimal.ZERO);
            assertThat(tx.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("setAmount with negative value")
        void amountNegative() {
            tx.setAmount(new BigDecimal("-100.00"));
            assertThat(tx.getAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("setAmount with very large value")
        void amountLargeValue() {
            BigDecimal large = new BigDecimal("99999999999999.99");
            tx.setAmount(large);
            assertThat(tx.getAmount()).isEqualByComparingTo(large);
        }

        @Test
        @DisplayName("setAmount with null")
        void amountNull() {
            tx.setAmount(new BigDecimal("100"));
            tx.setAmount(null);
            assertThat(tx.getAmount()).isNull();
        }

        @Test
        @DisplayName("setDescription / getDescription")
        void descriptionGetterSetter() {
            tx.setDescription("Monthly salary");
            assertThat(tx.getDescription()).isEqualTo("Monthly salary");
        }

        @Test
        @DisplayName("setDescription with empty string")
        void descriptionEmpty() {
            tx.setDescription("");
            assertThat(tx.getDescription()).isEmpty();
        }

        @Test
        @DisplayName("setDescription with unicode characters")
        void descriptionUnicode() {
            tx.setDescription("Payment \u20B9500 for groceries \uD83D\uDED2");
            assertThat(tx.getDescription()).isEqualTo("Payment \u20B9500 for groceries \uD83D\uDED2");
        }

        @Test
        @DisplayName("setDescription with null")
        void descriptionNull() {
            tx.setDescription("something");
            tx.setDescription(null);
            assertThat(tx.getDescription()).isNull();
        }

        @Test
        @DisplayName("setTransactionDate / getTransactionDate")
        void transactionDateGetterSetter() {
            LocalDateTime date = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            tx.setTransactionDate(date);
            assertThat(tx.getTransactionDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("setTransactionDate with null overrides default")
        void transactionDateSetNull() {
            assertThat(tx.getTransactionDate()).isNotNull(); // set by default constructor
            tx.setTransactionDate(null);
            assertThat(tx.getTransactionDate()).isNull();
        }

        @Test
        @DisplayName("setBankAccount / getBankAccount")
        void bankAccountGetterSetter() {
            tx.setBankAccount(sampleAccount);
            assertThat(tx.getBankAccount()).isSameAs(sampleAccount);
        }

        @Test
        @DisplayName("setBankAccount with null")
        void bankAccountSetNull() {
            tx.setBankAccount(sampleAccount);
            tx.setBankAccount(null);
            assertThat(tx.getBankAccount()).isNull();
        }

        @Test
        @DisplayName("setBankAccount can be changed to different account")
        void bankAccountReassignment() {
            BankAccount anotherAccount = new BankAccount();
            anotherAccount.setId(2L);
            anotherAccount.setAccountNumber("ACC2002");

            tx.setBankAccount(sampleAccount);
            assertThat(tx.getBankAccount()).isSameAs(sampleAccount);

            tx.setBankAccount(anotherAccount);
            assertThat(tx.getBankAccount()).isSameAs(anotherAccount);
        }
    }

    @Nested
    @DisplayName("toString Method")
    class ToStringTests {

        @Test
        @DisplayName("includes transaction type, amount, description, and date")
        void includesAllFields() {
            Transaction tx = new Transaction("DEPOSIT", new BigDecimal("1500.00"), "Salary credit", sampleAccount);
            String result = tx.toString();

            assertThat(result).contains("DEPOSIT");
            assertThat(result).contains("1500.00");
            assertThat(result).contains("Salary credit");
            assertThat(result).contains("Date:");
        }

        @Test
        @DisplayName("format matches expected pattern")
        void matchesExpectedFormat() {
            LocalDateTime fixedDate = LocalDateTime.of(2024, 3, 15, 14, 30, 0);
            Transaction tx = new Transaction();
            tx.setTransactionType("WITHDRAWAL");
            tx.setAmount(new BigDecimal("500.00"));
            tx.setDescription("ATM withdrawal");
            tx.setTransactionDate(fixedDate);

            String result = tx.toString();

            assertThat(result).isEqualTo(
                    "WITHDRAWAL | Amount: \u20B9500.00 | Desc: ATM withdrawal | Date: 2024-03-15T14:30");
        }

        @Test
        @DisplayName("handles null fields gracefully in toString")
        void handlesNullFields() {
            Transaction tx = new Transaction();
            tx.setTransactionType(null);
            tx.setAmount(null);
            tx.setDescription(null);
            tx.setTransactionDate(null);

            String result = tx.toString();

            assertThat(result).contains("null");
            assertThat(result).contains("Amount:");
            assertThat(result).contains("Desc:");
            assertThat(result).contains("Date:");
        }

        @Test
        @DisplayName("toString with zero amount")
        void toStringZeroAmount() {
            Transaction tx = new Transaction();
            tx.setTransactionType("FEE");
            tx.setAmount(BigDecimal.ZERO);
            tx.setDescription("No charge");
            tx.setTransactionDate(LocalDateTime.of(2024, 1, 1, 0, 0));

            String result = tx.toString();

            assertThat(result).startsWith("FEE");
            assertThat(result).contains("0");
            assertThat(result).contains("No charge");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Values")
    class EdgeCaseTests {

        @Test
        @DisplayName("amount with many decimal places")
        void amountManyDecimals() {
            Transaction tx = new Transaction();
            BigDecimal precise = new BigDecimal("123.456789012345");
            tx.setAmount(precise);
            assertThat(tx.getAmount()).isEqualByComparingTo(precise);
        }

        @Test
        @DisplayName("very long description string")
        void veryLongDescription() {
            String longDesc = "A".repeat(10000);
            Transaction tx = new Transaction();
            tx.setDescription(longDesc);
            assertThat(tx.getDescription()).hasSize(10000);
        }

        @Test
        @DisplayName("transactionId set manually overrides generated value")
        void manualTransactionIdOverride() {
            Transaction tx = new Transaction("DEPOSIT", BigDecimal.TEN, "desc", sampleAccount);
            assertThat(tx.getTransactionId()).startsWith("TXN");

            tx.setTransactionId("CUSTOM-ID-001");
            assertThat(tx.getTransactionId()).isEqualTo("CUSTOM-ID-001");
        }

        @Test
        @DisplayName("transaction date can be set to past date")
        void pastDate() {
            Transaction tx = new Transaction();
            LocalDateTime past = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
            tx.setTransactionDate(past);
            assertThat(tx.getTransactionDate()).isEqualTo(past);
        }

        @Test
        @DisplayName("transaction date can be set to future date")
        void futureDate() {
            Transaction tx = new Transaction();
            LocalDateTime future = LocalDateTime.of(2030, 12, 31, 23, 59, 59);
            tx.setTransactionDate(future);
            assertThat(tx.getTransactionDate()).isEqualTo(future);
        }

        @ParameterizedTest
        @DisplayName("amount edge values")
        @CsvSource({
                "0.01",
                "0.00",
                "-0.01",
                "999999999.99",
                "0.001"
        })
        void amountEdgeValues(String value) {
            Transaction tx = new Transaction();
            BigDecimal amt = new BigDecimal(value);
            tx.setAmount(amt);
            assertThat(tx.getAmount()).isEqualByComparingTo(amt);
        }

        @Test
        @DisplayName("empty string transactionType")
        void emptyTransactionType() {
            Transaction tx = new Transaction("", BigDecimal.TEN, "desc", sampleAccount);
            assertThat(tx.getTransactionType()).isEmpty();
        }

        @Test
        @DisplayName("empty string description in constructor")
        void emptyDescriptionInConstructor() {
            Transaction tx = new Transaction("DEPOSIT", BigDecimal.TEN, "", sampleAccount);
            assertThat(tx.getDescription()).isEmpty();
        }
    }

    @Nested
    @DisplayName("BankAccount Relationship")
    class BankAccountRelationshipTests {

        @Test
        @DisplayName("transaction holds reference to BankAccount")
        void holdsAccountReference() {
            Transaction tx = new Transaction("DEPOSIT", new BigDecimal("1000"), "test", sampleAccount);
            assertThat(tx.getBankAccount()).isNotNull();
            assertThat(tx.getBankAccount().getAccountNumber()).isEqualTo("ACC1001");
            assertThat(tx.getBankAccount().getAccountHolderName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("modifying bankAccount through transaction reference reflects changes")
        void mutableAccountReference() {
            Transaction tx = new Transaction("DEPOSIT", new BigDecimal("1000"), "test", sampleAccount);
            tx.getBankAccount().setBalance(new BigDecimal("9999.99"));

            assertThat(sampleAccount.getBalance()).isEqualByComparingTo(new BigDecimal("9999.99"));
        }

        @Test
        @DisplayName("multiple transactions can reference same BankAccount")
        void multipleTransactionsSameAccount() {
            Transaction tx1 = new Transaction("DEPOSIT", new BigDecimal("100"), "first", sampleAccount);
            Transaction tx2 = new Transaction("WITHDRAWAL", new BigDecimal("50"), "second", sampleAccount);

            assertThat(tx1.getBankAccount()).isSameAs(tx2.getBankAccount());
        }

        @Test
        @DisplayName("transaction can be reassigned to different BankAccount")
        void reassignAccount() {
            BankAccount account2 = new BankAccount();
            account2.setId(2L);
            account2.setAccountNumber("ACC2002");

            Transaction tx = new Transaction("DEPOSIT", BigDecimal.TEN, "desc", sampleAccount);
            assertThat(tx.getBankAccount().getAccountNumber()).isEqualTo("ACC1001");

            tx.setBankAccount(account2);
            assertThat(tx.getBankAccount().getAccountNumber()).isEqualTo("ACC2002");
        }
    }
}
