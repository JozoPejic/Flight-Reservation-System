package com.jozo.flightreservationsystem.service;

import com.jozo.flightreservationsystem.model.Airplane;
import com.jozo.flightreservationsystem.model.Seat;
import com.jozo.flightreservationsystem.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    public void createSeatsForAirplane(Airplane airplane) {
        List<Seat> seats = new ArrayList<>();

        int rows = airplane.getRows();
        int seatsPerRow = airplane.getSeatsPerRow();
        

        int totalSeats = rows * seatsPerRow;
        int firstClassSeats = 0;
        int businessClassSeats = 0;
        int economyClassSeats = totalSeats;
        
        if (airplane.getType() == Airplane.AirplaneType.SMALL) {
            businessClassSeats = (int) (totalSeats * 0.2);
            economyClassSeats = totalSeats - businessClassSeats;
        } else {
            firstClassSeats = (int) (totalSeats * 0.1);
            businessClassSeats = (int) (totalSeats * 0.3);
            economyClassSeats = totalSeats - firstClassSeats - businessClassSeats;
        }

        int seatCounter = 0;
        for (int row = 1; row <= rows; row++) {
            for (int seatPos = 0; seatPos < seatsPerRow; seatPos++) {
                Seat seat = new Seat();
                seat.setAirplane(airplane);
                seat.setSeatNumber(generateSeatNumber(row, seatPos, seatsPerRow));

                if (seatCounter < firstClassSeats) {
                    seat.setSeatClass(Seat.SeatClass.FIRST);
                } else if (seatCounter < firstClassSeats + businessClassSeats) {
                    seat.setSeatClass(Seat.SeatClass.BUSINESS);
                } else {
                    seat.setSeatClass(Seat.SeatClass.ECONOMY);
                }
                
                seats.add(seat);
                seatCounter++;
            }
        }

        seatRepository.saveAll(seats);
    }
    
    private String generateSeatNumber(int row, int seatPos, int seatsPerRow) {
        char[] seatLetters;
        
        if (seatsPerRow == 4) {
            seatLetters = new char[]{'A', 'B', 'C', 'D'};
        } else {
            seatLetters = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G'};
        }
        
        return row + String.valueOf(seatLetters[seatPos]);
    }
}
