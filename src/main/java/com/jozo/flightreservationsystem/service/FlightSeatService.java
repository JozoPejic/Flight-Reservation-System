package com.jozo.flightreservationsystem.service;

import com.jozo.flightreservationsystem.model.Flight;
import com.jozo.flightreservationsystem.model.FlightSeat;
import com.jozo.flightreservationsystem.model.Seat;
import com.jozo.flightreservationsystem.repository.FlightSeatRepository;
import com.jozo.flightreservationsystem.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlightSeatService {

    @Autowired
    private FlightSeatRepository flightSeatRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Transactional
    public void createFlightSeatsForFlight(Flight flight) {
        List<Seat> seats = seatRepository.findByAirplaneId(flight.getAirplane().getId());
        
        for (Seat seat : seats) {
            FlightSeat flightSeat = new FlightSeat();
            flightSeat.setFlight(flight);
            flightSeat.setSeat(seat);
            flightSeat.setStatus(FlightSeat.FlightSeatStatus.AVAILABLE);
            flightSeat.setPrice(calculateSeatPrice(seat, flight));
            
            flightSeatRepository.save(flightSeat);
        }
    }

    private BigDecimal calculateSeatPrice(Seat seat, Flight flight) {
        BigDecimal economy = (flight.getBasePrice() != null) ? flight.getBasePrice() : new BigDecimal("100.00");
        switch (seat.getSeatClass()) {
            case FIRST:
                return economy.multiply(new BigDecimal("1.6"));
            case BUSINESS:
                return economy.multiply(new BigDecimal("1.3"));
            case ECONOMY:
            default:
                return economy;
        }
    }

    @Transactional
    public boolean reserveSeat(int flightSeatId, int userId) {
        Optional<FlightSeat> flightSeatOpt = flightSeatRepository.findById(flightSeatId);
        
        if (flightSeatOpt.isEmpty()) {
            return false;
        }
        
        FlightSeat flightSeat = flightSeatOpt.get();

        if (flightSeat.getStatus() != FlightSeat.FlightSeatStatus.AVAILABLE) {
            return false;
        }

        flightSeat.setStatus(FlightSeat.FlightSeatStatus.RESERVED);
        flightSeat.setReservedUntil(LocalDateTime.now().plusMinutes(5));
        
        flightSeatRepository.save(flightSeat);
        return true;
    }

    @Transactional
    public boolean confirmReservation(int flightSeatId) {
        Optional<FlightSeat> flightSeatOpt = flightSeatRepository.findById(flightSeatId);
        
        if (flightSeatOpt.isEmpty()) {
            return false;
        }
        
        FlightSeat flightSeat = flightSeatOpt.get();

        if (flightSeat.getStatus() != FlightSeat.FlightSeatStatus.RESERVED || 
            flightSeat.getReservedUntil().isBefore(LocalDateTime.now())) {
            return false;
        }

        flightSeat.setStatus(FlightSeat.FlightSeatStatus.BOOKED);
        flightSeat.setReservedUntil(null);
        
        flightSeatRepository.save(flightSeat);
        return true;
    }

    @Transactional
    public boolean extendReservation(int flightSeatId, int minutes) {
        Optional<FlightSeat> flightSeatOpt = flightSeatRepository.findById(flightSeatId);
        if (flightSeatOpt.isEmpty()) return false;
        FlightSeat flightSeat = flightSeatOpt.get();
        if (flightSeat.getStatus() != FlightSeat.FlightSeatStatus.RESERVED) return false;
        if (flightSeat.getReservedUntil() != null && flightSeat.getReservedUntil().isBefore(LocalDateTime.now())) return false;
        flightSeat.setReservedUntil(LocalDateTime.now().plusMinutes(minutes));
        flightSeatRepository.save(flightSeat);
        return true;
    }

    @Transactional
    public boolean cancelReservation(int flightSeatId) {
        Optional<FlightSeat> flightSeatOpt = flightSeatRepository.findById(flightSeatId);
        
        if (flightSeatOpt.isEmpty()) {
            return false;
        }
        
        FlightSeat flightSeat = flightSeatOpt.get();

        if (flightSeat.getStatus() != FlightSeat.FlightSeatStatus.RESERVED) {
            return false;
        }

        flightSeat.setStatus(FlightSeat.FlightSeatStatus.AVAILABLE);
        flightSeat.setReservedUntil(null);
        
        flightSeatRepository.save(flightSeat);
        return true;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<FlightSeat> expiredReservations = flightSeatRepository.findByStatusAndReservedUntilBefore(
            FlightSeat.FlightSeatStatus.RESERVED, now);
        
        for (FlightSeat flightSeat : expiredReservations) {
            flightSeat.setStatus(FlightSeat.FlightSeatStatus.AVAILABLE);
            flightSeat.setReservedUntil(null);
        }
        
        if (!expiredReservations.isEmpty()) {
            flightSeatRepository.saveAll(expiredReservations);
        }
    }

    @Transactional
    public void releaseReservationIfExpired(int flightSeatId) {
        flightSeatRepository.findById(flightSeatId).ifPresent(fs -> {
            if (fs.getStatus() == FlightSeat.FlightSeatStatus.RESERVED && fs.getReservedUntil() != null
                    && fs.getReservedUntil().isBefore(LocalDateTime.now())) {
                fs.setStatus(FlightSeat.FlightSeatStatus.AVAILABLE);
                fs.setReservedUntil(null);
                flightSeatRepository.save(fs);
            }
        });
    }

    public List<FlightSeat> getAvailableSeatsForFlight(int flightId) {
        return flightSeatRepository.findByFlightIdAndStatus(flightId, FlightSeat.FlightSeatStatus.AVAILABLE);
    }

    public List<FlightSeat> getReservedSeatsForFlight(int flightId) {
        return flightSeatRepository.findByFlightIdAndStatus(flightId, FlightSeat.FlightSeatStatus.RESERVED);
    }

    public List<FlightSeat> getBookedSeatsForFlight(int flightId) {
        return flightSeatRepository.findByFlightIdAndStatus(flightId, FlightSeat.FlightSeatStatus.BOOKED);
    }

    public List<FlightSeat> getAllSeatsForFlight(int flightId) {
        return flightSeatRepository.findByFlightId(flightId);
    }

    public Optional<FlightSeat> getFlightSeatById(int flightSeatId) {
        return flightSeatRepository.findById(flightSeatId);
    }
}
