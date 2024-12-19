package com.mouad.frontend.Controllers.Admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.mouad.frontend.Components.CustomAlert;
import com.mouad.frontend.Services.ScheduleService;
import com.mouad.frontend.Services.TrainService;

public class ScheduleDialogController {
    @FXML private ComboBox<JsonNode> trainComboBox;
    @FXML private TextField departureField;
    @FXML private TextField destinationField;
    @FXML private DatePicker departureDatePicker;
    @FXML private TextField departureTimeField;
    @FXML private DatePicker arrivalDatePicker;
    @FXML private TextField arrivalTimeField;
    @FXML private TextField costField;
    @FXML private CheckBox availableCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final ScheduleService scheduleService;
    private final TrainService trainService;
    private final ObjectMapper objectMapper;
    private JsonNode schedule;
    private Runnable onSave;

    public ScheduleDialogController() {
        this.scheduleService = ScheduleService.getInstance();
        this.trainService = TrainService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @FXML
    public void initialize() {
        try {
            loadTrains();
            setupValidation();
            setupButtons();
        } catch (Exception e) {
            System.err.println("Error initializing dialog: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to initialize dialog: " + e.getMessage());
        }
    }

    private void loadTrains() {
        try {
            JsonNode trains = trainService.getAllTrains();
            if (trains != null && trains.isArray()) {
                ObservableList<JsonNode> trainsList = FXCollections.observableArrayList();
                trains.forEach(trainsList::add);
                trainComboBox.setItems(trainsList);
                
                trainComboBox.setCellFactory(param -> new ListCell<>() {
                    @Override
                    protected void updateItem(JsonNode item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.get("trainName").asText());
                        }
                    }
                });
                
                trainComboBox.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(JsonNode item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.get("trainName").asText());
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading trains: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to load trains: " + e.getMessage());
        }
    }

