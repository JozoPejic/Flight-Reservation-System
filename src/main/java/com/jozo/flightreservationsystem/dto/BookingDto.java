package com.jozo.flightreservationsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class BookingDto {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotEmpty(message = "Booking items are required")
    private List<BookingItemDto> bookingItems;

    private BigDecimal totalPrice;

    private String status = "PENDING";
}
