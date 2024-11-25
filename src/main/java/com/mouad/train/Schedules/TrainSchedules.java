package com.mouad.train.Schedules;

import com.mouad.train.trains.Train;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TrainSchedules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne // This establishes a foreign key relationship with the Train entity
    @JoinColumn(name = "train_id", nullable = false)
    private Train train; // Reference to Train entity (many bookings can belong to one train)

    private String departure;
    private String destination;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    @Column(nullable = false)
    private Double cost;

    // New field
    @Column(nullable = false)
    private boolean available;

    // Default constructor
    public TrainSchedules() {}

    // Constructor with parameters
    public TrainSchedules(Train train, String departure, String destination,
                          LocalDateTime departureTime, LocalDateTime arrivalTime, Double cost, boolean available) {
        this.train = train;
        this.departure = departure;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.cost = cost;
        this.available = available;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
