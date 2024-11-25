package com.mouad.train.Booking;

import com.mouad.train.Enums.Enums;
import com.mouad.train.Schedules.TrainSchedules;
import com.mouad.train.trains.Train;
import com.mouad.train.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Find bookings by user
    List<Booking> findByUser(User user);

    // Find bookings by train
    List<Booking> findByTrain(Train train);

    // Find bookings by schedule
    List<Booking> findBySchedule(TrainSchedules schedule);

    // Find bookings by status (using enum instead of String)
    List<Booking> findByStatus(Enums.BookingStatus status);
}
