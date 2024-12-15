package com.mouad.frontend.Models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class DestinationMetrics {
    private final SimpleStringProperty destination;
    private final SimpleIntegerProperty count;

    public DestinationMetrics(String destination, int count) {
        this.destination = new SimpleStringProperty(destination);
        this.count = new SimpleIntegerProperty(count);
    }

    public String getDestination() { return destination.get(); }
    public SimpleStringProperty destinationProperty() { return destination; }

    public int getCount() { return count.get(); }
    public SimpleIntegerProperty countProperty() { return count; }
}
