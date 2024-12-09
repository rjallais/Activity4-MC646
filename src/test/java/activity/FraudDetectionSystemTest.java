package activity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class FraudDetectionSystemTest {
    private FraudDetectionSystem fraudSystem;

    @BeforeEach
    void setUp() {
        fraudSystem = new FraudDetectionSystem();
    }

    @Test
    void testNormalTransaction() {
        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(500, LocalDateTime.now(), "Safe City");
        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(transaction, new ArrayList<>(), new ArrayList<>());

        Assertions.assertFalse(result.isFraudulent);
        Assertions.assertFalse(result.isBlocked);
        Assertions.assertFalse(result.verificationRequired);
        Assertions.assertEquals(0, result.riskScore);
    }

    @Test
    void testLargeAmountTransaction() {
        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(15000, LocalDateTime.now(), "New York");
        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(transaction, new ArrayList<>(), new ArrayList<>());

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(50, result.riskScore);
    }

    @Test
    void testExcessiveTransactions() {
        List<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 11; i++) {
            previousTransactions.add(new FraudDetectionSystem.Transaction(100, now.minusMinutes(i * 5), "New York"));
        }

        FraudDetectionSystem.Transaction currentTransaction = new FraudDetectionSystem.Transaction(100, now, "New York");
        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(currentTransaction, previousTransactions, new ArrayList<>());

        Assertions.assertTrue(result.isBlocked);
        Assertions.assertEquals(30, result.riskScore);
    }

    @Test
    void testQuickLocationChange() {
        List<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        previousTransactions.add(new FraudDetectionSystem.Transaction(100, now.minusMinutes(20), "New York"));

        FraudDetectionSystem.Transaction currentTransaction = new FraudDetectionSystem.Transaction(100, now, "Los Angeles");
        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(currentTransaction, previousTransactions, new ArrayList<>());

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(20, result.riskScore);
    }

    @Test
    void testBlacklistedLocation() {
        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, LocalDateTime.now(), "Suspicious City");
        List<String> blacklistedLocations = Arrays.asList("Suspicious City", "Fraud Town");

        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(transaction, new ArrayList<>(), blacklistedLocations);

        Assertions.assertTrue(result.isBlocked);
        Assertions.assertEquals(100, result.riskScore);
    }

    @Test
    void testExcessiveTransactionsInLastHour() {
        List<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 11; i++) {
            previousTransactions.add(new FraudDetectionSystem.Transaction(100, now.minusMinutes(i * 5), "New York"));
        }

        FraudDetectionSystem.Transaction currentTransaction = new FraudDetectionSystem.Transaction(100, now, "New York");
        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(currentTransaction, previousTransactions, new ArrayList<>());

        Assertions.assertTrue(result.isBlocked);
        Assertions.assertTrue(result.riskScore >= 30);
    }

    @Test
    void testQuickLocationChangeWithinShortTimeFrame() {
        List<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        previousTransactions.add(new FraudDetectionSystem.Transaction(100, now.minusMinutes(25), "New York"));

        FraudDetectionSystem.Transaction currentTransaction = new FraudDetectionSystem.Transaction(100, now, "Los Angeles");
        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(currentTransaction, previousTransactions, new ArrayList<>());

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertTrue(result.riskScore >= 20);
    }

    @Test
    void testNoPreviousTransactions() {
        LocalDateTime now = LocalDateTime.now();
        FraudDetectionSystem.Transaction currentTransaction = new FraudDetectionSystem.Transaction(100, now, "New York");
        List<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        List<String> blacklistedLocations = new ArrayList<>();

        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(currentTransaction, previousTransactions, blacklistedLocations);

        Assertions.assertFalse(result.isFraudulent);
        Assertions.assertFalse(result.isBlocked);
        Assertions.assertFalse(result.verificationRequired);
        Assertions.assertEquals(0, result.riskScore);
    }

    @Test
    void testNonBlacklistedLocation() {
        LocalDateTime now = LocalDateTime.now();
        FraudDetectionSystem.Transaction currentTransaction = new FraudDetectionSystem.Transaction(100, now, "Safe City");
        List<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        List<String> blacklistedLocations = Arrays.asList("Suspicious City", "Fraud Town");

        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(currentTransaction, previousTransactions, blacklistedLocations);

        Assertions.assertFalse(result.isBlocked);
        Assertions.assertEquals(0, result.riskScore);
    }

    @Test
    void testLocationChangeButNotWithinShortTimeFrame() {
        List<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        previousTransactions.add(new FraudDetectionSystem.Transaction(100, now.minusMinutes(31), "New York"));

        FraudDetectionSystem.Transaction currentTransaction = new FraudDetectionSystem.Transaction(100, now, "Los Angeles");
        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(currentTransaction, previousTransactions, new ArrayList<>());

        Assertions.assertFalse(result.isFraudulent);
        Assertions.assertFalse(result.verificationRequired);
        Assertions.assertEquals(0, result.riskScore);
    }

    @Test
    void testTransactionMoreThan60MinutesApart() {
        List<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Add a transaction that occurred more than 60 minutes ago
        previousTransactions.add(new FraudDetectionSystem.Transaction(100, now.minusMinutes(61), "New York"));

        // Current transaction
        FraudDetectionSystem.Transaction currentTransaction = new FraudDetectionSystem.Transaction(100, now, "New York");

        FraudDetectionSystem.FraudCheckResult result = fraudSystem.checkForFraud(currentTransaction, previousTransactions, new ArrayList<>());

        // The transaction should not be considered fraudulent or blocked
        Assertions.assertFalse(result.isFraudulent);
        Assertions.assertFalse(result.isBlocked);
        Assertions.assertEquals(0, result.riskScore);
    }

    @Test
    void testLocationChangeWithinShortTimeFrame() {
        FraudDetectionSystem system = new FraudDetectionSystem();

        // Set up the last transaction to be 20 minutes ago in "New York"
        double lastTransactionAmount = 500;
        LocalDateTime lastTransactionTime = LocalDateTime.now().minusMinutes(20);
        String lastTransactionLocation = "New York";

        // Set up the current transaction to be in the same location
        double currentTransactionAmount = 300;
        LocalDateTime currentTransactionTime = LocalDateTime.now();
        String currentTransactionLocation = "New York";
        List<String> blacklistedLocations = List.of(); // No blacklisted locations for this test

        // Mock the list of previous transactions and include the last transaction data
        List<FraudDetectionSystem.Transaction> previousTransactions = List.of(
                new FraudDetectionSystem.Transaction(lastTransactionAmount, lastTransactionTime, lastTransactionLocation)
        );

        // Perform the fraud check
        FraudDetectionSystem.FraudCheckResult result = system.checkForFraud(
                new FraudDetectionSystem.Transaction(currentTransactionAmount, currentTransactionTime, currentTransactionLocation),
                previousTransactions,
                blacklistedLocations
        );

        // Assert that the condition does not trigger fraud detection
        Assertions.assertFalse(result.isFraudulent);
        Assertions.assertFalse(result.verificationRequired);
        Assertions.assertEquals(0, result.riskScore);
    }

}