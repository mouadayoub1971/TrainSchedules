package com.mouad.frontend.Controllers.Admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mouad.frontend.Components.CustomAlert;
import com.mouad.frontend.Services.BookingService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class BookingEditDialogController {
    @FXML
    private TextField userField;
    @FXML
    private ComboBox<JsonNode> scheduleComboBox;
    @FXML
    private Spinner<Integer> seatsSpinner;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;
    private JsonNode currentBooking;

    public BookingEditDialogController() {
        this.bookingService = BookingService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @FXML
    public void initialize() {
        try {
            // Setup seats spinner
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
            seatsSpinner.setValueFactory(valueFactory);

            // Setup status options to match backend enum
            statusComboBox.getItems().addAll("CONFIRMED", "CANCELED", "COMPLETED");

            // Load schedules
            JsonNode schedules = bookingService.getAvailableSchedules();
            if (schedules != null && schedules.isArray()) {
                for (JsonNode schedule : schedules) {
                    scheduleComboBox.getItems().add(schedule);
                }
            }

            // Setup display for schedules
            scheduleComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(JsonNode schedule) {
                    if (schedule == null) return null;
                    return String.format("%s -> %s (%s)", 
                        schedule.get("departure").asText(),
                        schedule.get("destination").asText(),
                        schedule.get("departureTime").asText()
                    );
                }

                @Override
                public JsonNode fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading schedules: " + e.getMessage());
            CustomAlert.showInformation("Error", "Could not load schedules: " + e.getMessage());
        }
    }

    public void setBooking(JsonNode booking) {
        try {
            System.out.println("Received booking data: " + booking.toString());
            
            this.currentBooking = booking;
            
            // Set user email (read-only)
            JsonNode user = booking.get("user");
            if (user == null) {
                throw new IllegalArgumentException("Booking has no user information");
            }
            
            JsonNode emailNode = user.get("email");
            JsonNode firstNameNode = user.get("firstName");
            JsonNode secondNameNode = user.get("secondName");
            
            if (emailNode == null) {
                throw new IllegalArgumentException("User has no email");
            }
            
            StringBuilder userDisplay = new StringBuilder(emailNode.asText());
            if (firstNameNode != null && secondNameNode != null) {
                userDisplay.append(" (").append(firstNameNode.asText())
                          .append(" ").append(secondNameNode.asText()).append(")");
            }
            userField.setText(userDisplay.toString());
            
            // Set current values
            JsonNode seatsNode = booking.get("numberOfSeats");
            if (seatsNode == null) {
                throw new IllegalArgumentException("Booking has no seats information");
            }
            seatsSpinner.getValueFactory().setValue(seatsNode.asInt());
            
            JsonNode statusNode = booking.get("status");
            if (statusNode == null) {
                throw new IllegalArgumentException("Booking has no status information");
            }
            statusComboBox.setValue(statusNode.asText());
            
            // Find and select the current schedule
            JsonNode currentSchedule = booking.get("schedule");
            if (currentSchedule == null) {
                throw new IllegalArgumentException("Booking has no schedule information");
            }
            
            Long scheduleId = currentSchedule.get("id").asLong();
            boolean found = false;
            for (JsonNode schedule : scheduleComboBox.getItems()) {
                if (schedule.get("id").asLong() == scheduleId) {
                    scheduleComboBox.setValue(schedule);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                throw new IllegalArgumentException("Could not find matching schedule with ID: " + scheduleId);
            }
            
        } catch (Exception e) {
            System.err.println("Error setting booking data: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showInformation("Error", "Could not load booking details: " + e.getMessage());
        }
    }

    @FXML
    public void onSave() {
        try {
            if (!validateInputs()) {
                return;
            }

            // Check if only status is being updated
            String newStatus = statusComboBox.getValue();
            String currentStatus = currentBooking.get("status").asText();
            boolean onlyStatusChanged = seatsSpinner.getValue() == currentBooking.get("numberOfSeats").asInt() &&
                    scheduleComboBox.getValue().get("id").asInt() == currentBooking.get("schedule").get("id").asInt() &&
                    !currentStatus.equals(newStatus);

            if (onlyStatusChanged) {
                // Just update the status
                bookingService.updateBookingStatus(currentBooking.get("id").asInt(), newStatus);
                CustomAlert.showSuccess("Success", "Booking status updated successfully");
            } else {
                // Create booking object for full update
                ObjectNode booking = objectMapper.createObjectNode();
                booking.put("id", currentBooking.get("id").asInt());
                booking.put("numberOfSeats", seatsSpinner.getValue());
                booking.put("status", newStatus);
                
                // Set booking time from current booking
                if (currentBooking.has("bookingTime")) {
                    booking.put("bookingTime", currentBooking.get("bookingTime").asText());
                }
                
                // Set user
                ObjectNode userNode = booking.putObject("user");
                userNode.put("id", currentBooking.get("user").get("id").asInt());
                
                // Set schedule with all required fields
                JsonNode selectedSchedule = scheduleComboBox.getValue();
                ObjectNode scheduleNode = booking.putObject("schedule");
                scheduleNode.put("id", selectedSchedule.get("id").asInt());
                scheduleNode.put("departure", selectedSchedule.get("departure").asText());
                scheduleNode.put("destination", selectedSchedule.get("destination").asText());
                scheduleNode.put("departureTime", selectedSchedule.get("departureTime").asText());
                scheduleNode.put("arrivalTime", selectedSchedule.get("arrivalTime").asText());
                scheduleNode.put("cost", selectedSchedule.get("cost").asDouble());
                
                // Add train information to schedule
                JsonNode trainNode = selectedSchedule.get("train");
                if (trainNode != null) {
                    ObjectNode scheduleTrainNode = scheduleNode.putObject("train");
                    scheduleTrainNode.put("id", trainNode.get("id").asInt());
                    scheduleTrainNode.put("trainName", trainNode.get("trainName").asText());
                }

                System.out.println("Sending update request with data: " + booking.toPrettyString());

                // Update booking
                bookingService.updateBooking(booking);
                CustomAlert.showSuccess("Success", "Booking updated successfully");
            }

            // Close the dialog
            closeDialog();
        } catch (Exception e) {
            System.err.println("Error updating booking: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showError("Error", "Could not update booking: " + e.getMessage());
        }
    }

    public boolean validateInputs() {
        if (scheduleComboBox.getValue() == null) {
            CustomAlert.showInformation("Validation Error", "Please select a schedule");
            return false;
        }

        if (seatsSpinner.getValue() == null || seatsSpinner.getValue() < 1) {
            CustomAlert.showInformation("Validation Error", "Please enter a valid number of seats");
            return false;
        }

        if (statusComboBox.getValue() == null) {
            CustomAlert.showInformation("Validation Error", "Please select a status");
            return false;
        }

        return true;
    }

    @FXML
    public void onCancel() {
        closeDialog();
    }

    public void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}