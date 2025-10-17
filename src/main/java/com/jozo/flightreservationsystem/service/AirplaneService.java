package com.jozo.flightreservationsystem.service;

import com.jozo.flightreservationsystem.model.Airplane;
import com.jozo.flightreservationsystem.repository.AirplaneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AirplaneService {

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private SeatService seatService;

    @Transactional
    public Airplane saveAirplane(Airplane airplane) {
        Airplane savedAirplane = airplaneRepository.save(airplane);
        seatService.createSeatsForAirplane(savedAirplane);
        return savedAirplane;
    }

    public List<Airplane> getAllAirplanes() {
        return airplaneRepository.findAll();
    }

    public Optional<Airplane> getAirplaneById(int id) {
        return airplaneRepository.findById(id);
    }

    public Optional<Airplane> getAirplaneByRegistrationNumber(String registrationNumber) {
        return airplaneRepository.findByRegistrationNumber(registrationNumber);
    }

    public boolean existsByRegistrationNumber(String registrationNumber) {
        return airplaneRepository.existsByRegistrationNumber(registrationNumber);
    }

    public void deleteAirplane(int id) {
        airplaneRepository.deleteById(id);
    }

    public List<Airplane> getAirplanesByManufacturer(String manufacturer) {
        return airplaneRepository.findByManufacturer(manufacturer);
    }

    public List<Airplane> getAirplanesByType(Airplane.AirplaneType type) {
        return airplaneRepository.findByType(type);
    }
}
