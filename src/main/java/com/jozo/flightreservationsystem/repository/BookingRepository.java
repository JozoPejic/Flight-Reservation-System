package com.jozo.flightreservationsystem.repository;

import com.jozo.flightreservationsystem.model.Booking;
import com.jozo.flightreservationsystem.model.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Find bookings by user ID
    List<Booking> findByUserId(Integer userId);

    // Find bookings by user ID and status
    List<Booking> findByUserIdAndStatus(Integer userId, BookingStatus status);

    // Find bookings by status
    List<Booking> findByStatus(BookingStatus status);

    // Find bookings by booking date range
    List<Booking> findByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find bookings by user ID and booking date range
    List<Booking> findByUserIdAndBookingDateBetween(Integer userId, LocalDateTime startDate, LocalDateTime endDate);

    // Find expired pending bookings
    List<Booking> findByStatusAndBookingDateBefore(BookingStatus status, LocalDateTime before);

    // Find bookings by user ID ordered by booking date descending
    List<Booking> findByUserIdOrderByBookingDateDesc(Integer userId);

    // Count bookings by status
    long countByStatus(BookingStatus status);
}
