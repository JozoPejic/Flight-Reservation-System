package com.jozo.flightreservationsystem.service;

import com.jozo.flightreservationsystem.dto.RouteDto;
import com.jozo.flightreservationsystem.dto.SeatClassOption;
import com.jozo.flightreservationsystem.model.Flight;
import com.jozo.flightreservationsystem.model.FlightSeat;
import com.jozo.flightreservationsystem.model.Seat;
import com.jozo.flightreservationsystem.repository.FlightRepository;
import com.jozo.flightreservationsystem.repository.FlightSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteSearchService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightSeatRepository flightSeatRepository;

    private static final int MAX_STOPS = 2;
    private static final int MIN_CONNECTION_TIME = 60;
    private static final int MAX_CONNECTION_TIME = 720;

    public List<RouteDto> searchRoutes(String origin, String destination, LocalDateTime departureDate) {
        List<RouteDto> routes = new ArrayList<>();

        List<Flight> directFlights = flightRepository.findByOriginAndDestination(origin, destination);
        directFlights = filterFlightsByDate(directFlights, departureDate);

        if (!directFlights.isEmpty()) {
            List<Flight> availableFlights = directFlights.stream()
                    .filter(flight -> hasAvailableSeats(flight.getId()))
                    .collect(Collectors.toList());
            
            if (!availableFlights.isEmpty()) {
                RouteDto route = new RouteDto(availableFlights);
                route.setSeatClassOptions(createSeatClassOptionsForAllFlights(availableFlights));
                route.setTotalPrice(calculateRoutePrice(availableFlights));
                routes.add(route);
            }
        }

        boolean hasAggregatedDirect = !routes.isEmpty();
        if (routes.isEmpty() || routes.size() < 5) {
            List<RouteDto> connectingRoutes = searchConnectingRoutes(origin, destination, departureDate);
            if (hasAggregatedDirect) {
                connectingRoutes = connectingRoutes.stream()
                        .filter(r -> r.getFlights() != null && r.getFlights().size() > 1)
                        .collect(Collectors.toList());
            }
            routes.addAll(connectingRoutes);
        }

        routes.sort((r1, r2) -> {
            int priceCompare = r1.getTotalPrice().compareTo(r2.getTotalPrice());
            if (priceCompare != 0) return priceCompare;
            return Integer.compare(r1.getTotalDuration(), r2.getTotalDuration());
        });

        Map<String, RouteDto> unique = new LinkedHashMap<>();
        for (RouteDto r : routes) {
            String key = (r.getFlights() == null || r.getFlights().isEmpty())
                    ? "NO_FLIGHTS"
                    : r.getFlights().stream().map(f -> String.valueOf(f.getId())).collect(Collectors.joining("-"));
            unique.putIfAbsent(key, r);
        }

        return unique.values().stream().limit(10).collect(Collectors.toList());
    }

    private List<RouteDto> searchConnectingRoutes(String origin, String destination, LocalDateTime departureDate) {
        List<RouteDto> routes = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        Queue<RouteSearchNode> queue = new LinkedList<>();
        queue.offer(new RouteSearchNode(origin, departureDate, new ArrayList<>(), 0));

        while (!queue.isEmpty()) {
            RouteSearchNode current = queue.poll();
            
            if (current.getStops() > MAX_STOPS) continue;
            if (visited.contains(current.getCurrentAirport() + current.getStops())) continue;
            
            visited.add(current.getCurrentAirport() + current.getStops());

            List<Flight> flights = flightRepository.findByOrigin(current.getCurrentAirport());
            flights = filterFlightsByDate(flights, current.getDepartureTime().toLocalDate());

            for (Flight flight : flights) {
                if (current.getFlights().contains(flight)) continue;
                
                LocalDateTime flightDeparture = flight.getDepartureTime();
                LocalDateTime flightArrival = flight.getArrivalTime();

                if (current.getFlights().size() > 0) {
                    Flight lastFlight = current.getFlights().get(current.getFlights().size() - 1);
                    long connectionTime = java.time.Duration.between(lastFlight.getArrivalTime(), flightDeparture).toMinutes();
                    
                    if (connectionTime < MIN_CONNECTION_TIME || connectionTime > MAX_CONNECTION_TIME) {
                        continue;
                    }
                }

                List<Flight> newRoute = new ArrayList<>(current.getFlights());
                newRoute.add(flight);

                if (flight.getDestination().equals(destination)) {
                    if (hasAvailableSeats(flight.getId())) {
                        RouteDto route = new RouteDto(newRoute);
                        route.setSeatClassOptions(createSeatClassOptionsForAllFlights(newRoute));
                        route.setTotalPrice(calculateRoutePrice(newRoute));
                        routes.add(route);
                    }
                } else {
                    RouteSearchNode nextNode = new RouteSearchNode(
                        flight.getDestination(),
                        flightArrival,
                        newRoute,
                        current.getStops() + 1
                    );
                    queue.offer(nextNode);
                }
            }
        }

        return routes;
    }

    private List<Flight> filterFlightsByDate(List<Flight> flights, LocalDateTime departureDate) {
        return flights.stream()
                .filter(flight -> flight.getDepartureTime().toLocalDate().equals(departureDate.toLocalDate()))
                .collect(Collectors.toList());
    }

    private List<Flight> filterFlightsByDate(List<Flight> flights, java.time.LocalDate date) {
        return flights.stream()
                .filter(flight -> flight.getDepartureTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    private boolean hasAvailableSeats(int flightId) {
        return flightSeatRepository.countByFlightIdAndStatus(flightId, FlightSeat.FlightSeatStatus.AVAILABLE) > 0;
    }

    private java.math.BigDecimal calculateRoutePrice(List<Flight> flights) {
        return flights.stream()
                .map(flight -> java.math.BigDecimal.valueOf(100))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    private Map<String, SeatClassOption> createSeatClassOptions(int flightId) {
        Map<String, SeatClassOption> options = new HashMap<>();
        java.math.BigDecimal basePrice = java.math.BigDecimal.valueOf(100);

        List<FlightSeat> availableSeats = flightSeatRepository.findByFlightIdAndStatus(flightId, FlightSeat.FlightSeatStatus.AVAILABLE);

        Map<Seat.SeatClass, Integer> seatCounts = new HashMap<>();
        for (FlightSeat flightSeat : availableSeats) {
            Seat.SeatClass seatClass = flightSeat.getSeat().getSeatClass();
            seatCounts.put(seatClass, seatCounts.getOrDefault(seatClass, 0) + 1);
        }

        for (Seat.SeatClass seatClass : Seat.SeatClass.values()) {
            int availableCount = seatCounts.getOrDefault(seatClass, 0);
            if (availableCount > 0) {
                SeatClassOption option = new SeatClassOption(seatClass, basePrice, availableCount);
                options.put(seatClass.name(), option);
            }
        }
        
        return options;
    }

    public Map<String, SeatClassOption> getSeatClassOptionsForFlight(int flightId) {
        return createSeatClassOptions(flightId);
    }
    
    private Map<String, SeatClassOption> createSeatClassOptionsForAllFlights(List<Flight> flights) {
        Map<String, SeatClassOption> options = new HashMap<>();
        java.math.BigDecimal basePrice = java.math.BigDecimal.valueOf(100);

        Map<Seat.SeatClass, Integer> totalSeatCounts = new HashMap<>();
        
        for (Flight flight : flights) {
            List<FlightSeat> availableSeats = flightSeatRepository.findByFlightIdAndStatus(flight.getId(), FlightSeat.FlightSeatStatus.AVAILABLE);
            
            for (FlightSeat flightSeat : availableSeats) {
                Seat.SeatClass seatClass = flightSeat.getSeat().getSeatClass();
                totalSeatCounts.put(seatClass, totalSeatCounts.getOrDefault(seatClass, 0) + 1);
            }
        }

        for (Seat.SeatClass seatClass : Seat.SeatClass.values()) {
            int availableCount = totalSeatCounts.getOrDefault(seatClass, 0);
            if (availableCount > 0) {
                SeatClassOption option = new SeatClassOption(seatClass, basePrice, availableCount);
                options.put(seatClass.name(), option);
            }
        }
        
        return options;
    }
    
    private Map<String, SeatClassOption> createSeatClassOptionsForRoute(List<Flight> flights) {
        Map<String, SeatClassOption> options = new HashMap<>();
        java.math.BigDecimal basePrice = java.math.BigDecimal.valueOf(100);

        Map<Seat.SeatClass, Integer> minSeatCounts = new HashMap<>();

        for (Seat.SeatClass seatClass : Seat.SeatClass.values()) {
            minSeatCounts.put(seatClass, Integer.MAX_VALUE);
        }
        
        for (Flight flight : flights) {
            List<FlightSeat> availableSeats = flightSeatRepository.findByFlightIdAndStatus(flight.getId(), FlightSeat.FlightSeatStatus.AVAILABLE);

            Map<Seat.SeatClass, Integer> flightSeatCounts = new HashMap<>();
            for (FlightSeat flightSeat : availableSeats) {
                Seat.SeatClass seatClass = flightSeat.getSeat().getSeatClass();
                flightSeatCounts.put(seatClass, flightSeatCounts.getOrDefault(seatClass, 0) + 1);
            }

            for (Seat.SeatClass seatClass : Seat.SeatClass.values()) {
                int flightCount = flightSeatCounts.getOrDefault(seatClass, 0);
                int currentMin = minSeatCounts.get(seatClass);
                minSeatCounts.put(seatClass, Math.min(currentMin, flightCount));
            }
        }

        for (Seat.SeatClass seatClass : Seat.SeatClass.values()) {
            int availableCount = minSeatCounts.get(seatClass);
            if (availableCount > 0 && availableCount != Integer.MAX_VALUE) {
                SeatClassOption option = new SeatClassOption(seatClass, basePrice, availableCount);
                options.put(seatClass.name(), option);
            }
        }
        
        return options;
    }

    private static class RouteSearchNode {
        private String currentAirport;
        private LocalDateTime departureTime;
        private List<Flight> flights;
        private int stops;

        public RouteSearchNode(String currentAirport, LocalDateTime departureTime, List<Flight> flights, int stops) {
            this.currentAirport = currentAirport;
            this.departureTime = departureTime;
            this.flights = flights;
            this.stops = stops;
        }

        public String getCurrentAirport() { return currentAirport; }
        public LocalDateTime getDepartureTime() { return departureTime; }
        public List<Flight> getFlights() { return flights; }
        public int getStops() { return stops; }
    }
}
