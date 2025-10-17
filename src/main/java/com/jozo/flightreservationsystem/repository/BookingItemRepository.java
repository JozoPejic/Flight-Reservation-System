package com.jozo.flightreservationsystem.repository;

import com.jozo.flightreservationsystem.model.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Integer> {

    // Find booking items by booking ID
    List<BookingItem> findByBookingId(Integer bookingId);

    // Find booking items by flight seat ID
    List<BookingItem> findByFlightSeatId(Integer flightSeatId);

    // Find booking items by price range
    List<BookingItem> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find booking items with price less than specified amount
    List<BookingItem> findByPriceLessThan(BigDecimal price);
}
