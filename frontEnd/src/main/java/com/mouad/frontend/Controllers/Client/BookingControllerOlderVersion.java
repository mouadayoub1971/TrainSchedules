package com.mouad.frontend.Controllers.Client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mouad.frontend.Controllers.TrainSchedule;
import com.mouad.frontend.Controllers.backend.BackendService;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BookingControllerOlderVersion {
    public Button addBookingAdmin;
    public TextField NumberOfSeatsAdminText;
    public TextField DestinationAdminText;
    public TextField ArrivalAdminText;
    public DatePicker Date;

    public void AddedBooking(ActionEvent actionEvent) {
    }


    public void GoToAdminMenu(ActionEvent actionEvent) {
    }

    public void initialize(){
        addBookingAdmin.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        System.out.println("begin");
        try {
            // Get user input values
            String numberOfSeats = NumberOfSeatsAdminText.getText();
            String destination = DestinationAdminText.getText();
            String arrival = ArrivalAdminText.getText();
            LocalDate date = Date.getValue();

            // Log input for debugging
            System.out.println("Inputs: " +
                    "\nNumber of Seats: " + numberOfSeats +
                    "\nDestination: " + destination +
                    "\nArrival: " + arrival +
                    "\nDate: " + date);

            // Fetch schedules from the backend
            String jsonResponse = BackendService.fetchData("/schedules");
            System.out.println("The response from the backend: " + jsonResponse);

            // Deserialize JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // Register the module for Java 8 date/time types
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional: Use ISO-8601 format
            List<TrainSchedule> schedules = objectMapper.readValue(jsonResponse, new TypeReference<List<TrainSchedule>>() {});

            // Filter schedules based on user input
            List<TrainSchedule> filteredSchedules = schedules.stream()
                    .filter(schedule -> schedule.getDestination().equalsIgnoreCase(destination))
                    .filter(schedule -> schedule.getDeparture().equalsIgnoreCase(arrival))
                    .filter(schedule -> schedule.getDepartureTime().toLocalDate().equals(date))
                    .toList();

            // Check if any schedules match
            if (filteredSchedules.isEmpty()) {
                // No matching schedules
                showAlert(Alert.AlertType.INFORMATION, "No Schedules Found", "No schedules match your criteria.");
            } else {
                // Build response message for matching schedules
                StringBuilder responseMessage = new StringBuilder("Matching Schedules:\n");
                for (TrainSchedule schedule : filteredSchedules) {
                    responseMessage.append("Train Name: ").append(schedule.getTrain().getTrainName())
                            .append(", Departure: ").append(schedule.getDeparture())
                            .append(", Destination: ").append(schedule.getDestination())
                            .append(", Departure Time: ").append(schedule.getDepartureTime())
                            .append(", Arrival Time: ").append(schedule.getArrivalTime())
                            .append(", Cost: ").append(schedule.getCost())
                            .append("\n");
                }
                // Show a success alert with matching schedules
                showAlert(Alert.AlertType.INFORMATION, "Schedules Found", responseMessage.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }
    public static class bookingPayload{
        public int id = 1;
        public int scheduleId ;
        public int numberOfSetts;
        public LocalDateTime bookingTime ;

    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Example request payload class
    static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
