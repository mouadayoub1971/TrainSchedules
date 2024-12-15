package com.mouad.frontend.Controllers.Client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mouad.frontend.Controllers.TrainSchedule;
import com.mouad.frontend.Controllers.backend.BackendService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingController {
    @FXML
    public Button searchSchedulesButton;
    @FXML
    public TextField NumberOfSeatsAdminText;
    @FXML
    public TextField DestinationAdminText;
    @FXML
    public TextField ArrivalAdminText;
    @FXML
    public DatePicker Date;

    @FXML
    public ImageView logoImageView;

    @FXML
    public TableView<TrainSchedule> schedulesTableView;
    @FXML
    public TableColumn<TrainSchedule, String> trainNameColumn;
    @FXML
    public TableColumn<TrainSchedule, String> departureColumn;
    @FXML
    public TableColumn<TrainSchedule, String> destinationColumn;
    @FXML
    public TableColumn<TrainSchedule, String> departureTimeColumn;
    @FXML
    public TableColumn<TrainSchedule, String> arrivalTimeColumn;
    @FXML
    public TableColumn<TrainSchedule, Double> costColumn;
    @FXML
    public TableColumn<TrainSchedule, Void> actionColumn;

    private ObservableList<TrainSchedule> schedulesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initially hide the table
        schedulesTableView.setVisible(false);

        // Set up columns
        trainNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTrain().getTrainName()));
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("departure"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
        departureTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDepartureTime().toString()));
        arrivalTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getArrivalTime().toString()));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));

        // Setup action column with booking buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button bookButton = new Button("Book");

            {
                bookButton.setOnAction(event -> {
                    TrainSchedule schedule = getTableView().getItems().get(getIndex());
                    bookSchedule(schedule);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(bookButton);
                }
            }
        });

        // Set the table's items
        schedulesTableView.setItems(schedulesList);
    }

    @FXML
    public void toggleLogoVisibility(MouseEvent event) {
        logoImageView.setVisible(false);
    }

    @FXML
    public void searchSchedules(ActionEvent actionEvent) {
        try {
            // Get user input values
            String numberOfSeats = NumberOfSeatsAdminText.getText();
            String destination = DestinationAdminText.getText();
            String arrival = ArrivalAdminText.getText();
            LocalDate date = Date.getValue();

            // Validate inputs
            if (numberOfSeats.isEmpty() || destination.isEmpty() || arrival.isEmpty() || date == null) {
                showAlert(Alert.AlertType.WARNING, "Incomplete Information", "Please fill in all fields.");
                return;
            }

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
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            List<TrainSchedule> schedules = objectMapper.readValue(jsonResponse, new TypeReference<List<TrainSchedule>>() {});

            // Filter schedules based on user input
            List<TrainSchedule> filteredSchedules = schedules.stream()
                    .filter(schedule -> schedule.getDestination().equalsIgnoreCase(destination))
                    .filter(schedule -> schedule.getDeparture().equalsIgnoreCase(arrival))
                    .filter(schedule -> schedule.getDepartureTime().toLocalDate().equals(date))
                    .toList();

            // Clear previous results and add new filtered schedules
            schedulesList.clear();
            schedulesList.addAll(filteredSchedules);

            // Check if any schedules match
            if (filteredSchedules.isEmpty()) {
                // No matching schedules
                schedulesTableView.setVisible(false);
                showAlert(Alert.AlertType.INFORMATION, "No Schedules Found", "No schedules match your criteria.");
            } else {
                // Make the table visible
                schedulesTableView.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            schedulesTableView.setVisible(false);
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void bookSchedule(TrainSchedule schedule) {
        try {
            // Validate number of seats
            int numberOfSeats = Integer.parseInt(NumberOfSeatsAdminText.getText());

            if (numberOfSeats <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Seats", "Number of seats must be greater than 0.");
                return;
            }

            // Prepare booking payload
            Map<String, Object> bookingPayload = new HashMap<>();

            // User object
            // Jarmouni
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", 1); // Hardcoded user ID
            bookingPayload.put("user", userMap);

            // Schedule object

            Map<String, Object> scheduleMap = new HashMap<>();
            scheduleMap.put("id", schedule.getId());
            bookingPayload.put("schedule", scheduleMap);

            // Additional booking details
            bookingPayload.put("numberOfSeats", numberOfSeats);
            bookingPayload.put("bookingTime", LocalDateTime.now().toString());
            bookingPayload.put("status", "CONFIRMED");

            // Send booking to backend
            try {
                String response = BackendService.postData("/bookings", bookingPayload);

                // Process successful booking
                showAlert(Alert.AlertType.INFORMATION, "Booking Successful",
                        "You've booked " + numberOfSeats + " seats on " +
                                schedule.getTrain().getTrainName() +
                                " from " + schedule.getDeparture() +
                                " to " + schedule.getDestination());
            } catch (Exception e) {
                // Handle booking error
                showAlert(Alert.AlertType.ERROR, "Booking Failed",
                        "Unable to complete booking: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number of seats.");
        }
    }

    @FXML
    public void GoToAdminMenu(ActionEvent actionEvent) {
        // Implementation for going back to admin menu
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}