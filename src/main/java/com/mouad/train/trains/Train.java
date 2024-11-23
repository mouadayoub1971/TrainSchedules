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
    private Boolean active;

    public Train() {}

    // Constructor with all parameters (excluding ID)
    public Train(String trainName, String trainType, Integer capacity, Boolean active) {
        this.trainName = trainName;
        this.trainType = trainType;
        this.capacity = capacity;
        this.active = active;
    }

    public Train(Integer id, String trainName, String trainType, Integer capacity, Boolean active) {
        this.id = id;
        this.trainName = trainName;
        this.trainType = trainType;
        this.capacity = capacity;
        this.active = active;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
