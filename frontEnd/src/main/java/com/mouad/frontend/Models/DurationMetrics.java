package com.mouad.frontend.Models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class DurationMetrics {
    private final SimpleStringProperty duration;
    private final SimpleIntegerProperty count;

    public DurationMetrics(String duration, int count) {
        this.duration = new SimpleStringProperty(duration);
        this.count = new SimpleIntegerProperty(count);
    }

    public String getDuration() { return duration.get(); }
    public SimpleStringProperty durationProperty() { return duration; }

    public int getCount() { return count.get(); }
    public SimpleIntegerProperty countProperty() { return count; }
}
