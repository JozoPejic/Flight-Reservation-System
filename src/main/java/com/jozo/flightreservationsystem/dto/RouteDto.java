package com.jozo.flightreservationsystem.dto;

import com.jozo.flightreservationsystem.model.Flight;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RouteDto {

    private List<Flight> flights;
    private BigDecimal totalPrice;
    private int totalDuration; // in minutes
    private int stops; // number of stops
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String origin;
    private String destination;
    private Map<String, SeatClassOption> seatClassOptions; // Key: seat class name, Value: option details

    public RouteDto(List<Flight> flights) {
        this.flights = flights;
        this.stops = Math.max(0, flights.size() - 1);
        
        if (!flights.isEmpty()) {
            this.departureTime = flights.get(0).getDepartureTime();
            this.arrivalTime = flights.get(flights.size() - 1).getArrivalTime();
            this.origin = flights.get(0).getOrigin();
            this.destination = flights.get(flights.size() - 1).getDestination();
            
            // Calculate total duration
            this.totalDuration = (int) java.time.Duration.between(departureTime, arrivalTime).toMinutes();
        }
    }
}
