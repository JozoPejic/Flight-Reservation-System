package com.jozo.flightreservationsystem.controller;

import com.jozo.flightreservationsystem.model.Flight;
import com.jozo.flightreservationsystem.model.FlightSeat;
import com.jozo.flightreservationsystem.model.Seat;
import com.jozo.flightreservationsystem.service.FlightService;
import com.jozo.flightreservationsystem.service.FlightSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightSeatService flightSeatService;

    @GetMapping("/flight/{flightId}")
    public String showFlightSeats(@PathVariable int flightId,
                                  @RequestParam(required = false) String seatClass,
                                  @SessionAttribute(name = "bookingWizard", required = false) com.jozo.flightreservationsystem.dto.BookingWizard wizard,
                                  Model model) {
        flightSeatService.getAllSeatsForFlight(flightId).forEach(fs -> {
            flightSeatService.releaseReservationIfExpired(fs.getId());
        });
        Optional<Flight> flightOpt = flightService.getFlightById(flightId);
        
        if (flightOpt.isEmpty()) {
            model.addAttribute("error", "Flight not found");
            return "error";
        }
        
        Flight flight = flightOpt.get();
        List<FlightSeat> allSeats = flightSeatService.getAllSeatsForFlight(flightId);

        if (seatClass != null && !seatClass.isEmpty()) {
            try {
                Seat.SeatClass selectedClass = Seat.SeatClass.valueOf(seatClass);
                allSeats = allSeats.stream()
                        .filter(flightSeat -> flightSeat.getSeat().getSeatClass() == selectedClass)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
            }
        }

        Map<Seat.SeatClass, List<FlightSeat>> seatsByClass = allSeats.stream()
                .collect(Collectors.groupingBy(flightSeat -> flightSeat.getSeat().getSeatClass()));

        for (Map.Entry<Seat.SeatClass, List<FlightSeat>> entry : seatsByClass.entrySet()) {
            entry.getValue().sort((s1, s2) -> {
                String seat1 = s1.getSeat().getSeatNumber();
                String seat2 = s2.getSeat().getSeatNumber();
                return seat1.compareTo(seat2);
            });
        }

        Map<Seat.SeatClass, Map<Integer, List<FlightSeat>>> seatsByClassRows = new HashMap<>();
        for (Map.Entry<Seat.SeatClass, List<FlightSeat>> entry : seatsByClass.entrySet()) {
            Map<Integer, List<FlightSeat>> byRow = entry.getValue().stream()
                    .collect(Collectors.groupingBy(fs -> {
                        String num = fs.getSeat().getSeatNumber().replaceAll("[^0-9]", "");
                        try { return Integer.parseInt(num); } catch (NumberFormatException e) { return 0; }
                    }));
            Map<Integer, List<FlightSeat>> orderedRows = new java.util.TreeMap<>(byRow);
            orderedRows.replaceAll((row, seats) -> {
                seats.sort((a, b) -> a.getSeat().getSeatNumber().compareTo(b.getSeat().getSeatNumber()));
                return seats;
            });
            seatsByClassRows.put(entry.getKey(), new java.util.LinkedHashMap<>(orderedRows));
        }

        List<FlightSeat> availableSeats = allSeats.stream()
                .filter(fs -> fs.getStatus() == FlightSeat.FlightSeatStatus.AVAILABLE)
                .collect(Collectors.toList());
        List<FlightSeat> reservedSeats = allSeats.stream()
                .filter(fs -> fs.getStatus() == FlightSeat.FlightSeatStatus.RESERVED)
                .collect(Collectors.toList());
        List<FlightSeat> bookedSeats = allSeats.stream()
                .filter(fs -> fs.getStatus() == FlightSeat.FlightSeatStatus.BOOKED)
                .collect(Collectors.toList());

        model.addAttribute("flight", flight);
        model.addAttribute("seatsByClass", seatsByClass);
        model.addAttribute("airplane", flight.getAirplane());
        model.addAttribute("selectedSeatClass", seatClass);
        model.addAttribute("allSeats", allSeats);
        model.addAttribute("availableSeats", availableSeats);
        model.addAttribute("reservedSeats", reservedSeats);
        model.addAttribute("bookedSeats", bookedSeats);
        model.addAttribute("seatsByClassRows", seatsByClassRows);
        boolean hasWizard = wizard != null;
        boolean hasNextSegment = hasWizard && wizard.hasNext();
        model.addAttribute("hasWizard", hasWizard);
        model.addAttribute("hasNextSegment", hasNextSegment);
        
        return "reservations/flight_seats";
    }

    @GetMapping("/next")
    public String nextSegment(@SessionAttribute(name = "bookingWizard", required = false) com.jozo.flightreservationsystem.dto.BookingWizard wizard) {
        if (wizard == null) {
            return "redirect:/booking/search";
        }
        if (wizard.hasNext()) {
            wizard.advance();
            int flightId = wizard.getCurrentFlightId();
            String seatClass = wizard.getCurrentSeatClass();
            return "redirect:/reservations/flight/" + flightId + "?seatClass=" + seatClass;
        }
        return "redirect:/booking/summary";
    }

    @PostMapping("/reserve/{flightSeatId}")
    @ResponseBody
    public String reserveSeat(@PathVariable int flightSeatId) {
        boolean success = flightSeatService.reserveSeat(flightSeatId, 1); // TODO: Get actual user ID from session
        return success ? "SUCCESS" : "FAILED";
    }

    @PostMapping("/confirm/{flightSeatId}")
    @ResponseBody
    public String confirmReservation(@PathVariable int flightSeatId,
                                     @SessionAttribute(name = "bookingWizard", required = false) com.jozo.flightreservationsystem.dto.BookingWizard wizard) {
        boolean success = flightSeatService.confirmReservation(flightSeatId);
        if (success && wizard != null) {
            wizard.addSelectedSeatId(flightSeatId);
        }
        return success ? "SUCCESS" : "FAILED";
    }

    @PostMapping("/cancel/{flightSeatId}")
    @ResponseBody
    public String cancelReservation(@PathVariable int flightSeatId) {
        boolean success = flightSeatService.cancelReservation(flightSeatId);
        return success ? "SUCCESS" : "FAILED";
    }
    
    private SeatMapData createSeatMap(List<FlightSeat> flightSeats, com.jozo.flightreservationsystem.model.Airplane airplane) {
        int rows = airplane.getRows();
        int seatsPerRow = airplane.getSeatsPerRow();

        SeatInfo[][] seatMap = new SeatInfo[rows][seatsPerRow];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < seatsPerRow; col++) {
                seatMap[row][col] = new SeatInfo();
            }
        }

        for (FlightSeat flightSeat : flightSeats) {
            String seatNumber = flightSeat.getSeat().getSeatNumber();
            int row = extractRowNumber(seatNumber) - 1;
            int col = extractColumnIndex(seatNumber);
            
            if (row >= 0 && row < rows && col >= 0 && col < seatsPerRow) {
                seatMap[row][col] = new SeatInfo(
                    flightSeat.getId(),
                    seatNumber,
                    flightSeat.getStatus(),
                    flightSeat.getPrice()
                );
            }
        }
        
        return new SeatMapData(seatMap, rows, seatsPerRow);
    }
    
    private int extractRowNumber(String seatNumber) {
        String rowStr = seatNumber.replaceAll("[A-Z]", "");
        return Integer.parseInt(rowStr);
    }
    
    private int extractColumnIndex(String seatNumber) {
        String letter = seatNumber.replaceAll("[0-9]", "");
        return letter.charAt(0) - 'A';
    }

    public static class SeatMapData {
        private SeatInfo[][] seatMap;
        private int rows;
        private int seatsPerRow;
        
        public SeatMapData(SeatInfo[][] seatMap, int rows, int seatsPerRow) {
            this.seatMap = seatMap;
            this.rows = rows;
            this.seatsPerRow = seatsPerRow;
        }
        
        public SeatInfo[][] getSeatMap() { return seatMap; }
        public int getRows() { return rows; }
        public int getSeatsPerRow() { return seatsPerRow; }
    }
    
    public static class SeatInfo {
        private int flightSeatId;
        private String seatNumber;
        private com.jozo.flightreservationsystem.model.FlightSeat.FlightSeatStatus status;
        private java.math.BigDecimal price;
        private boolean isEmpty;
        
        public SeatInfo() {
            this.isEmpty = true;
        }
        
        public SeatInfo(int flightSeatId, String seatNumber, 
                       com.jozo.flightreservationsystem.model.FlightSeat.FlightSeatStatus status, 
                       java.math.BigDecimal price) {
            this.flightSeatId = flightSeatId;
            this.seatNumber = seatNumber;
            this.status = status;
            this.price = price;
            this.isEmpty = false;
        }

        public int getFlightSeatId() { return flightSeatId; }
        public String getSeatNumber() { return seatNumber; }
        public com.jozo.flightreservationsystem.model.FlightSeat.FlightSeatStatus getStatus() { return status; }
        public java.math.BigDecimal getPrice() { return price; }
        public boolean isEmpty() { return isEmpty; }
    }
}
