package com.mouad.train.Booking;

import com.mouad.train.Enums.Enums;
import com.mouad.train.Log.LogsGene;
import com.mouad.train.Producer.KafkaProducer;
import com.mouad.train.Schedules.TrainSchedules;
import com.mouad.train.Schedules.TrainSchedulesRepository;
import com.mouad.train.trains.Train;
import com.mouad.train.trains.TrainRepository;
import com.mouad.train.users.User;
import com.mouad.train.users.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    public final KafkaProducer kafkaProducer;
    public final LogsGene logsGene;
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

    // Get bookings by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUserId(@PathVariable Integer userId) {
        try {
            User user = validateUser(userId);
            List<Booking> bookings = bookingRepository.findByUser(user);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Get active bookings by user ID
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<?> getActiveBookingsByUserId(@PathVariable Integer userId) {
        try {
            User user = validateUser(userId);
            List<Booking> activeBookings = bookingRepository.findByUserAndStatus(user, Enums.BookingStatus.CONFIRMED);
            return ResponseEntity.ok(activeBookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Create booking
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking bookingRequest) {
        String Messsage;
        try {
            // Validate User and Schedule
            User user = validateUser(bookingRequest.getUser().getId());
            TrainSchedules schedule = validateSchedule(bookingRequest.getSchedule().getId());

            // Validate seat availability
            validateSeatAvailability(schedule, bookingRequest.getNumberOfSeats());

            // Create booking
            bookingRequest.setUser(user);
            bookingRequest.setSchedule(schedule);
            bookingRequest.setBookingTime(LocalDateTime.now());
            bookingRequest.setStatus(Enums.BookingStatus.CONFIRMED);

            Booking savedBooking = bookingRepository.save(bookingRequest);


            // Log schedule path and booked seats
            String scheduleLog = String.format(
                    "Schedule Details: Path: %s -> %s, Train: %s, Timing: %s -> %s, Seats Booked: %d",
                    schedule.getDeparture(),
                    schedule.getDestination(),
                    schedule.getTrain().getTrainName(),
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime(),
                    bookingRequest.getNumberOfSeats()
            );
            kafkaProducer.sendMessage(logsGene.generateLog("INFO", "BookingController", scheduleLog, "Schedule Info"));

            // Log user details
            String userLog = String.format(
                    "User Details: Email: %s, Booking ID: %d",
                    user.getEmail(),
                    savedBooking.getId()
            );
            kafkaProducer.sendMessage(logsGene.generateLog("INFO", "BookingController", userLog, "User Info"));

            Messsage = logsGene.generateLog("INFO", "BookingController", "user email" + user.getEmail(), "details: "+ schedule.getDeparture() + " -> " + schedule.getDestination() + "  ;" );
            kafkaProducer.sendMessage(Messsage);
            Messsage = logsGene.generateLog("INFO", "BookingController", "trip:" + schedule.getDeparture() + "->" + schedule.getDestination(), "details: " + schedule.getTrain().getTrainName());
            kafkaProducer.sendMessage(Messsage);
            Messsage = logsGene.generateLog("INFO", "BookingController", String.format(
                    "timing:%s -> %s ",
                    schedule.getDepartureTime().toString(),
                    schedule.getArrivalTime().toString()
            ), "details: " + schedule.getTrain().getTrainName());
            kafkaProducer.sendMessage(Messsage);
            Messsage = logsGene.generateLog("INFO", "BookingController", "depart:" + schedule.getDeparture() , "details: " + schedule.getTrain().getTrainName());
            kafkaProducer.sendMessage(Messsage);
            Messsage = logsGene.generateLog("INFO", "BookingController", "arrive:" + schedule.getDestination() , "details: " + schedule.getTrain().getTrainName());
            kafkaProducer.sendMessage(Messsage);

            // Return the created booking
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Update a booking
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Integer id, @RequestBody Booking bookingRequest) {
        try {
            Booking existingBooking = validateBooking(id);
            User user = validateUser(bookingRequest.getUser().getId());
            TrainSchedules schedule = validateSchedule(bookingRequest.getSchedule().getId());

            // Validate seat availability for update
            validateSeatAvailabilityForUpdate(schedule, bookingRequest.getNumberOfSeats(), existingBooking.getNumberOfSeats());

            existingBooking.setUser(user);
            existingBooking.setSchedule(schedule);
            existingBooking.setNumberOfSeats(bookingRequest.getNumberOfSeats());
            existingBooking.setBookingTime(LocalDateTime.now());
            existingBooking.setStatus(Enums.BookingStatus.CONFIRMED);

            Booking updatedBooking = bookingRepository.save(existingBooking);

            // Log schedule path and booked seats
            String scheduleLog = String.format(
                    "Updated Schedule: Path: %s -> %s, Train: %s, Timing: %s -> %s, Seats Updated: %d",
                    schedule.getDeparture(),
                    schedule.getDestination(),
                    schedule.getTrain().getTrainName(),
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime(),
                    bookingRequest.getNumberOfSeats()
            );
            kafkaProducer.sendMessage(logsGene.generateLog("INFO", "BookingControllerUpdated", scheduleLog, "Schedule Info"));

            // Log user details
            String userLog = String.format(
                    "Updated User: Email: %s, Booking ID: %d",
                    user.getEmail(),
                    updatedBooking.getId()
            );
            kafkaProducer.sendMessage(logsGene.generateLog("INFO", "BookingControllerUpdated", userLog, "User Info"));

            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Get all train schedules with available seats
    @GetMapping("/schedules")
    public ResponseEntity<?> getSchedulesWithAvailableSeats() {
        try {
            // Fetch all train schedules
            List<TrainSchedules> schedules = trainSchedulesRepository.findAll();

            // Map each schedule to its available seats
            List<ScheduleDetails> scheduleDetails = schedules.stream()
                    .map(schedule -> {
                        int trainCapacity = schedule.getTrain().getCapacity(); // Train capacity
                        int bookedSeats = bookingRepository.findBySchedule(schedule) // Total booked seats for this schedule
                                .stream()
                                .mapToInt(Booking::getNumberOfSeats)
                                .sum();
                        int seatsLeft = trainCapacity - bookedSeats;

                        return new ScheduleDetails(schedule, seatsLeft);
                    })
                    .toList();

            return ResponseEntity.ok(scheduleDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Inner class to represent schedule details
    @Getter
    public static class ScheduleDetails {
        private final TrainSchedules schedule;
        private final int seatsLeft;

        public ScheduleDetails(TrainSchedules schedule, int seatsLeft) {
            this.schedule = schedule;
            this.seatsLeft = seatsLeft;
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
        String Messsage;
        try {
            Booking booking = validateBooking(id);
            Enums.BookingStatus bookingStatus = Enums.BookingStatus.valueOf(status.toUpperCase());

            booking.setStatus(bookingStatus);
            Booking updatedBooking = bookingRepository.save(booking);
            Messsage = logsGene.generateLog("INFO", "BookingControllerUpdatedStatus", booking.getStatus() + "->" + updatedBooking.getStatus()  , "details: ");
            kafkaProducer.sendMessage(Messsage);
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
        String Messsage;
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found with ID: " + id);
        }
        bookingRepository.deleteById(id);
        Messsage = logsGene.generateLog("INFO", "BookingControllerDelete", "Delete a booking"  , "details: ");
        kafkaProducer.sendMessage(Messsage);
        return ResponseEntity.noContent().build();
    }

    // Get total number of bookings
    @GetMapping("/stats/total")
    public ResponseEntity<?> getTotalBookingsCount() {
        try {
            long totalBookings = bookingRepository.count();
            return ResponseEntity.ok(new BookingStats("Total bookings", totalBookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting total bookings count: " + e.getMessage());
        }
    }

    // Get number of active bookings
    @GetMapping("/stats/active")
    public ResponseEntity<?> getActiveBookingsCount() {
        try {
            long activeBookings = bookingRepository.countByStatus(Enums.BookingStatus.CONFIRMED);
            return ResponseEntity.ok(new BookingStats("Active bookings", activeBookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting active bookings count: " + e.getMessage());
        }
    }

    // Get today's bookings
    @GetMapping("/stats/today")
    public ResponseEntity<?> getTodayBookings() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
            
            List<Booking> todayBookings = bookingRepository.findByBookingTimeBetween(startOfDay, endOfDay);
            return ResponseEntity.ok(new BookingStats("Today's bookings", todayBookings.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting today's bookings: " + e.getMessage());
        }
    }

    // Helper class for booking statistics
    private static class BookingStats {
        private String label;
        private long count;

        public BookingStats(String label, long count) {
            this.label = label;
            this.count = count;
        }

        public String getLabel() {
            return label;
        }

        public long getCount() {
            return count;
        }
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
