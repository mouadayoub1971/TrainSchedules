package com.mouad.frontend.Models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class UserBooking {
    private final SimpleStringProperty userEmail;
    private final SimpleIntegerProperty bookingCount;

    public UserBooking(String userEmail, int bookingCount) {
        this.userEmail = new SimpleStringProperty(userEmail);
        this.bookingCount = new SimpleIntegerProperty(bookingCount);
    }

    public String getUserEmail() { return userEmail.get(); }
    public SimpleStringProperty userEmailProperty() { return userEmail; }

    public int getBookingCount() { return bookingCount.get(); }
    public SimpleIntegerProperty bookingCountProperty() { return bookingCount; }
}