package com.jozo.flightreservationsystem.service;

import com.jozo.flightreservationsystem.model.Airplane;
import com.jozo.flightreservationsystem.model.Flight;
import com.jozo.flightreservationsystem.repository.AirplaneRepository;
import com.jozo.flightreservationsystem.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private FlightSeatService flightSeatService;

    @Transactional
    public Flight saveFlight(Flight flight) {
        Flight savedFlight = flightRepository.save(flight);
        flightSeatService.createFlightSeatsForFlight(savedFlight);
        return savedFlight;
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Optional<Flight> getFlightById(Integer id) {
        return flightRepository.findById(id);
    }

    public List<Airplane> getAllAirplanes() {
        return airplaneRepository.findAll();
    }

    public Optional<Airplane> getAirplaneById(Integer id) {
        return airplaneRepository.findById(id);
    }

    public List<Flight> getFlightsByOrigin(String origin) {
        return flightRepository.findByOrigin(origin);
    }

    public List<Flight> getFlightsByDestination(String destination) {
        return flightRepository.findByDestination(destination);
    }

    public List<Flight> getFlightsByOriginAndDestination(String origin, String destination) {
        return flightRepository.findByOriginAndDestination(origin, destination);
    }
}
