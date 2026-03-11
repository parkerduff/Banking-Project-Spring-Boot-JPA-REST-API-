package com.sr_banking.banking_project.model;

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

class BankAccountTest {

    @Nested
    @DisplayName("Default Constructor")
    class DefaultConstructorTests {

        @Test
        @DisplayName("initializes balance to ZERO")
        void defaultConstructorSetsBalanceToZero() {
            BankAccount account = new BankAccount();
            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("initializes createdAt to approximately now")
        void defaultConstructorSetsCreatedAt() {
            LocalDateTime before = LocalDateTime.now();
            BankAccount account = new BankAccount();
            LocalDateTime after = LocalDateTime.now();

            assertThat(account.getCreatedAt()).isNotNull();
            assertThat(account.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(account.getCreatedAt()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("leaves id as null")
        void defaultConstructorLeavesIdNull() {
            BankAccount account = new BankAccount();
            assertThat(account.getId()).isNull();
        }

        @Test
        @DisplayName("leaves accountNumber as null")
        void defaultConstructorLeavesAccountNumberNull() {
            BankAccount account = new BankAccount();
            assertThat(account.getAccountNumber()).isNull();
        }

        @Test
        @DisplayName("leaves accountHolderName as null")
        void defaultConstructorLeavesAccountHolderNameNull() {
            BankAccount account = new BankAccount();
            assertThat(account.getAccountHolderName()).isNull();
        }

        @Test
        @DisplayName("leaves accountType as null")
        void defaultConstructorLeavesAccountTypeNull() {
            BankAccount account = new BankAccount();
            assertThat(account.getAccountType()).isNull();
        }
    }

    @Nested
    @DisplayName("Parameterized Constructor")
    class ParameterizedConstructorTests {

        @Test
        @DisplayName("sets accountHolderName, accountType, and balance")
        void paramConstructorSetsFields() {
            BankAccount account = new BankAccount("John Doe", "SAVINGS", new BigDecimal("1000.50"));

            assertThat(account.getAccountHolderName()).isEqualTo("John Doe");
            assertThat(account.getAccountType()).isEqualTo("SAVINGS");
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000.50"));
        }

        @Test
        @DisplayName("generates accountNumber starting with ACC")
        void paramConstructorGeneratesAccountNumber() {
            BankAccount account = new BankAccount("Jane Doe", "CHECKING", BigDecimal.TEN);

            assertThat(account.getAccountNumber()).isNotNull();
            assertThat(account.getAccountNumber()).startsWith("ACC");
        }

        @Test
        @DisplayName("generates accountNumber with numeric suffix after ACC prefix")
        void paramConstructorAccountNumberHasNumericSuffix() {
            BankAccount account = new BankAccount("Jane Doe", "CHECKING", BigDecimal.TEN);

            String suffix = account.getAccountNumber().substring(3);
            assertThat(suffix).matches("\\d+");
        }

        @Test
        @DisplayName("generates account numbers based on currentTimeMillis")
        void paramConstructorGeneratesAccountNumbersBasedOnTime() throws InterruptedException {
            BankAccount account1 = new BankAccount("Alice", "SAVINGS", BigDecimal.ONE);
            Thread.sleep(2); // Ensure different millis
            BankAccount account2 = new BankAccount("Bob", "CHECKING", BigDecimal.TEN);

            assertThat(account1.getAccountNumber()).startsWith("ACC");
            assertThat(account2.getAccountNumber()).startsWith("ACC");
            assertThat(account1.getAccountNumber()).isNotEqualTo(account2.getAccountNumber());
        }

        @Test
        @DisplayName("sets createdAt to approximately now")
        void paramConstructorSetsCreatedAt() {
            LocalDateTime before = LocalDateTime.now();
            BankAccount account = new BankAccount("Test User", "SAVINGS", BigDecimal.ZERO);
            LocalDateTime after = LocalDateTime.now();

            assertThat(account.getCreatedAt()).isNotNull();
            assertThat(account.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(account.getCreatedAt()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("leaves id as null")
        void paramConstructorLeavesIdNull() {
            BankAccount account = new BankAccount("Test User", "SAVINGS", BigDecimal.ZERO);
            assertThat(account.getId()).isNull();
        }

        @Test
        @DisplayName("accepts zero balance")
        void paramConstructorAcceptsZeroBalance() {
            BankAccount account = new BankAccount("Zero Balance", "SAVINGS", BigDecimal.ZERO);
            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("accepts negative balance")
        void paramConstructorAcceptsNegativeBalance() {
            BankAccount account = new BankAccount("Overdrawn", "CHECKING", new BigDecimal("-500.00"));
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("-500.00"));
        }

        @Test
        @DisplayName("accepts null accountHolderName")
        void paramConstructorAcceptsNullName() {
            BankAccount account = new BankAccount(null, "SAVINGS", BigDecimal.ONE);
            assertThat(account.getAccountHolderName()).isNull();
        }

        @Test
        @DisplayName("accepts null accountType")
        void paramConstructorAcceptsNullType() {
            BankAccount account = new BankAccount("User", null, BigDecimal.ONE);
            assertThat(account.getAccountType()).isNull();
        }

        @Test
        @DisplayName("accepts null balance")
        void paramConstructorAcceptsNullBalance() {
            BankAccount account = new BankAccount("User", "SAVINGS", null);
            assertThat(account.getBalance()).isNull();
        }

        @Test
        @DisplayName("accepts empty string accountHolderName")
        void paramConstructorAcceptsEmptyName() {
            BankAccount account = new BankAccount("", "SAVINGS", BigDecimal.ONE);
            assertThat(account.getAccountHolderName()).isEmpty();
        }

        @Test
        @DisplayName("accepts empty string accountType")
        void paramConstructorAcceptsEmptyType() {
            BankAccount account = new BankAccount("User", "", BigDecimal.ONE);
            assertThat(account.getAccountType()).isEmpty();
        }

        @Test
        @DisplayName("accepts large balance value")
        void paramConstructorAcceptsLargeBalance() {
            BigDecimal largeAmount = new BigDecimal("99999999999999.99");
            BankAccount account = new BankAccount("Rich User", "SAVINGS", largeAmount);
            assertThat(account.getBalance()).isEqualByComparingTo(largeAmount);
        }

        @Test
        @DisplayName("accepts unicode characters in accountHolderName")
        void paramConstructorAcceptsUnicodeName() {
            BankAccount account = new BankAccount("\u00c9l\u00e8na M\u00fcller-\u00d6stberg", "SAVINGS", BigDecimal.TEN);
            assertThat(account.getAccountHolderName()).isEqualTo("\u00c9l\u00e8na M\u00fcller-\u00d6stberg");
        }
    }

    @Nested
    @DisplayName("Getter and Setter Methods")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId round-trip")
        void setAndGetId() {
            BankAccount account = new BankAccount();
            account.setId(42L);
            assertThat(account.getId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("setId accepts null")
        void setIdNull() {
            BankAccount account = new BankAccount();
            account.setId(100L);
            account.setId(null);
            assertThat(account.getId()).isNull();
        }

        @Test
        @DisplayName("setAccountNumber and getAccountNumber round-trip")
        void setAndGetAccountNumber() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC123456");
            assertThat(account.getAccountNumber()).isEqualTo("ACC123456");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "ACC999", "CUSTOM-FORMAT-001"})
        @DisplayName("setAccountNumber accepts various values")
        void setAccountNumberVariousValues(String value) {
            BankAccount account = new BankAccount();
            account.setAccountNumber(value);
            assertThat(account.getAccountNumber()).isEqualTo(value);
        }

        @Test
        @DisplayName("setAccountHolderName and getAccountHolderName round-trip")
        void setAndGetAccountHolderName() {
            BankAccount account = new BankAccount();
            account.setAccountHolderName("Alice Smith");
            assertThat(account.getAccountHolderName()).isEqualTo("Alice Smith");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "A", "Very Long Name With Many Words In It"})
        @DisplayName("setAccountHolderName accepts various values")
        void setAccountHolderNameVariousValues(String value) {
            BankAccount account = new BankAccount();
            account.setAccountHolderName(value);
            assertThat(account.getAccountHolderName()).isEqualTo(value);
        }

        @Test
        @DisplayName("setAccountType and getAccountType round-trip")
        void setAndGetAccountType() {
            BankAccount account = new BankAccount();
            account.setAccountType("CHECKING");
            assertThat(account.getAccountType()).isEqualTo("CHECKING");
        }

        @ParameterizedTest
        @CsvSource({"SAVINGS", "CHECKING", "BUSINESS", "JOINT"})
        @DisplayName("setAccountType accepts common account types")
        void setAccountTypeCommonTypes(String type) {
            BankAccount account = new BankAccount();
            account.setAccountType(type);
            assertThat(account.getAccountType()).isEqualTo(type);
        }

        @Test
        @DisplayName("setBalance and getBalance round-trip")
        void setAndGetBalance() {
            BankAccount account = new BankAccount();
            account.setBalance(new BigDecimal("5000.75"));
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("5000.75"));
        }

        @Test
        @DisplayName("setBalance accepts null")
        void setBalanceNull() {
            BankAccount account = new BankAccount();
            account.setBalance(null);
            assertThat(account.getBalance()).isNull();
        }

        @Test
        @DisplayName("setBalance accepts negative value")
        void setBalanceNegative() {
            BankAccount account = new BankAccount();
            account.setBalance(new BigDecimal("-100.00"));
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("setCreatedAt and getCreatedAt round-trip")
        void setAndGetCreatedAt() {
            BankAccount account = new BankAccount();
            LocalDateTime fixedTime = LocalDateTime.of(2025, 6, 15, 10, 30, 0);
            account.setCreatedAt(fixedTime);
            assertThat(account.getCreatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("setCreatedAt accepts null")
        void setCreatedAtNull() {
            BankAccount account = new BankAccount();
            account.setCreatedAt(null);
            assertThat(account.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("setBalance overrides previous value")
        void setBalanceOverridesPrevious() {
            BankAccount account = new BankAccount("User", "SAVINGS", new BigDecimal("1000.00"));
            account.setBalance(new BigDecimal("2000.00"));
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("2000.00"));
        }

        @Test
        @DisplayName("setAccountNumber overrides generated value")
        void setAccountNumberOverridesGenerated() {
            BankAccount account = new BankAccount("User", "SAVINGS", BigDecimal.TEN);
            String original = account.getAccountNumber();
            account.setAccountNumber("CUSTOM123");
            assertThat(account.getAccountNumber()).isEqualTo("CUSTOM123");
            assertThat(account.getAccountNumber()).isNotEqualTo(original);
        }
    }

    @Nested
    @DisplayName("toString Method")
    class ToStringTests {

        @Test
        @DisplayName("returns expected format with all fields set")
        void toStringWithAllFields() {
            BankAccount account = new BankAccount("John Doe", "SAVINGS", new BigDecimal("1500.00"));
            String result = account.toString();

            assertThat(result).contains("Account: ");
            assertThat(result).contains(account.getAccountNumber());
            assertThat(result).contains("Holder: John Doe");
            assertThat(result).contains("Balance: \u20b91500.00");
        }

        @Test
        @DisplayName("handles null accountNumber gracefully")
        void toStringWithNullAccountNumber() {
            BankAccount account = new BankAccount();
            account.setAccountHolderName("Test");
            String result = account.toString();

            assertThat(result).contains("Account: null");
            assertThat(result).contains("Holder: Test");
        }

        @Test
        @DisplayName("handles null accountHolderName gracefully")
        void toStringWithNullHolderName() {
            BankAccount account = new BankAccount();
            String result = account.toString();

            assertThat(result).contains("Holder: null");
        }

        @Test
        @DisplayName("handles null balance")
        void toStringWithNullBalance() {
            BankAccount account = new BankAccount();
            account.setBalance(null);
            String result = account.toString();

            assertThat(result).contains("Balance: \u20b9null");
        }

        @Test
        @DisplayName("toString format matches expected pattern")
        void toStringFormatPattern() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC100");
            account.setAccountHolderName("Bob");
            account.setBalance(new BigDecimal("250.00"));

            String expected = "Account: ACC100 | Holder: Bob | Balance: \u20b9250.00";
            assertThat(account.toString()).isEqualTo(expected);
        }

        @Test
        @DisplayName("toString with zero balance")
        void toStringWithZeroBalance() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC200");
            account.setAccountHolderName("Eve");

            String result = account.toString();
            assertThat(result).contains("Balance: \u20b90");
        }

        @Test
        @DisplayName("toString with negative balance")
        void toStringWithNegativeBalance() {
            BankAccount account = new BankAccount();
            account.setAccountNumber("ACC300");
            account.setAccountHolderName("Dave");
            account.setBalance(new BigDecimal("-50.00"));

            String result = account.toString();
            assertThat(result).contains("Balance: \u20b9-50.00");
        }
    }

    @Nested
    @DisplayName("Equality Behavior (default Object.equals)")
    class EqualityTests {

        @Test
        @DisplayName("same instance is equal to itself")
        void sameInstanceIsEqual() {
            BankAccount account = new BankAccount("Alice", "SAVINGS", BigDecimal.TEN);
            assertThat(account).isEqualTo(account);
        }

        @Test
        @DisplayName("two different instances with same data are not equal (reference equality)")
        void differentInstancesNotEqual() {
            BankAccount account1 = new BankAccount();
            account1.setId(1L);
            account1.setAccountNumber("ACC100");
            account1.setAccountHolderName("Alice");
            account1.setAccountType("SAVINGS");
            account1.setBalance(BigDecimal.TEN);

            BankAccount account2 = new BankAccount();
            account2.setId(1L);
            account2.setAccountNumber("ACC100");
            account2.setAccountHolderName("Alice");
            account2.setAccountType("SAVINGS");
            account2.setBalance(BigDecimal.TEN);

            // Default Object.equals uses reference equality
            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("account is not equal to null")
        void notEqualToNull() {
            BankAccount account = new BankAccount("User", "SAVINGS", BigDecimal.ONE);
            assertThat(account).isNotEqualTo(null);
        }

        @Test
        @DisplayName("account is not equal to different type")
        void notEqualToDifferentType() {
            BankAccount account = new BankAccount("User", "SAVINGS", BigDecimal.ONE);
            assertThat(account).isNotEqualTo("not a BankAccount");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Values")
    class EdgeCaseTests {

        @Test
        @DisplayName("BigDecimal precision is preserved")
        void bigDecimalPrecisionPreserved() {
            BigDecimal precise = new BigDecimal("12345.6789012345");
            BankAccount account = new BankAccount("Precise", "SAVINGS", precise);
            assertThat(account.getBalance()).isEqualByComparingTo(precise);
        }

        @Test
        @DisplayName("very small positive balance")
        void verySmallBalance() {
            BigDecimal tiny = new BigDecimal("0.01");
            BankAccount account = new BankAccount("Tiny", "SAVINGS", tiny);
            assertThat(account.getBalance()).isEqualByComparingTo(tiny);
        }

        @Test
        @DisplayName("maximum Long value for id")
        void maxLongId() {
            BankAccount account = new BankAccount();
            account.setId(Long.MAX_VALUE);
            assertThat(account.getId()).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("minimum Long value for id")
        void minLongId() {
            BankAccount account = new BankAccount();
            account.setId(Long.MIN_VALUE);
            assertThat(account.getId()).isEqualTo(Long.MIN_VALUE);
        }

        @Test
        @DisplayName("multiple setter calls update field correctly")
        void multipleSetterCalls() {
            BankAccount account = new BankAccount();
            account.setAccountHolderName("First");
            account.setAccountHolderName("Second");
            account.setAccountHolderName("Third");
            assertThat(account.getAccountHolderName()).isEqualTo("Third");
        }

        @Test
        @DisplayName("setting all fields via setters")
        void settingAllFieldsViaSetters() {
            BankAccount account = new BankAccount();
            LocalDateTime time = LocalDateTime.of(2025, 1, 1, 0, 0);

            account.setId(1L);
            account.setAccountNumber("ACC001");
            account.setAccountHolderName("Full Test");
            account.setAccountType("BUSINESS");
            account.setBalance(new BigDecimal("99999.99"));
            account.setCreatedAt(time);

            assertThat(account.getId()).isEqualTo(1L);
            assertThat(account.getAccountNumber()).isEqualTo("ACC001");
            assertThat(account.getAccountHolderName()).isEqualTo("Full Test");
            assertThat(account.getAccountType()).isEqualTo("BUSINESS");
            assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("99999.99"));
            assertThat(account.getCreatedAt()).isEqualTo(time);
        }

        @Test
        @DisplayName("whitespace-only accountHolderName is accepted")
        void whitespaceOnlyName() {
            BankAccount account = new BankAccount("   ", "SAVINGS", BigDecimal.ONE);
            assertThat(account.getAccountHolderName()).isEqualTo("   ");
        }

        @Test
        @DisplayName("whitespace-only accountType is accepted")
        void whitespaceOnlyType() {
            BankAccount account = new BankAccount("User", "   ", BigDecimal.ONE);
            assertThat(account.getAccountType()).isEqualTo("   ");
        }
    }
}
