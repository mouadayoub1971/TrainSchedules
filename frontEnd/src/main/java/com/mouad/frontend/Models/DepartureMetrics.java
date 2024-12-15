package com.mouad.frontend.Models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class DepartureMetrics {
    private final SimpleStringProperty departure;
    private final SimpleIntegerProperty count;

    public DepartureMetrics(String departure, int count) {
        this.departure = new SimpleStringProperty(departure);
        this.count = new SimpleIntegerProperty(count);
    }

    public String getDeparture() { return departure.get(); }
    public SimpleStringProperty departureProperty() { return departure; }

    public int getCount() { return count.get(); }
    public SimpleIntegerProperty countProperty() { return count; }
}
