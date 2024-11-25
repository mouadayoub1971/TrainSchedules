package com.mouad.train.Booking;

import com.mouad.train.Enums.Enums;
import com.mouad.train.Schedules.TrainSchedules;
import com.mouad.train.users.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private TrainSchedules schedule;

    private Integer numberOfSeats;

    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.BookingStatus status;

    public Booking() {}

    public Booking(User user, TrainSchedules schedule, Integer numberOfSeats, LocalDateTime bookingTime, Enums.BookingStatus status) {
        this.user = user;
        this.schedule = schedule;
        this.numberOfSeats = numberOfSeats;
        this.bookingTime = bookingTime;
        this.status = status;
    }

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
