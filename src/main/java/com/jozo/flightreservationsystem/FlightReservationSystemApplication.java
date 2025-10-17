package com.jozo.flightreservationsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlightReservationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightReservationSystemApplication.class, args);
    }

}
