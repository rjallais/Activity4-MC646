package activity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class FraudDetectionSystem {

    public static class Transaction {
        double amount;
        LocalDateTime timestamp;
        String location;

        public Transaction(double amount, LocalDateTime timestamp, String location) {
            this.amount = amount;
            this.timestamp = timestamp;
            this.location = location;
        }
    }

    public static class FraudCheckResult {
        boolean isFraudulent;
        boolean isBlocked;
        boolean verificationRequired;
        int riskScore;

        public FraudCheckResult(boolean isFraudulent, boolean isBlocked, boolean verificationRequired, int riskScore) {
            this.isFraudulent = isFraudulent;
            this.isBlocked = isBlocked;
            this.verificationRequired = verificationRequired;
            this.riskScore = riskScore;
        }
    }

    public FraudCheckResult checkForFraud(Transaction currentTransaction, List<Transaction> previousTransactions, List<String> blacklistedLocations) {
        boolean isFraudulent = false;
        boolean isBlocked = false;
        boolean verificationRequired = false;
        int riskScore = 0;

        // Check transaction amount
        if (currentTransaction.amount > 10000) {
            isFraudulent = true;
            verificationRequired = true;
            riskScore += 50;
        }

        // Check for excessive transactions in the last hour
        int recentTransactionCount = 0;
        for (Transaction transaction : previousTransactions) {
            if (Duration.between(transaction.timestamp, currentTransaction.timestamp).toMinutes() <= 60) {
                recentTransactionCount++;
            }
        }
        if (recentTransactionCount > 10) {
            isBlocked = true;
            riskScore += 30;
        }

        // Check for location change within a short time frame
        if (!previousTransactions.isEmpty()) {
            Transaction lastTransaction = previousTransactions.get(previousTransactions.size() - 1);
            long minutesSinceLastTransaction = Duration.between(lastTransaction.timestamp, currentTransaction.timestamp).toMinutes();
            if (minutesSinceLastTransaction < 30 && !lastTransaction.location.equals(currentTransaction.location)) {
                isFraudulent = true;
                verificationRequired = true;
                riskScore += 20;
            }
        }

        // Blacklist check
        if (blacklistedLocations.contains(currentTransaction.location)) {
            isBlocked = true;
            riskScore = 100;
        }

        return new FraudCheckResult(isFraudulent, isBlocked, verificationRequired, riskScore);
    }
}
