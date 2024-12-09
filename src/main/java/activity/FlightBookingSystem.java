package activity;

import java.time.Duration;
import java.time.LocalDateTime;

public class FlightBookingSystem {


    public static class BookingResult {
        boolean confirmation;
        double totalPrice;
        double refundAmount;
        boolean pointsUsed;

        public BookingResult(boolean confirmation, double totalPrice, double refundAmount, boolean pointsUsed) {
            this.confirmation = confirmation;
            this.totalPrice = totalPrice;
            this.refundAmount = refundAmount;
            this.pointsUsed = pointsUsed;
        }
    }

    public BookingResult bookFlight(int passengers, LocalDateTime bookingTime, int availableSeats,
                                    double currentPrice, int previousSales, boolean isCancellation,
                                    LocalDateTime departureTime, int rewardPointsAvailable) {
        double finalPrice = 0;
        double refundAmount = 0;
        boolean confirmation = false;
        boolean pointsUsed = false;

        // Check if enough seats are available
        if (passengers > availableSeats) {
            return new BookingResult(confirmation, finalPrice, refundAmount, pointsUsed);
        }

        // Dynamic pricing based on sales and demand index
        double priceFactor = (previousSales / 100.0) * 0.8;
        finalPrice = currentPrice * priceFactor * passengers;

        // Last-minute fee
        long hoursToDeparture = Duration.between(bookingTime, departureTime).toHours();
        if (hoursToDeparture < 24) {
            finalPrice += 100;
        }

        // Group booking discount
        if (passengers > 4) {
            finalPrice *= 0.95;  // 5% discount
        }

        // Reward points redemption
        if (rewardPointsAvailable > 0) {
            finalPrice -= rewardPointsAvailable * 0.01;
            pointsUsed = true;
        }

        // Cancellations
        if (isCancellation) {
            if (hoursToDeparture >= 48) {
                refundAmount = finalPrice;
            } else {
                refundAmount = finalPrice * 0.5;
            }
            return new BookingResult(false, 0, refundAmount, false);
        }
        confirmation = true;

        return new BookingResult(confirmation, finalPrice, refundAmount, pointsUsed);
    }
}
