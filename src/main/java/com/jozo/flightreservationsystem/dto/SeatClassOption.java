package com.jozo.flightreservationsystem.dto;

import com.jozo.flightreservationsystem.model.Seat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SeatClassOption {
    
    private Seat.SeatClass seatClass;
    private BigDecimal price;
    private int availableSeats;
    private String displayName;
    
    public SeatClassOption(Seat.SeatClass seatClass, BigDecimal basePrice, int availableSeats) {
        this.seatClass = seatClass;
        this.availableSeats = availableSeats;
        this.displayName = getDisplayName(seatClass);
        
        // Calculate price based on seat class
        switch (seatClass) {
            case FIRST:
                this.price = basePrice.multiply(new BigDecimal("1.6")); // 60% more expensive
                break;
            case BUSINESS:
                this.price = basePrice.multiply(new BigDecimal("1.3")); // 30% more expensive
                break;
            case ECONOMY:
            default:
                this.price = basePrice; // Base price
                break;
        }
    }
    
    private String getDisplayName(Seat.SeatClass seatClass) {
        switch (seatClass) {
            case FIRST:
                return "First Class";
            case BUSINESS:
                return "Business Class";
            case ECONOMY:
                return "Economy Class";
            default:
                return "Unknown";
        }
    }
}
