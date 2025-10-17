package com.jozo.flightreservationsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BookingItemDto {

    @NotNull(message = "Flight ID is required")
    private Integer flightId;

    @NotNull(message = "Flight seat ID is required")
    private Integer flightSeatId;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private String status = "RESERVED";
}
