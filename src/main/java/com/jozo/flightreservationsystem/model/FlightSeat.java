package com.jozo.flightreservationsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flight_seat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlightSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FlightSeatStatus status;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    @OneToMany(mappedBy = "flightSeat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingItem> bookingItems;

    public enum FlightSeatStatus {
        AVAILABLE, RESERVED, BOOKED
    }
}
