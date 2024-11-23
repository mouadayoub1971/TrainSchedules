package com.mouad.train.Schedules;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TrainSchedulesRepository extends JpaRepository<TrainSchedules, Integer> {

    // Find schedules by train ID
    List<TrainSchedules> findByTrainId(Integer trainId);

    // Find schedules by departure location
    List<TrainSchedules> findByDeparture(String departure);

    // Find schedules by destination location
    List<TrainSchedules> findByDestination(String destination);

    // Find schedules by departure and destination
    List<TrainSchedules> findByDepartureAndDestination(String departure, String destination);

    // Find schedules within a specific departure time range
    List<TrainSchedules> findByDepartureTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // Find schedules by cost (if needed)
    List<TrainSchedules> findByCost(Double cost);
}
