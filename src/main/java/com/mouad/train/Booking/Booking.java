package com.mouad.train.Booking;

import com.mouad.train.trains.Train;
import com.mouad.train.users.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.mouad.train.Enums.Enums;
import com.mouad.train.Schedules.TrainSchedules; // Assuming this is the TrainSchedules entity

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne // This establishes a foreign key relationship with the User entity
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Reference to User entity (many bookings can belong to one user)

    @ManyToOne // This establishes a foreign key relationship with the Train entity
    @JoinColumn(name = "train_id", nullable = false)
    private Train train; // Reference to Train entity (many bookings can belong to one train)

    @ManyToOne // This establishes a foreign key relationship with the TrainSchedules entity
    @JoinColumn(name = "schedule_id", nullable = false)
    private TrainSchedules schedule; // Reference to TrainSchedules entity (many bookings can belong to one schedule)

    private Integer numberOfSeats;

    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING) // Store as String in the database
    @Column(nullable = false)
    private Enums.BookingStatus status; // Reference the enum from Enums.java

    // Default constructor
    public Booking() {}

    // Constructor with all fields
    public Booking(User user, Train train, TrainSchedules schedule, Integer numberOfSeats, LocalDateTime bookingTime, Enums.BookingStatus status) {
        this.user = user;
        this.train = train;
        this.schedule = schedule;
        this.numberOfSeats = numberOfSeats;
        this.bookingTime = bookingTime;
        this.status = status;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public TrainSchedules getSchedule() {
        return schedule;
    }

    public void setSchedule(TrainSchedules schedule) {
        this.schedule = schedule;
    }

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public Enums.BookingStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.BookingStatus status) {
        this.status = status;
    }
}
