package com.jozo.flightreservationsystem.dto;

import com.jozo.flightreservationsystem.model.Airplane;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirplaneDto {

    @NotEmpty(message = "Manufacturer is required")
    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    private String manufacturer;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @NotNull(message = "Type is required")
    private Airplane.AirplaneType type;

    @NotEmpty(message = "Registration number is required")
    @Size(max = 20, message = "Registration number must not exceed 20 characters")
    private String registrationNumber;
}
