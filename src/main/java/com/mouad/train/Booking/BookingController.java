package com.mouad.train.Booking;

import com.mouad.train.Enums.Enums;
import com.mouad.train.Schedules.TrainSchedules;
import com.mouad.train.Schedules.TrainSchedulesRepository;
import com.mouad.train.trains.Train;
import com.mouad.train.trains.TrainRepository;
import com.mouad.train.users.User;
import com.mouad.train.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

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
    private TrainRepository trainRepository;

    @Autowired
    private TrainSchedulesRepository trainScheduleRepository;

    // Get all bookings
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    // Get booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        return ResponseEntity.ok(booking);
    }

    // Create a new booking
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking bookingRequest) {
        // Validate user
        User user = userRepository.findById(bookingRequest.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate train
        Train train = trainRepository.findById(bookingRequest.getTrain().getId())
                .orElseThrow(() -> new RuntimeException("Train not found"));

        // Validate schedule
        TrainSchedules schedule = trainScheduleRepository.findById(bookingRequest.getSchedule().getId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // Check seat availability
        int bookedSeats = bookingRepository.findByTrain(train).stream()
                .mapToInt(Booking::getNumberOfSeats)
                .sum();

        int availableSeats = train.getCapacity() - bookedSeats;
        if (bookingRequest.getNumberOfSeats() > availableSeats) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // Not enough seats available
        }

        // Set booking time and default status
        bookingRequest.setBookingTime(LocalDateTime.now());
        bookingRequest.setStatus(Enums.BookingStatus.CONFIRMED);

        // Save the new booking
        Booking savedBooking = bookingRepository.save(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);
    }

    // Update booking status
    @PutMapping("/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Integer id, @RequestParam String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        try {
            // Convert status String to enum
            Enums.BookingStatus bookingStatus = Enums.BookingStatus.valueOf(status.toUpperCase());
            booking.setStatus(bookingStatus);
        } catch (IllegalArgumentException e) {
            // Handle invalid status
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return ResponseEntity.ok(updatedBooking);
    }

    // Delete booking
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Integer id) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Check availability for a specific train schedule
    @GetMapping("/availability/{scheduleId}")
    public ResponseEntity<String> checkAvailability(@PathVariable Integer scheduleId) {
        TrainSchedules schedule = trainScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + scheduleId));

        Train train = trainRepository.findById(schedule.getTrain().getId())
                .orElseThrow(() -> new RuntimeException("Train not found"));

        int bookedSeats = bookingRepository.findBySchedule(schedule).stream()
                .mapToInt(Booking::getNumberOfSeats).sum();

        int availableSeats = train.getCapacity() - bookedSeats;

        if (availableSeats > 0) {
            return ResponseEntity.ok("Seats available: " + availableSeats);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body("No seats available for schedule ID: " + scheduleId);
        }
    }

    // Get bookings by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUser(user);
        return ResponseEntity.ok(bookings);
    }

    // Get bookings by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable String status) {
        Enums.BookingStatus bookingStatus;
        try {
            bookingStatus = Enums.BookingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<Booking> bookings = bookingRepository.findByStatus(bookingStatus);
        return ResponseEntity.ok(bookings);
    }
}
