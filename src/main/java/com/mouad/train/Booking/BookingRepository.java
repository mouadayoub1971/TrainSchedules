package com.mouad.train.Booking;

import com.mouad.train.Enums.Enums;
import com.mouad.train.Schedules.TrainSchedules;
import com.mouad.train.trains.Train;
import com.mouad.train.Schedules.TrainSchedulesRepository;
import com.mouad.train.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Find bookings by user
    List<Booking> findByUser(User user);

    // Find bookings by schedule
    List<Booking> findBySchedule(TrainSchedules schedule);

    // Find bookings by status (using enum instead of String)
    List<Booking> findByStatus(Enums.BookingStatus status);

    // Find active bookings by user
    List<Booking> findByUserAndStatus(User user, Enums.BookingStatus status);

    // Find bookings by train indirectly through TrainSchedules
    default List<Booking> findByTrain(Train train, TrainSchedulesRepository trainSchedulesRepository) {
        // Fetch all schedules for the given train
        List<TrainSchedules> schedules = trainSchedulesRepository.findByTrain(train);

        // Fetch bookings for each schedule and flatten the results
        return schedules.stream()
                .flatMap(schedule -> findBySchedule(schedule).stream())
                .toList();
    }

    // Find bookings by booking time between
    List<Booking> findByBookingTimeBetween(LocalDateTime start, LocalDateTime end);

    // Count all bookings
    long count();

    // Count bookings by status
    long countByStatus(Enums.BookingStatus status);
}
