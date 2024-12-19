package com.mouad.frontend.Controllers.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mouad.frontend.Components.CustomAlert;
import com.mouad.frontend.Services.BookingService;
import com.mouad.frontend.Services.UserService;
import com.mouad.frontend.Models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Map;

public class BookingDialogController {
    @FXML private Label fromLabel;
    @FXML private Label toLabel;
    @FXML private Label departureTimeLabel;
    @FXML private Label arrivalTimeLabel;
    @FXML private Label costLabel;
    @FXML private Label totalCostLabel;
    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private Map<String, Object> schedule;
    private double costPerSeat;
    private final BookingService bookingService = BookingService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        seatsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateTotalCost();
        });
    }

    public void setScheduleData(Map<String, Object> schedule) {
        if (schedule == null) {
            CustomAlert.showError("Error", "Invalid schedule data");
            return;
        }
        
        this.schedule = schedule;
        
        // Debug print
        System.out.println("Setting schedule data in dialog: " + schedule);
        
        // Safely get values with defaults
        String from = getStringValue(schedule, "from", "N/A");
        String to = getStringValue(schedule, "to", "N/A");
        String departureTime = getStringValue(schedule, "departureTime", "N/A");
        String arrivalTime = getStringValue(schedule, "arrivalTime", "N/A");
        
        fromLabel.setText(from);
        toLabel.setText(to);
        departureTimeLabel.setText(departureTime);
        arrivalTimeLabel.setText(arrivalTime);
        
        // Get cost with default
        costPerSeat = getDoubleValue(schedule, "cost", 0.0);
        costLabel.setText(String.format("%.2f DH", costPerSeat));
        
        // Set up spinner with available seats
        int availableSeats = getIntValue(schedule, "availableSeats", 10);
        seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, availableSeats, 1));
        
        updateTotalCost();
        
        // Debug print
        System.out.println("Dialog data set successfully");
    }

    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void updateTotalCost() {
        int seats = seatsSpinner.getValue();
        double total = seats * costPerSeat;
        totalCostLabel.setText(String.format("%.2f DH", total));
    }

    @FXML
    private void onConfirmClicked() {
        try {
            // Get the current user ID
            Long userId = userService.getCurrentUserId();
            if (userId == null) {
                CustomAlert.showError("Error", "You must be logged in to make a booking");
                return;
            }

            if (schedule == null || !schedule.containsKey("scheduleId")) {
                CustomAlert.showError("Error", "Invalid schedule data");
                return;
            }

            // Create booking request
            ObjectNode bookingData = objectMapper.createObjectNode();
            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("id", userId);
            bookingData.set("user", userNode);

            ObjectNode scheduleNode = objectMapper.createObjectNode();
            scheduleNode.put("id", Integer.parseInt(schedule.get("scheduleId").toString()));
            bookingData.set("schedule", scheduleNode);

            bookingData.put("numberOfSeats", seatsSpinner.getValue());

            // Debug print
            System.out.println("Sending booking data: " + bookingData.toString());

            // Make the booking
            try {
                JsonNode response = bookingService.createBooking(bookingData);
                CustomAlert.showSuccess("Success", "Booking confirmed successfully!");
                closeDialog();
            } catch (RuntimeException e) {
                String message = e.getMessage();
                if (message.contains("Not enough available seats")) {
                    // Extract the number of available seats from the error message
                    String availableSeats = message.replaceAll(".*Only (\\d+) seats left.*", "$1");
                    CustomAlert.showError("Not Enough Seats", 
                        "There are only " + availableSeats + " seats available on this train. " +
                        "Please select a smaller number of seats.");
                } else {
                    CustomAlert.showError("Booking Failed", "Failed to create booking: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showError("Error", "Failed to create booking: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelClicked() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
