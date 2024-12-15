package com.mouad.frontend.Controllers.Admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mouad.frontend.Services.AdminService;
import com.mouad.frontend.Views.CustomAlert;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class BookingDialogController {
    @FXML private Label dialogTitle;
    @FXML private ComboBox<JsonNode> userComboBox;
    @FXML private ComboBox<JsonNode> scheduleComboBox;
    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label trainNameLabel;
    @FXML private Label routeLabel;
    @FXML private Label timeLabel;
    @FXML private Label availableSeatsLabel;
    @FXML private Label costLabel;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final ObjectMapper objectMapper;
    private AdminService adminService;
    private JsonNode editingBooking;
    private Stage dialogStage;
    private boolean saveClicked = false;
    private boolean viewOnly;
    private Runnable onSave;

    public BookingDialogController() {
        this.objectMapper = new ObjectMapper();
    }

    @FXML
    private void initialize() {
        // Initialize status combo box
        statusComboBox.getItems().addAll("CONFIRMED", "CANCELLED", "PENDING");

        // Setup combo box display
        userComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(JsonNode user) {
                if (user == null) return "";
                JsonNode firstName = user.get("firstName");
                JsonNode lastName = user.get("lastName");
                JsonNode email = user.get("email");
                return (firstName != null ? firstName.asText() : "Unknown") + " " + 
                       (lastName != null ? lastName.asText() : "Unknown") + " (" + 
                       (email != null ? email.asText() : "N/A") + ")";
            }

            @Override
            public JsonNode fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        scheduleComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(JsonNode schedule) {
                if (schedule == null) return "";
                JsonNode departure = schedule.get("departure");
                JsonNode destination = schedule.get("destination");
                JsonNode departureTime = schedule.get("departureTime");
                return (departure != null ? departure.asText() : "Unknown") + " → " + 
                       (destination != null ? destination.asText() : "Unknown") + " (" +
                       (departureTime != null ? departureTime.asText() : "Unknown") + ")";
            }

            @Override
            public JsonNode fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        // Add listeners
        scheduleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateScheduleDetails(newVal));
        
        // Configure seats spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        seatsSpinner.setValueFactory(valueFactory);

        // Load data
        loadUsers();
        loadSchedules();
    }

    private void loadUsers() {
        try {
            JsonNode users = adminService.getAllUsers();
            userComboBox.getItems().clear();
            if (users != null && users.isArray()) {
                for (JsonNode user : users) {
                    if (user != null) {
                        userComboBox.getItems().add(user);
                    }
                }
            }
        } catch (Exception e) {
            showError("Error loading users: " + e.getMessage());
        }
    }

    private void loadSchedules() {
        try {
            JsonNode schedules = adminService.getSchedulesWithAvailableSeats();
            scheduleComboBox.getItems().clear();
            if (schedules != null && schedules.isArray()) {
                for (JsonNode schedule : schedules) {
                    if (schedule != null) {
                        scheduleComboBox.getItems().add(schedule);
                    }
                }
            }
        } catch (Exception e) {
            showError("Error loading schedules: " + e.getMessage());
        }
    }

    private void updateScheduleDetails(JsonNode schedule) {
        if (schedule != null) {
            JsonNode train = schedule.get("train");
            if (train != null) {
                JsonNode trainName = train.get("trainName");
                trainNameLabel.setText(trainName != null ? trainName.asText() : "Unknown");
            } else {
                trainNameLabel.setText("Unknown");
            }

            JsonNode departure = schedule.get("departure");
            JsonNode destination = schedule.get("destination");
            routeLabel.setText((departure != null ? departure.asText() : "Unknown") + " → " + 
                             (destination != null ? destination.asText() : "Unknown"));
            
            JsonNode departureTime = schedule.get("departureTime");
            JsonNode arrivalTime = schedule.get("arrivalTime");
            String timeText = (departureTime != null ? departureTime.asText() : "Unknown") + " → " + 
                            (arrivalTime != null ? arrivalTime.asText() : "Unknown");
            timeLabel.setText(timeText);
            
            int availableSeats = 0;
            if (train != null && train.get("capacity") != null) {
                try {
                    int totalCapacity = train.get("capacity").asInt();
                    int bookedSeats = adminService.getBookedSeatsForSchedule(schedule);
                    availableSeats = totalCapacity - bookedSeats;
                } catch (Exception e) {
                    showError("Error getting booked seats: " + e.getMessage());
                }
            }
            availableSeatsLabel.setText(String.valueOf(availableSeats));

            JsonNode cost = schedule.get("cost");
            costLabel.setText("$" + (cost != null ? cost.asText() : "0"));

            // Update spinner max value based on available seats
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) seatsSpinner.getValueFactory())
                .setMax(Math.max(1, availableSeats));
        } else {
            clearScheduleDetails();
        }
    }

    private void clearScheduleDetails() {
        trainNameLabel.setText("");
        routeLabel.setText("");
        timeLabel.setText("");
        availableSeatsLabel.setText("");
        costLabel.setText("");
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setBooking(JsonNode booking) {
        this.editingBooking = booking;
        if (booking != null) {
            dialogTitle.setText("Edit Booking");
            
            // Find and select the user
            JsonNode bookingUser = booking.get("user");
            if (bookingUser != null) {
                JsonNode userId = bookingUser.get("id");
                if (userId != null) {
                    userComboBox.getItems().stream()
                        .filter(user -> {
                            JsonNode id = user.get("id");
                            return id != null && id.asInt() == userId.asInt();
                        })
                        .findFirst()
                        .ifPresent(user -> userComboBox.setValue(user));
                }
            }

            // Find and select the schedule
            JsonNode bookingSchedule = booking.get("schedule");
            if (bookingSchedule != null) {
                JsonNode scheduleId = bookingSchedule.get("id");
                if (scheduleId != null) {
                    scheduleComboBox.getItems().stream()
                        .filter(schedule -> {
                            JsonNode id = schedule.get("id");
                            return id != null && id.asInt() == scheduleId.asInt();
                        })
                        .findFirst()
                        .ifPresent(schedule -> scheduleComboBox.setValue(schedule));
                }
            }

            JsonNode seats = booking.get("numberOfSeats");
            if (seats != null) {
                seatsSpinner.getValueFactory().setValue(seats.asInt());
            }

            JsonNode status = booking.get("status");
            if (status != null) {
                statusComboBox.setValue(status.asText());
            }
        }
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    public void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
        if (viewOnly) {
            saveButton.setVisible(false);
            saveButton.setManaged(false);
            scheduleComboBox.setDisable(true);
            userComboBox.setDisable(true);
            seatsSpinner.setDisable(true);
            statusComboBox.setDisable(true);
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            try {
                saveBooking();
                if (onSave != null) {
                    onSave.run();
                }
                closeDialog();
            } catch (Exception e) {
                showError("Failed to save booking: " + e.getMessage());
            }
        }
    }

    private void saveBooking() throws Exception {
        ObjectNode bookingData = objectMapper.createObjectNode();
        if (editingBooking != null) {
            bookingData.put("id", editingBooking.get("id").asInt());
        }

        bookingData.set("schedule", scheduleComboBox.getValue());
        bookingData.set("passenger", userComboBox.getValue());
        bookingData.put("seats", seatsSpinner.getValue());
        bookingData.put("status", statusComboBox.getValue());

        if (editingBooking != null) {
            adminService.updateBooking(editingBooking.get("id").asInt(), bookingData);
            CustomAlert.showInformation("Success", "Booking updated successfully");
        } else {
            adminService.createBooking(bookingData);
            CustomAlert.showInformation("Success", "Booking created successfully");
        }

        saveClicked = true;
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void onCancelClicked() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        if (type == Alert.AlertType.ERROR) {
            CustomAlert.showError(title, message);
        } else {
            CustomAlert.showInformation(title, message);
        }
    }

    private void showError(String message) {
        CustomAlert.showError("Error", message);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (scheduleComboBox.getValue() == null) {
            errors.append("Please select a schedule\n");
        }
        if (userComboBox.getValue() == null) {
            errors.append("Please select a passenger\n");
        }
        if (seatsSpinner.getValue() == null || seatsSpinner.getValue() < 1) {
            errors.append("Please select number of seats\n");
        }
        if (statusComboBox.getValue() == null || statusComboBox.getValue().trim().isEmpty()) {
            errors.append("Please select a status\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }
}
