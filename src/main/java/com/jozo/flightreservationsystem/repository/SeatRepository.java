package com.jozo.flightreservationsystem.repository;

import com.jozo.flightreservationsystem.model.Seat;
import com.jozo.flightreservationsystem.model.Seat.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

    // Find seats by airplane
    List<Seat> findByAirplaneId(Integer airplaneId);

    // Find seats by airplane and seat class
    List<Seat> findByAirplaneIdAndSeatClass(Integer airplaneId, SeatClass seatClass);

    // Find seats by airplane ordered by seat number
    List<Seat> findByAirplaneIdOrderBySeatNumberAsc(Integer airplaneId);

    // Count seats by airplane and seat class
    int countByAirplaneIdAndSeatClass(Integer airplaneId, SeatClass seatClass);
}
