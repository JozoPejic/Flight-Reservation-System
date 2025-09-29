package com.jozo.flightreservationsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "airplane")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Airplane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @Column(name = "model", nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AirplaneType type;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(name = "rows", nullable = false)
    private Integer rows;

    @Column(name = "seats_per_row", nullable = false)
    private Integer seatsPerRow;

    public Airplane(String manufacturer, String model, AirplaneType type, String registrationNumber) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.type = type;
        this.registrationNumber = registrationNumber;

        if (type == AirplaneType.SMALL) {
            this.rows = 10;
            this.seatsPerRow = 4;
            this.capacity = 40;
        } else {
            this.rows = 20;
            this.seatsPerRow = 7;
            this.capacity = 140;
        }
    }

    @OneToMany(mappedBy = "airplane", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;

    @OneToMany(mappedBy = "airplane", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Flight> flights;

    public enum AirplaneType {
        SMALL, LARGE
    }
}
