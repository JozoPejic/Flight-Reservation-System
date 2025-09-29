package com.jozo.flightreservationsystem.repository;

import com.jozo.flightreservationsystem.model.Airplane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirplaneRepository extends JpaRepository<Airplane, Integer> {

    // Find airplane by registration number
    Optional<Airplane> findByRegistrationNumber(String registrationNumber);

    // Find airplanes by manufacturer
    List<Airplane> findByManufacturer(String manufacturer);

    // Find airplanes by model
    List<Airplane> findByModel(String model);

    // Find airplanes by type
    List<Airplane> findByType(Airplane.AirplaneType type);

    // Find airplanes by capacity range
    List<Airplane> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);

    // Find airplanes by manufacturer and model
    List<Airplane> findByManufacturerAndModel(String manufacturer, String model);

    // Check if registration number exists
    boolean existsByRegistrationNumber(String registrationNumber);

    // Custom query to find airplanes with available seats for a specific flight
    @Query("SELECT DISTINCT a FROM Airplane a " +
           "JOIN a.seats s " +
           "JOIN s.flightSeats fs " +
           "WHERE fs.flight.id = :flightId AND fs.status = 'AVAILABLE'")
    List<Airplane> findAirplanesWithAvailableSeatsForFlight(@Param("flightId") Long flightId);
}
