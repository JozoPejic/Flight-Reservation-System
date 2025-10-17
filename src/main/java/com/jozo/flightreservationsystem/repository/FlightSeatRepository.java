package com.jozo.flightreservationsystem.repository;

import com.jozo.flightreservationsystem.model.FlightSeat;
import com.jozo.flightreservationsystem.model.FlightSeat.FlightSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightSeatRepository extends JpaRepository<FlightSeat, Integer> {

    // Find flight seats by flight ID
    List<FlightSeat> findByFlightId(Integer flightId);

    // Find flight seats by flight ID and status
    List<FlightSeat> findByFlightIdAndStatus(Integer flightId, FlightSeatStatus status);

    // Find flight seats by status
    List<FlightSeat> findByStatus(FlightSeatStatus status);

    // Find expired reservations
    List<FlightSeat> findByStatusAndReservedUntilBefore(FlightSeatStatus status, LocalDateTime before);

    // Find flight seat by flight ID and seat ID
    Optional<FlightSeat> findByFlightIdAndSeatId(Integer flightId, Integer seatId);

    // Count seats by flight ID and status
    int countByFlightIdAndStatus(Integer flightId, FlightSeatStatus status);

    // Find flight seats by price range
    List<FlightSeat> findByPriceBetween(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);

    // Find flight seats by seat class and status
    @Query("SELECT fs FROM FlightSeat fs " +
           "JOIN fs.seat s " +
           "WHERE fs.flight.id = :flightId AND s.seatClass = :seatClass AND fs.status = :status")
    List<FlightSeat> findByFlightIdAndSeatClassAndStatus(@Param("flightId") Integer flightId,
                                                          @Param("seatClass") String seatClass,
                                                          @Param("status") FlightSeatStatus status);
}
