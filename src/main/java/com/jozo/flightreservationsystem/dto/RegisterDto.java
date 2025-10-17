package com.jozo.flightreservationsystem.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RegisterDto {

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    @Email
    private String email;

    @Size(min = 8, message = "Minimum password length is 8 characters")
    private String password;

    private String matchingPassword;

}
