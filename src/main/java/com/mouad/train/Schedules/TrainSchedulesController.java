package com.mouad.train.Schedules;

import com.mouad.train.trains.Train;
import com.mouad.train.trains.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/schedules")
public class TrainSchedulesController {

    @Autowired
    private TrainSchedulesRepository trainSchedulesRepository;

    @Autowired
    private TrainRepository trainRepository;

    // Get all schedules
    @GetMapping
    public List<TrainSchedules> getAllSchedules() {
        return trainSchedulesRepository.findAll();
    }

    // Get schedules by train (using Train object)
    @GetMapping("/train/{trainId}")
    public List<TrainSchedules> getSchedulesByTrainId(@PathVariable Integer trainId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found with ID: " + trainId));
        return trainSchedulesRepository.findByTrain(train);
    }

    // Get schedules by departure location
    @GetMapping("/departure/{departure}")
    public List<TrainSchedules> getSchedulesByDeparture(@PathVariable String departure) {
        return trainSchedulesRepository.findByDeparture(departure);
    }

    // Get schedules by destination location
    @GetMapping("/destination/{destination}")
    public List<TrainSchedules> getSchedulesByDestination(@PathVariable String destination) {
        return trainSchedulesRepository.findByDestination(destination);
    }

    // Get schedules by departure and destination
    @GetMapping("/departure/{departure}/destination/{destination}")
    public List<TrainSchedules> getSchedulesByDepartureAndDestination(@PathVariable String departure, @PathVariable String destination) {
        return trainSchedulesRepository.findByDepartureAndDestination(departure, destination);
    }

    // Get schedules within a specific departure date range (start and end date)
    @GetMapping("/departureTime")
    public List<TrainSchedules> getSchedulesByDepartureDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        // Convert the start and end date strings into LocalDate objects
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        // Convert LocalDate to LocalDateTime (start of the day for start date, end of the day for end date)
        LocalDateTime startDateTime = LocalDateTime.of(start, LocalTime.MIN); // Start of the day
        LocalDateTime endDateTime = LocalDateTime.of(end, LocalTime.MAX); // End of the day

        // Fetch the schedules within the specified date range
        return trainSchedulesRepository.findByDepartureTimeBetween(startDateTime, endDateTime);
    }

    // Get schedules by cost
    @GetMapping("/cost/{cost}")
    public List<TrainSchedules> getSchedulesByCost(@PathVariable Double cost) {
        return trainSchedulesRepository.findByCost(cost);
    }

    // Add a new schedule
    @PostMapping
    public TrainSchedules addSchedule(
            @RequestBody TrainSchedules trainSchedule) {
        // Check for conflicting schedules
        List<TrainSchedules> conflictingSchedules = trainSchedulesRepository.findConflictingSchedules(
                trainSchedule.getTrain(),
                trainSchedule.getDepartureTime(),
                trainSchedule.getArrivalTime()
        );

        if (!conflictingSchedules.isEmpty()) {
            throw new RuntimeException("Conflict detected with existing schedules for this train.");
        }

        // Save the schedule if no conflicts are found
        return trainSchedulesRepository.save(trainSchedule);
    }
}
