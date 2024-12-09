package activity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class FlightBookingSystemTest {

    private FlightBookingSystem bookingSystem;

    @BeforeEach
    void setUp() {
        bookingSystem = new FlightBookingSystem();
    }

    @Test
    void testSuccessfulBooking() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusDays(2);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(2, bookingTime, 5, 100.0, 50, false, departureTime, 0);

        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(80.0, result.totalPrice, 0.01);
        Assertions.assertEquals(0.0, result.refundAmount, 0.01);
        Assertions.assertFalse(result.pointsUsed);
    }

    @Test
    void testBookingWithInsufficientSeats() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusDays(2);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(6, bookingTime, 5, 100.0, 50, false, departureTime, 0);

        Assertions.assertFalse(result.confirmation);
        Assertions.assertEquals(0.0, result.totalPrice, 0.01);
    }

    @Test
    void testLastMinuteBooking() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusHours(12);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(1, bookingTime, 5, 100.0, 50, false, departureTime, 0);

        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(140.0, result.totalPrice, 0.01);  // 40 (base price) + 100 (last-minute fee)
    }

    @Test
    void testGroupBookingDiscount() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusDays(2);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(5, bookingTime, 10, 100.0, 50, false, departureTime, 0);

        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(190.0, result.totalPrice, 0.01);  // (100 * 0.4 * 5) * 0.95
    }

    @Test
    void testRewardPointsRedemption() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusDays(2);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(1, bookingTime, 5, 100.0, 50, false, departureTime, 1000);

        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(30.0, result.totalPrice, 0.01);  // 40 (base price) - 10 (reward points)
        Assertions.assertTrue(result.pointsUsed);
    }

    @Test
    void testCancellationWithFullRefund() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusDays(3);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(1, bookingTime, 5, 100.0, 50, true, departureTime, 0);

        Assertions.assertFalse(result.confirmation);
        Assertions.assertEquals(0.0, result.totalPrice, 0.01);
        Assertions.assertEquals(40.0, result.refundAmount, 0.01);
    }

    @Test
    void testCancellationWithPartialRefund() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusHours(36);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(1, bookingTime, 5, 100.0, 50, true, departureTime, 0);

        Assertions.assertFalse(result.confirmation);
        Assertions.assertEquals(0.0, result.totalPrice, 0.01);
        Assertions.assertEquals(20.0, result.refundAmount, 0.01);  // 50% of 40
    }

    @Test
    void testZeroPassengers() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusDays(2);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(0, bookingTime, 5, 100.0, 50, false, departureTime, 0);

        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(0.0, result.totalPrice, 0.01);
    }

    @Test
    void testNegativeRewardPoints() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusDays(2);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(1, bookingTime, 5, 100.0, 50, false, departureTime, -100);

        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(40.0, result.totalPrice, 0.01);
        Assertions.assertFalse(result.pointsUsed);
    }

    @Test
    void testExactly24HoursBeforeDeparture() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusHours(24);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(1, bookingTime, 5, 100.0, 50, false, departureTime, 0);

        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(40.0, result.totalPrice, 0.01);
    }

    @Test
    void testCancellationExactly48HoursBeforeDeparture() {
        LocalDateTime bookingTime = LocalDateTime.now();
        LocalDateTime departureTime = bookingTime.plusHours(48);
        FlightBookingSystem.BookingResult result = bookingSystem.bookFlight(1, bookingTime, 5, 100.0, 50, true, departureTime, 0);

        Assertions.assertFalse(result.confirmation);
        Assertions.assertEquals(0.0, result.totalPrice, 0.01);
        Assertions.assertEquals(40.0, result.refundAmount, 0.01);
    }
}
