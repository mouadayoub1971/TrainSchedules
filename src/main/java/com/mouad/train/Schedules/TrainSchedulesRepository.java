package com.mouad.train.Schedules;

import com.mouad.train.trains.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TrainSchedulesRepository extends JpaRepository<TrainSchedules, Integer> {

    // Find schedules by train entity
    List<TrainSchedules> findByTrain(Train train);

    // Find schedules by departure location
    List<TrainSchedules> findByDeparture(String departure);

    // Find schedules by destination location
    List<TrainSchedules> findByDestination(String destination);

    // Find schedules by departure and destination
    List<TrainSchedules> findByDepartureAndDestination(String departure, String destination);

    // Find schedules within a specific departure time range
    List<TrainSchedules> findByDepartureTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // Find schedules by cost
    List<TrainSchedules> findByCost(Double cost);

    // Find schedules that conflict with a specific time range for a train
    @Query("SELECT s FROM TrainSchedules s " +
            "WHERE s.train = :train " +
            "AND (s.departureTime < :endTime AND s.arrivalTime > :startTime)")
    List<TrainSchedules> findConflictingSchedules(
            @Param("train") Train train,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Find schedules marked as available
    List<TrainSchedules> findByAvailableTrue();
}
