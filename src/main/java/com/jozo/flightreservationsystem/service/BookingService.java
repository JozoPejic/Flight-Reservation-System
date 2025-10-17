package com.jozo.flightreservationsystem.service;

import com.jozo.flightreservationsystem.dto.BookingDto;
import com.jozo.flightreservationsystem.dto.BookingItemDto;
import com.jozo.flightreservationsystem.model.*;
import com.jozo.flightreservationsystem.repository.BookingItemRepository;
import com.jozo.flightreservationsystem.repository.BookingRepository;
import com.jozo.flightreservationsystem.repository.FlightSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private FlightSeatRepository flightSeatRepository;

    @Autowired
    private AppUserService appUserService;

    public AppUser getUserByEmail(String email) {
        return appUserService.getUserByEmail(email).orElse(null);
    }

    @Transactional
    public Booking createBooking(BookingDto bookingDto) {
        AppUser user = appUserService.getUserById(bookingDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.PENDING);

        BigDecimal totalPrice = bookingDto.getBookingItems().stream()
                .map(BookingItemDto::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        booking.setTotalPrice(totalPrice);

        Booking savedBooking = bookingRepository.save(booking);

        for (BookingItemDto itemDto : bookingDto.getBookingItems()) {
            BookingItem bookingItem = new BookingItem();
            bookingItem.setBooking(savedBooking);
            bookingItem.setPrice(itemDto.getPrice());

            FlightSeat flightSeat = flightSeatRepository.findById(itemDto.getFlightSeatId())
                    .orElseThrow(() -> new RuntimeException("Flight seat not found"));

            if (flightSeat.getStatus() == FlightSeat.FlightSeatStatus.AVAILABLE ||
                flightSeat.getStatus() == FlightSeat.FlightSeatStatus.RESERVED) {
                flightSeat.setStatus(FlightSeat.FlightSeatStatus.BOOKED);
                flightSeat.setReservedUntil(null);
            } else if (flightSeat.getStatus() == FlightSeat.FlightSeatStatus.BOOKED) {
            } else {
                throw new RuntimeException("Seat cannot be booked right now: " + flightSeat.getSeat().getSeatNumber());
            }

            bookingItem.setFlightSeat(flightSeat);

            flightSeatRepository.save(flightSeat);
            bookingItemRepository.save(bookingItem);
        }

        return savedBooking;
    }

    @Transactional
    public boolean confirmBooking(int bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return false;
        }

        Booking booking = bookingOpt.get();
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            return false;
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        List<BookingItem> bookingItems = bookingItemRepository.findByBookingId(bookingId);
        for (BookingItem item : bookingItems) {
            FlightSeat flightSeat = item.getFlightSeat();
            flightSeat.setStatus(FlightSeat.FlightSeatStatus.BOOKED);
            flightSeat.setReservedUntil(null);
            flightSeatRepository.save(flightSeat);
        }

        return true;
    }

    @Transactional
    public boolean cancelBooking(int bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return false;
        }

        Booking booking = bookingOpt.get();
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            return false;
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        List<BookingItem> bookingItems = bookingItemRepository.findByBookingId(bookingId);
        for (BookingItem item : bookingItems) {
            FlightSeat flightSeat = item.getFlightSeat();
            flightSeat.setStatus(FlightSeat.FlightSeatStatus.AVAILABLE);
            flightSeat.setReservedUntil(null);
            flightSeatRepository.save(flightSeat);
        }

        return true;
    }

    @Transactional
    public boolean deleteBookingAndRelease(int bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return false;
        }

        List<BookingItem> bookingItems = bookingItemRepository.findByBookingId(bookingId);
        for (BookingItem item : bookingItems) {
            FlightSeat flightSeat = item.getFlightSeat();
            if (flightSeat != null) {
                flightSeat.setStatus(FlightSeat.FlightSeatStatus.AVAILABLE);
                flightSeat.setReservedUntil(null);
                flightSeatRepository.save(flightSeat);
            }
        }

        bookingItemRepository.deleteAll(bookingItems);
        bookingRepository.deleteById(bookingId);
        return true;
    }

    public List<Booking> getBookingsByUserId(int userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Optional<Booking> getBookingById(int bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<BookingItem> getBookingItemsByBookingId(int bookingId) {
        return bookingItemRepository.findByBookingId(bookingId);
    }

    @Transactional
    public void releaseExpiredBookings() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndBookingDateBefore(
                Booking.BookingStatus.PENDING, thirtyMinutesAgo);

        for (Booking booking : expiredBookings) {
            cancelBooking(booking.getId());
        }
    }
}
