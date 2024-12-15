package com.mouad.frontend.Models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class RouteMetrics {
    private final SimpleStringProperty route;
    private final SimpleIntegerProperty count;

    public RouteMetrics(String route, int count) {
        this.route = new SimpleStringProperty(route);
        this.count = new SimpleIntegerProperty(count);
    }

    public String getRoute() { return route.get(); }
    public SimpleStringProperty routeProperty() { return route; }

    public int getCount() { return count.get(); }
    public SimpleIntegerProperty countProperty() { return count; }
}
