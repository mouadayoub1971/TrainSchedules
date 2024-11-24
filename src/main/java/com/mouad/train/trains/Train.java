package com.mouad.train.trains;

import jakarta.persistence.*;

@Entity
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String trainName;

    private String trainType;

    private Integer capacity;

    @Column(nullable = false)
    private String status; // e.g., "active", "cancelled", "completed"

    public Train() {}

    // Constructor with all parameters (excluding ID)
    public Train(String trainName, String trainType, Integer capacity, String status) {
        this.trainName = trainName;
        this.trainType = trainType;
        this.capacity = capacity;
        this.status = status;
    }

    public Train(Integer id, String trainName, String trainType, Integer capacity, String status) {
        this.id = id;
        this.trainName = trainName;
        this.trainType = trainType;
        this.capacity = capacity;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
