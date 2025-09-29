package com.jozo.flightreservationsystem.controller;

import com.jozo.flightreservationsystem.dto.BookingDto;
import com.jozo.flightreservationsystem.dto.RouteDto;
import com.jozo.flightreservationsystem.dto.SeatClassOption;
import com.jozo.flightreservationsystem.dto.BookingWizard;
import com.jozo.flightreservationsystem.model.Booking;
import com.jozo.flightreservationsystem.model.BookingItem;
import com.jozo.flightreservationsystem.model.Flight;
import com.jozo.flightreservationsystem.service.BookingService;
import com.jozo.flightreservationsystem.service.FlightService;
import com.jozo.flightreservationsystem.service.FlightSeatService;
import com.jozo.flightreservationsystem.service.RouteSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
@SessionAttributes("bookingWizard")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RouteSearchService routeSearchService;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightSeatService flightSeatService;

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("origin", "");
        model.addAttribute("destination", "");
        model.addAttribute("departureDate", LocalDate.now().plusDays(1));
        return "booking/search";
    }

    @PostMapping("/search")
    public String searchRoutes(@RequestParam String origin,
                              @RequestParam String destination,
                              @RequestParam LocalDate departureDate,
                              Model model) {
        LocalDateTime departureDateTime = departureDate.atStartOfDay();
        List<RouteDto> routes = routeSearchService.searchRoutes(origin, destination, departureDateTime);
        
        model.addAttribute("origin", origin);
        model.addAttribute("destination", destination);
        model.addAttribute("departureDate", departureDate);
        model.addAttribute("routes", routes);
        
        return "booking/search";
    }

    @GetMapping("/select-classes")
    public String selectClasses(@RequestParam("flightIds") String flightIdsCsv, Model model) {
        List<Integer> flightIds = java.util.Arrays.stream(flightIdsCsv.split(","))
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();

        List<Map<String, SeatClassOption>> perFlightOptions = new java.util.ArrayList<>();
        List<Flight> flights = new java.util.ArrayList<>();
        for (Integer flightId : flightIds) {
            perFlightOptions.add(routeSearchService.getSeatClassOptionsForFlight(flightId));
            flightService.getFlightById(flightId).ifPresent(flights::add);
        }
        model.addAttribute("flightIds", flightIds);
        model.addAttribute("perFlightOptions", perFlightOptions);
        model.addAttribute("flights", flights);
        return "booking/select_classes";
    }

    @PostMapping("/review")
    public String reviewSelectedClasses(@RequestParam List<Integer> flightId,
                                       @RequestParam("selectedClass") List<String> selectedClass,
                                       Model model) {
        if (flightId == null || flightId.isEmpty()) {
            return "redirect:/booking/search";
        }
        BookingWizard wizard = new BookingWizard(flightId, selectedClass);
        model.addAttribute("bookingWizard", wizard);
        return "redirect:/booking/segment";
    }

    @GetMapping("/segment")
    public String segmentRedirect(@ModelAttribute("bookingWizard") BookingWizard wizard) {
        if (wizard == null || wizard.getFlightIds() == null || wizard.getFlightIds().isEmpty()) {
            return "redirect:/booking/search";
        }
        int flightId = wizard.getCurrentFlightId();
        String seatClass = wizard.getCurrentSeatClass();
        return "redirect:/reservations/flight/" + flightId + "?seatClass=" + seatClass;
    }

    @GetMapping("/summary")
    public String summary(@ModelAttribute("bookingWizard") BookingWizard wizard, Model model) {
        if (wizard == null) {
            return "redirect:/booking/search";
        }
        if (wizard.getSelectedSeatIds() == null || wizard.getSelectedSeatIds().size() < wizard.getFlightIds().size()) {
            for (int i = 0; i < wizard.getFlightIds().size(); i++) {
                if (wizard.getSelectedSeatIds() == null || wizard.getSelectedSeatIds().size() <= i || wizard.getSelectedSeatIds().get(i) == null) {
                    wizard.setCurrentIndex(i);
                    int flightId = wizard.getFlightIds().get(i);
                    String seatClass = wizard.getSelectedClasses().get(i);
                    return "redirect:/reservations/flight/" + flightId + "?seatClass=" + seatClass;
                }
            }
        }
        List<Flight> flights = new java.util.ArrayList<>();
        List<com.jozo.flightreservationsystem.model.FlightSeat> seats = new java.util.ArrayList<>();
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (Integer id : wizard.getFlightIds()) {
            flightService.getFlightById(id).ifPresent(flights::add);
        }
        for (Integer seatId : wizard.getSelectedSeatIds()) {
            flightSeatService.getFlightSeatById(seatId).ifPresent(seat -> {
                seats.add(seat);
            });
        }
        for (com.jozo.flightreservationsystem.model.FlightSeat fs : seats) {
            total = total.add(fs.getPrice());
        }
        model.addAttribute("flights", flights);
        model.addAttribute("seats", seats);
        model.addAttribute("totalPrice", total);
        model.addAttribute("wizard", wizard);
        return "booking/summary";
    }

    @PostMapping("/confirm")
    public String confirm(@ModelAttribute("bookingWizard") BookingWizard wizard,
                          org.springframework.security.core.Authentication authentication) {
        if (wizard == null || wizard.getFlightIds() == null || wizard.getSelectedSeatIds() == null) {
            return "redirect:/booking/search";
        }

        com.jozo.flightreservationsystem.dto.BookingDto bookingDto = new com.jozo.flightreservationsystem.dto.BookingDto();

        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        com.jozo.flightreservationsystem.model.AppUser user = bookingService.getUserByEmail(authentication.getName());
        if (user == null) {
            return "redirect:/auth/login";
        }
        bookingDto.setUserId(user.getId());

        java.util.List<com.jozo.flightreservationsystem.dto.BookingItemDto> items = new java.util.ArrayList<>();
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (int i = 0; i < wizard.getFlightIds().size(); i++) {
            Integer flightId = wizard.getFlightIds().get(i);
            Integer seatId = wizard.getSelectedSeatIds().size() > i ? wizard.getSelectedSeatIds().get(i) : null;
            if (seatId == null) continue;
            var fsOpt = flightSeatService.getFlightSeatById(seatId);
            if (fsOpt.isEmpty()) continue;
            var fs = fsOpt.get();
            if (fs.getStatus() == com.jozo.flightreservationsystem.model.FlightSeat.FlightSeatStatus.AVAILABLE) {
                flightSeatService.reserveSeat(seatId, user.getId());
                fs = flightSeatService.getFlightSeatById(seatId).get();
            }
            if (fs.getStatus() == com.jozo.flightreservationsystem.model.FlightSeat.FlightSeatStatus.RESERVED) {
                flightSeatService.extendReservation(seatId, 10);
            }
            com.jozo.flightreservationsystem.dto.BookingItemDto item = new com.jozo.flightreservationsystem.dto.BookingItemDto();
            item.setFlightId(flightId);
            item.setFlightSeatId(seatId);
            item.setPrice(fs.getPrice());
            items.add(item);
            total = total.add(fs.getPrice());
        }
        bookingDto.setBookingItems(items);
        bookingDto.setTotalPrice(total);

        Booking booking = bookingService.createBooking(bookingDto);
        bookingService.confirmBooking(booking.getId());
        return "redirect:/user/bookings";
    }

    @PostMapping("/create")
    @ResponseBody
    public String createBooking(@RequestBody BookingDto bookingDto) {
        try {
            Booking booking = bookingService.createBooking(bookingDto);
            return "SUCCESS:" + booking.getId();
        } catch (Exception e) {
            return "ERROR:" + e.getMessage();
        }
    }

    @PostMapping("/confirm/{bookingId}")
    @ResponseBody
    public String confirmBooking(@PathVariable int bookingId) {
        boolean success = bookingService.confirmBooking(bookingId);
        return success ? "SUCCESS" : "FAILED";
    }

    @PostMapping("/cancel/{bookingId}")
    @ResponseBody
    public String cancelBooking(@PathVariable int bookingId) {
        boolean success = bookingService.cancelBooking(bookingId);
        return success ? "SUCCESS" : "FAILED";
    }

    @PostMapping("/cancel/view/{bookingId}")
    public String cancelBookingView(@PathVariable int bookingId) {
        bookingService.deleteBookingAndRelease(bookingId);
        return "redirect:/";
    }

    @GetMapping("/my-bookings/{userId}")
    public String showUserBookings(@PathVariable int userId, Model model) {
        List<Booking> bookings = bookingService.getBookingsByUserId(userId);
        model.addAttribute("bookings", bookings);
        model.addAttribute("userId", userId);
        return "booking/my_bookings";
    }

    @GetMapping("/details/{bookingId}")
    public String showBookingDetails(@PathVariable int bookingId, Model model) {
        Booking booking = bookingService.getBookingById(bookingId).orElse(null);
        if (booking == null) {
            model.addAttribute("error", "Booking not found");
            return "error";
        }
        
        List<BookingItem> bookingItems = bookingService.getBookingItemsByBookingId(bookingId);
        
        model.addAttribute("booking", booking);
        model.addAttribute("bookingItems", bookingItems);
        
        return "booking/booking_details";
    }
}
