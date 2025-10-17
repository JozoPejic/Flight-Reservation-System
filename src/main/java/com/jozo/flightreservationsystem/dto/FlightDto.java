package com.jozo.flightreservationsystem.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
public class FlightDto {

    @NotEmpty(message = "Flight number is required")
    private String flightNumber;

    @NotNull(message = "Airplane is required")
    private Integer airplaneId;

    @NotEmpty(message = "Origin is required")
    private String origin;

    @NotEmpty(message = "Destination is required")
    private String destination;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Base price (Economy) is required")
    private BigDecimal basePrice;
}
