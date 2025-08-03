package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

/*
JUnit 5 and parameterized test

 */
class BankAccountTest {

    private BankAccount account;
    private final String correctPin = "1015";

    @BeforeEach
    void setUp() {
        account = new BankAccount(1000.00, correctPin);
        // Clean up log from previous test runs to ensure a clean slate
        File transactionLog = new File("transaction_history.csv");
        if (transactionLog.exists()) {
            transactionLog.delete();
        }
    }

    @Test
    @DisplayName("Constructor should set positive initial balance")
    void testConstructorWithPositiveBalance() {
        assertEquals(1000.00, account.getBalance(), "Incorrect initial balance.");
        assertFalse(account.getTransactionHistory().isEmpty(), "History should not be empty.");
    }

    @ParameterizedTest
    @DisplayName("PIN validation should work for various inputs")
    @CsvSource({
            "1015, true",   // Correct PIN
            "0000, false",  // Incorrect PIN
            "abcd, false",  // Incorrect PIN
            "'',   false"   // Empty PIN
    })
    void testPinValidation(String pin, boolean expectedResult) {
        assertEquals(expectedResult, account.validatePin(pin), "PIN validation failed for: " + pin);
    }

    @Test
    @DisplayName("Should change PIN successfully with correct old PIN")
    void testChangePinSuccess() {
        String newPin = "9876";
        assertTrue(account.changePin(correctPin, newPin), "PIN change should succeed.");
        assertTrue(account.validatePin(newPin), "New PIN should be valid.");
    }

    @Test
    @DisplayName("Should not change PIN with incorrect old PIN")
    void testChangePinFailure() {
        assertFalse(account.changePin("0000", "9876"), "PIN change should fail.");
        assertTrue(account.validatePin(correctPin), "Old PIN should remain valid.");
    }

    @Test
    @DisplayName("Should deposit a positive amount correctly")
    void testValidDeposit() {
        account.deposit(200.50);
        assertEquals(1200.50, account.getBalance(), "Balance incorrect after deposit.");
    }

    @ParameterizedTest
    @DisplayName("Should not deposit invalid amounts")
    @ValueSource(doubles = {-100.0, 0.0})
    void testInvalidDeposit(double amount) {
        account.deposit(amount);
        assertEquals(1000.00, account.getBalance(), "Balance should not change on invalid deposit.");
    }

    @Test
    @DisplayName("Should withdraw a valid amount correctly")
    void testValidWithdraw() {
        account.withdraw(300.00);
        assertEquals(700.00, account.getBalance(), "Balance incorrect after withdrawal.");
    }

    @ParameterizedTest
    @DisplayName("Should not withdraw invalid amounts")
    @ValueSource(doubles = {-200.0, 0.0, 1500.0}) // Includes insufficient funds
    void testInvalidWithdraw(double amount) {
        account.withdraw(amount);
        assertEquals(1000.00, account.getBalance(), "Balance should not change on invalid withdrawal.");
    }
}
