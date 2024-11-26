package com.mouad.train.Booking;

import com.mouad.train.Enums.Enums;
import com.mouad.train.Schedules.TrainSchedules;
import com.mouad.train.Schedules.TrainSchedulesRepository;
import com.mouad.train.trains.Train;
import com.mouad.train.trains.TrainRepository;
import com.mouad.train.users.User;
import com.mouad.train.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainSchedulesRepository trainSchedulesRepository;

    @Autowired
    private TrainRepository trainRepository;

    // Get all bookings
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    // Get booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Integer id) {
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // Get bookings by train
    @GetMapping("/train/{trainId}")
    public ResponseEntity<?> getBookingsByTrain(@PathVariable Integer trainId) {
        try {
            Train train = trainRepository.findById(trainId)
                    .orElseThrow(() -> new RuntimeException("Train not found with ID: " + trainId));

            List<TrainSchedules> schedules = trainSchedulesRepository.findByTrain(train);
            List<Booking> bookings = schedules.stream()
                    .flatMap(schedule -> bookingRepository.findBySchedule(schedule).stream())
                    .toList();

            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Create a new booking
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking bookingRequest) {
        try {
            User user = validateUser(bookingRequest.getUser().getId());
            TrainSchedules schedule = validateSchedule(bookingRequest.getSchedule().getId());

            validateSeatAvailability(schedule, bookingRequest.getNumberOfSeats());

            bookingRequest.setUser(user);
            bookingRequest.setSchedule(schedule);
            bookingRequest.setBookingTime(LocalDateTime.now());
            bookingRequest.setStatus(Enums.BookingStatus.CONFIRMED);

            Booking savedBooking = bookingRepository.save(bookingRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Update a booking
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Integer id, @RequestBody Booking bookingRequest) {
        try {
            Booking existingBooking = validateBooking(id);
            User user = validateUser(bookingRequest.getUser().getId());
            TrainSchedules schedule = validateSchedule(bookingRequest.getSchedule().getId());

            // Calculate seat availability considering the current booking
            validateSeatAvailabilityForUpdate(schedule, bookingRequest.getNumberOfSeats(), existingBooking.getNumberOfSeats());

            existingBooking.setUser(user);
            existingBooking.setSchedule(schedule);
            existingBooking.setNumberOfSeats(bookingRequest.getNumberOfSeats());
            existingBooking.setBookingTime(LocalDateTime.now());
            existingBooking.setStatus(Enums.BookingStatus.CONFIRMED);

            Booking updatedBooking = bookingRepository.save(existingBooking);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Helper methods for validation
    private void validateSeatAvailabilityForUpdate(TrainSchedules schedule, Integer requestedSeats, Integer currentSeats) {
        int trainCapacity = schedule.getTrain().getCapacity();
        int totalBookedSeats = bookingRepository.findBySchedule(schedule)
                .stream()
                .mapToInt(Booking::getNumberOfSeats)
                .sum();

        // Adjust the booked seats by subtracting the current booking's seats
        int availableSeats = trainCapacity - (totalBookedSeats - currentSeats);

        if (requestedSeats > availableSeats) {
            throw new RuntimeException("Not enough available seats. Only " + availableSeats + " seats left.");
        }
    }


    // Update booking status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            Booking booking = validateBooking(id);
            Enums.BookingStatus bookingStatus = Enums.BookingStatus.valueOf(status.toUpperCase());

            booking.setStatus(bookingStatus);
            Booking updatedBooking = bookingRepository.save(booking);
            return ResponseEntity.ok(updatedBooking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid booking status: " + status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Delete a booking
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Integer id) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found with ID: " + id);
        }
        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper methods for validation
    private User validateUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    private TrainSchedules validateSchedule(Integer scheduleId) {
        return trainSchedulesRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Train schedule not found with ID: " + scheduleId));
    }

    private Booking validateBooking(Integer bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
    }

    private void validateSeatAvailability(TrainSchedules schedule, Integer requestedSeats) {
        int trainCapacity = schedule.getTrain().getCapacity();
        int totalBookedSeats = bookingRepository.findBySchedule(schedule)
                .stream()
                .mapToInt(Booking::getNumberOfSeats)
                .sum();
        int availableSeats = trainCapacity - totalBookedSeats;

        if (requestedSeats > availableSeats) {
            throw new RuntimeException("Not enough available seats. Only " + availableSeats + " seats left.");
        }
    }
}
