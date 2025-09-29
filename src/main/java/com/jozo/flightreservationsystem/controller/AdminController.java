package com.jozo.flightreservationsystem.controller;

import com.jozo.flightreservationsystem.dto.AirplaneDto;
import com.jozo.flightreservationsystem.dto.FlightDto;
import com.jozo.flightreservationsystem.model.Airplane;
import com.jozo.flightreservationsystem.model.Flight;
import com.jozo.flightreservationsystem.service.AirplaneService;
import com.jozo.flightreservationsystem.service.FlightService;
import com.jozo.flightreservationsystem.service.FlightSeatService;
import com.jozo.flightreservationsystem.model.FlightSeat;
import com.jozo.flightreservationsystem.model.Seat;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AirplaneService airplaneService;

    @Autowired
    private FlightService flightService;
    
    @Autowired
    private FlightSeatService flightSeatService;

    @GetMapping("/airplanes")
    public String showAddAirplaneForm(Model model) {
        AirplaneDto airplaneDto = new AirplaneDto();
        model.addAttribute("airplaneDto", airplaneDto);
        model.addAttribute("success", false);
        model.addAttribute("airplaneTypes", Arrays.asList(Airplane.AirplaneType.values()));
        return "admin/add_airplane";
    }

    @PostMapping("/airplanes")
    public String addAirplane(Model model, @Valid AirplaneDto airplaneDto, BindingResult bindingResult) {

        if (airplaneService.existsByRegistrationNumber(airplaneDto.getRegistrationNumber())) {
            bindingResult.rejectValue("registrationNumber", "error.registration.exists", "Registration number already exists");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("airplaneDto", airplaneDto);
            model.addAttribute("airplaneTypes", Arrays.asList(Airplane.AirplaneType.values()));
            return "admin/add_airplane";
        }

        try {
            Airplane airplane = new Airplane(
                airplaneDto.getManufacturer(),
                airplaneDto.getModel(),
                airplaneDto.getType(),
                airplaneDto.getRegistrationNumber()
            );

            airplaneService.saveAirplane(airplane);

            model.addAttribute("airplaneDto", new AirplaneDto());
            model.addAttribute("success", true);
            model.addAttribute("airplaneTypes", Arrays.asList(Airplane.AirplaneType.values()));
            
        } catch (Exception e) {
            bindingResult.rejectValue("registrationNumber", "error.save.failed", "Failed to save airplane: " + e.getMessage());
            model.addAttribute("airplaneDto", airplaneDto);
            model.addAttribute("airplaneTypes", Arrays.asList(Airplane.AirplaneType.values()));
            return "admin/add_airplane";
        }

        return "admin/add_airplane";
    }

    @GetMapping("/flights")
    public String showAddFlightForm(Model model) {
        FlightDto flightDto = new FlightDto();
        model.addAttribute("flightDto", flightDto);
        model.addAttribute("success", false);
        model.addAttribute("airplanes", flightService.getAllAirplanes());
        return "admin/add_flight";
    }

    @PostMapping("/flights")
    public String addFlight(Model model, @Valid FlightDto flightDto, BindingResult bindingResult) {

        if (flightDto.getDepartureTime() != null && flightDto.getArrivalTime() != null) {
            if (flightDto.getArrivalTime().isBefore(flightDto.getDepartureTime())) {
                bindingResult.rejectValue("arrivalTime", "error.arrival.before.departure", "Arrival time must be after departure time");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("flightDto", flightDto);
            model.addAttribute("airplanes", flightService.getAllAirplanes());
            return "admin/add_flight";
        }

        try {
            Airplane airplane = flightService.getAirplaneById(flightDto.getAirplaneId())
                    .orElseThrow(() -> new RuntimeException("Airplane not found"));

            Flight flight = new Flight();
            flight.setFlightNumber(flightDto.getFlightNumber());
            flight.setAirplane(airplane);
            flight.setOrigin(flightDto.getOrigin());
            flight.setDestination(flightDto.getDestination());
            flight.setDepartureTime(flightDto.getDepartureTime());
            flight.setArrivalTime(flightDto.getArrivalTime());
            flight.setBasePrice(flightDto.getBasePrice());

            flightService.saveFlight(flight);

            model.addAttribute("flightDto", new FlightDto());
            model.addAttribute("success", true);
            model.addAttribute("airplanes", flightService.getAllAirplanes());
            
        } catch (Exception e) {
            bindingResult.rejectValue("airplaneId", "error.save.failed", "Failed to save flight: " + e.getMessage());
            model.addAttribute("flightDto", flightDto);
            model.addAttribute("airplanes", flightService.getAllAirplanes());
            return "admin/add_flight";
        }

        return "admin/add_flight";
    }

    @GetMapping("/flights/list")
    public String listFlights(Model model) {
        model.addAttribute("flights", flightService.getAllFlights());
        return "admin/flight_list";
    }
    
    @GetMapping("/flights/{flightId}/seats")
    public String showFlightSeats(@PathVariable int flightId, Model model) {
        Optional<Flight> flightOpt = flightService.getFlightById(flightId);
        
        if (flightOpt.isEmpty()) {
            model.addAttribute("error", "Flight not found");
            return "error";
        }
        
        Flight flight = flightOpt.get();
        List<FlightSeat> allSeats = flightSeatService.getAllSeatsForFlight(flightId);

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
        
        model.addAttribute("flight", flight);
        model.addAttribute("seatsByClass", seatsByClass);
        model.addAttribute("seatsByClassRows", seatsByClassRows);
        model.addAttribute("airplane", flight.getAirplane());
        
        return "admin/flight_seats";
    }

    private SeatMapData createSeatMap(List<FlightSeat> seats, Airplane airplane) {
        int rows = airplane.getRows();
        int seatsPerRow = airplane.getSeatsPerRow();

        SeatInfo[][] seatMap = new SeatInfo[rows][seatsPerRow];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < seatsPerRow; j++) {
                seatMap[i][j] = new SeatInfo(true, null, null, null, null);
            }
        }

        for (FlightSeat flightSeat : seats) {
            int rowNum = extractRowNumber(flightSeat.getSeat().getSeatNumber());
            int colIndex = extractColumnIndex(flightSeat.getSeat().getSeatNumber());
            
            if (rowNum >= 0 && rowNum < rows && colIndex >= 0 && colIndex < seatsPerRow) {
                seatMap[rowNum][colIndex] = new SeatInfo(
                    false,
                    flightSeat.getId(),
                    flightSeat.getSeat().getSeatNumber(),
                    flightSeat.getStatus(),
                    flightSeat.getSeat().getSeatClass()
                );
            }
        }
        
        return new SeatMapData(seatMap, rows, seatsPerRow);
    }
    
    private int extractRowNumber(String seatNumber) {
        try {
            return Integer.parseInt(seatNumber.replaceAll("[^0-9]", "")) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private int extractColumnIndex(String seatNumber) {
        String letter = seatNumber.replaceAll("[^A-Z]", "");
        if (letter.length() > 0) {
            return letter.charAt(0) - 'A';
        }
        return -1;
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
        private boolean empty;
        private Integer flightSeatId;
        private String seatNumber;
        private FlightSeat.FlightSeatStatus status;
        private Seat.SeatClass seatClass;
        
        public SeatInfo(boolean empty, Integer flightSeatId, String seatNumber, 
                       FlightSeat.FlightSeatStatus status, Seat.SeatClass seatClass) {
            this.empty = empty;
            this.flightSeatId = flightSeatId;
            this.seatNumber = seatNumber;
            this.status = status;
            this.seatClass = seatClass;
        }
        
        public boolean isEmpty() { return empty; }
        public Integer getFlightSeatId() { return flightSeatId; }
        public String getSeatNumber() { return seatNumber; }
        public FlightSeat.FlightSeatStatus getStatus() { return status; }
        public Seat.SeatClass getSeatClass() { return seatClass; }
    }
}