    private void setupValidation() {
        // Setup time field validation and formatting for departure time
        departureTimeField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$") || // Complete time (HH:mm)
                newText.matches("^([0-1]?[0-9]|2[0-3])$") || // Hours only
                newText.matches("^([0-1]?[0-9]|2[0-3]):([0-5])?$") || // Hours with colon and optional first minute digit
                newText.isEmpty()) { // Empty field
                return change;
            }
            return null;
        }));

        // Setup time field validation and formatting for arrival time
        arrivalTimeField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$") || // Complete time (HH:mm)
                newText.matches("^([0-1]?[0-9]|2[0-3])$") || // Hours only
                newText.matches("^([0-1]?[0-9]|2[0-3]):([0-5])?$") || // Hours with colon and optional first minute digit
                newText.isEmpty()) { // Empty field
                return change;
            }
            return null;
        }));

        // Add focus listeners to format time when leaving the field
        departureTimeField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                formatTimeField(departureTimeField);
            }
        });

        arrivalTimeField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                formatTimeField(arrivalTimeField);
            }
        });

        costField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*\\.?\\d*") || newText.isEmpty()) {
                return change;
            }
            return null;
        }));
    }

    private void formatTimeField(TextField timeField) {
        String text = timeField.getText().trim();
        if (!text.isEmpty()) {
            // Handle just hours
            if (text.matches("^[0-9]{1,2}$")) {
                int hours = Integer.parseInt(text);
                if (hours >= 0 && hours <= 23) {
                    timeField.setText(String.format("%02d:00", hours));
                }
            }
            // Handle hours with colon
            else if (text.matches("^[0-9]{1,2}:$")) {
                int hours = Integer.parseInt(text.replace(":", ""));
                if (hours >= 0 && hours <= 23) {
                    timeField.setText(String.format("%02d:00", hours));
                }
            }
            // Handle partial minutes
            else if (text.matches("^[0-9]{1,2}:[0-9]$")) {
                String[] parts = text.split(":");
                int hours = Integer.parseInt(parts[0]);
                if (hours >= 0 && hours <= 23) {
                    timeField.setText(String.format("%02d:%s0", hours, parts[1]));
                }
            }
            // Handle complete time but ensure proper formatting
            else if (text.matches("^[0-9]{1,2}:[0-9]{2}$")) {
                String[] parts = text.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
                    timeField.setText(String.format("%02d:%02d", hours, minutes));
                }
            }
        }
    }

    private void setupButtons() {
        saveButton.setOnAction(event -> saveSchedule());
        cancelButton.setOnAction(event -> closeDialog());
    }

    public void setSchedule(JsonNode schedule) {
        this.schedule = schedule;
        if (schedule != null) {
            // Find and select the train in the combo box
            JsonNode scheduleTrain = schedule.get("train");
            trainComboBox.getItems().forEach(train -> {
                if (train.get("id").asInt() == scheduleTrain.get("id").asInt()) {
                    trainComboBox.setValue(train);
                }
            });

            departureField.setText(schedule.get("departure").asText());
            destinationField.setText(schedule.get("destination").asText());

            LocalDateTime departureDateTime = LocalDateTime.parse(schedule.get("departureTime").asText());
            departureDatePicker.setValue(departureDateTime.toLocalDate());
            departureTimeField.setText(departureDateTime.format(DateTimeFormatter.ofPattern("HH:mm")));

            LocalDateTime arrivalDateTime = LocalDateTime.parse(schedule.get("arrivalTime").asText());
            arrivalDatePicker.setValue(arrivalDateTime.toLocalDate());
            arrivalTimeField.setText(arrivalDateTime.format(DateTimeFormatter.ofPattern("HH:mm")));

            costField.setText(String.format("%.2f", schedule.get("cost").asDouble()));
            availableCheckBox.setSelected(schedule.get("available").asBoolean());
        }
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    private void saveSchedule() {
        if (!validateInput()) {
            return;
        }

        try {
            ObjectNode scheduleData = objectMapper.createObjectNode();
            if (schedule != null) {
                scheduleData.put("id", schedule.get("id").asInt());
            }

            JsonNode selectedTrain = trainComboBox.getValue();
            scheduleData.set("train", selectedTrain);
            scheduleData.put("departure", departureField.getText());
            scheduleData.put("destination", destinationField.getText());

            LocalDateTime departureDateTime = LocalDateTime.of(
                departureDatePicker.getValue(),
                LocalTime.parse(departureTimeField.getText())
            );
            scheduleData.put("departureTime", departureDateTime.toString());

            LocalDateTime arrivalDateTime = LocalDateTime.of(
                arrivalDatePicker.getValue(),
                LocalTime.parse(arrivalTimeField.getText())
            );
            scheduleData.put("arrivalTime", arrivalDateTime.toString());

            scheduleData.put("cost", Double.parseDouble(costField.getText()));
            scheduleData.put("available", availableCheckBox.isSelected());

            if (schedule != null) {
                scheduleService.updateSchedule(schedule.get("id").asInt(), scheduleData);
            } else {
                scheduleService.createSchedule(scheduleData);
            }

            if (onSave != null) {
                onSave.run();
            }
            closeDialog();

        } catch (Exception e) {
            System.err.println("Error saving schedule: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to save schedule: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (trainComboBox.getValue() == null) {
            errorMessage.append("Please select a train\n");
        }
        if (departureField.getText().trim().isEmpty()) {
            errorMessage.append("Please enter a departure location\n");
        }
        if (destinationField.getText().trim().isEmpty()) {
            errorMessage.append("Please enter a destination\n");
        }
        if (departureDatePicker.getValue() == null) {
            errorMessage.append("Please select a departure date\n");
        }
        if (departureTimeField.getText().trim().isEmpty()) {
            errorMessage.append("Please enter a departure time\n");
        }
        if (arrivalDatePicker.getValue() == null) {
            errorMessage.append("Please select an arrival date\n");
        }
        if (arrivalTimeField.getText().trim().isEmpty()) {
            errorMessage.append("Please enter an arrival time\n");
        }
        if (costField.getText().trim().isEmpty()) {
            errorMessage.append("Please enter a cost\n");
        }

        if (errorMessage.length() > 0) {
            CustomAlert.showError("Validation Error", errorMessage.toString());
            return false;
        }

        // Validate that arrival time is after departure time
        LocalDateTime departureDateTime = LocalDateTime.of(
            departureDatePicker.getValue(),
            LocalTime.parse(departureTimeField.getText())
        );
        LocalDateTime arrivalDateTime = LocalDateTime.of(
            arrivalDatePicker.getValue(),
            LocalTime.parse(arrivalTimeField.getText())
        );

        if (arrivalDateTime.isBefore(departureDateTime) || arrivalDateTime.equals(departureDateTime)) {
            CustomAlert.showError("Validation Error", "Arrival time must be after departure time");
            return false;
        }

        return true;
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        if (type == Alert.AlertType.CONFIRMATION) {
            CustomAlert.showConfirmation(title, content);
        } else {
            CustomAlert.showInformation(title, content);
        }
    }
}
