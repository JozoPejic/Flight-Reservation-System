package com.jozo.flightreservationsystem.repository;

import com.jozo.flightreservationsystem.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Integer> {

    // Find flights by origin
    List<Flight> findByOrigin(String origin);

    // Find flights by destination
    List<Flight> findByDestination(String destination);

    // Find flights by origin and destination
    List<Flight> findByOriginAndDestination(String origin, String destination);

    // Find flights by airplane
    List<Flight> findByAirplaneId(Integer airplaneId);

    // Find flights by departure time range
    List<Flight> findByDepartureTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // Find flights by departure time after specific date
    List<Flight> findByDepartureTimeAfter(LocalDateTime departureTime);

    // Find flights by departure time before specific date
    List<Flight> findByDepartureTimeBefore(LocalDateTime departureTime);
}
