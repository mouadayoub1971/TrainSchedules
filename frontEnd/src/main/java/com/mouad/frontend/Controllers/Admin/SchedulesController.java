package com.mouad.frontend.Controllers.Admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.mouad.frontend.Views.ViewsFactory;
import com.mouad.frontend.Components.CustomAlert;
import com.mouad.frontend.Services.ScheduleService;
import com.mouad.frontend.Services.TrainService;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import java.util.Optional;
import javafx.scene.control.ButtonType;

public class SchedulesController {

    @FXML private ImageView homeIcon;
    @FXML private ImageView trainsIcon;
    @FXML private ImageView schedulesIcon;
    @FXML private ImageView bookingsIcon;
    @FXML private ImageView statisticsIcon;
    @FXML private ImageView logoutIcon;
    @FXML private Label trainsLabel;
    @FXML private Label schedulesLabel;
    @FXML private Label bookingsLabel;
    @FXML private Label statisticsLabel;

    @FXML private ComboBox<JsonNode> trainFilterCombo;
    @FXML private TextField departureFilterField;
    @FXML private TextField destinationFilterField;
    @FXML private DatePicker dateFilterPicker;
    
    @FXML private TableView<JsonNode> schedulesTable;
    @FXML private TableColumn<JsonNode, String> trainColumn;
    @FXML private TableColumn<JsonNode, String> departureColumn;
    @FXML private TableColumn<JsonNode, String> destinationColumn;
    @FXML private TableColumn<JsonNode, String> departureTimeColumn;
    @FXML private TableColumn<JsonNode, String> arrivalTimeColumn;
    @FXML private TableColumn<JsonNode, String> costColumn;
    @FXML private TableColumn<JsonNode, String> availableColumn;
    @FXML private TableColumn<JsonNode, Void> actionsColumn;

    private final ScheduleService scheduleService;
    private final TrainService trainService;
    private final ObjectMapper objectMapper;
    private ObservableList<JsonNode> schedulesList;

    public SchedulesController() {
        this.scheduleService = ScheduleService.getInstance();
        this.trainService = TrainService.getInstance();
        this.objectMapper = new ObjectMapper();
        this.schedulesList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        try {
            // Set up navigation click handlers
            setupNavigationHandlers();
            
            // Initialize table columns
            setupTableColumns();
            
            // Load trains for filter
            loadTrains();
            
            // Load initial data
            loadSchedules();
            
            // Setup filters
            setupFilters();
            
        } catch (Exception e) {
            System.err.println("Error initializing SchedulesController: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showError("Error", "Failed to initialize schedules page: " + e.getMessage());
        }
    }

    private void setupNavigationHandlers() {
        homeIcon.setOnMouseClicked(event -> onHomeClicked());
        trainsIcon.setOnMouseClicked(event -> onTrainsClicked());
        schedulesIcon.setOnMouseClicked(event -> onSchedulesClicked());
        bookingsIcon.setOnMouseClicked(event -> onBookingsClicked());
        statisticsIcon.setOnMouseClicked(event -> onStatisticsClicked());
        logoutIcon.setOnMouseClicked(event -> onLogoutClicked());
        
        trainsLabel.setOnMouseClicked(event -> onTrainsClicked());
        schedulesLabel.setOnMouseClicked(event -> onSchedulesClicked());
        bookingsLabel.setOnMouseClicked(event -> onBookingsClicked());
        statisticsLabel.setOnMouseClicked(event -> onStatisticsClicked());
    }

    private void setupTableColumns() {
        trainColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("train").get("trainName").asText()));
            
        departureColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("departure").asText()));
            
        destinationColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("destination").asText()));
            
        departureTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatDateTime(cellData.getValue().get("departureTime").asText())));
            
        arrivalTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatDateTime(cellData.getValue().get("arrivalTime").asText())));
            
        costColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.2f", cellData.getValue().get("cost").asDouble())));
            
        availableColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("available").asBoolean() ? "Yes" : "No"));
        
        setupActionsColumn();
        
        schedulesTable.setItems(schedulesList);
    }

    private String formatDateTime(String dateTimeStr) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.getStyleClass().addAll("action-button");
                deleteBtn.getStyleClass().addAll("action-button", "cancel-button");
                
                // Set minimum width for consistent button sizes
                editBtn.setMinWidth(80);
                deleteBtn.setMinWidth(80);
                
                editBtn.setOnAction(event -> {
                    JsonNode schedule = getTableView().getItems().get(getIndex());
                    editSchedule(schedule);
                });
                
                deleteBtn.setOnAction(event -> {
                    JsonNode schedule = getTableView().getItems().get(getIndex());
                    deleteSchedule(schedule);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);  // Increased spacing between buttons
                    container.setAlignment(javafx.geometry.Pos.CENTER);  // Center the buttons
                    container.getChildren().addAll(editBtn, deleteBtn);
                    setGraphic(container);
                }
            }
        });
    }

    private void loadTrains() {
        try {
            JsonNode trains = trainService.getAllTrains();
            if (trains != null && trains.isArray()) {
                ObservableList<JsonNode> trainsList = FXCollections.observableArrayList();
                trains.forEach(trainsList::add);
                trainFilterCombo.setItems(trainsList);
                
                // Set the display converter to show only the train name
                trainFilterCombo.setConverter(new StringConverter<JsonNode>() {
                    @Override
                    public String toString(JsonNode train) {
                        return train != null ? train.get("trainName").asText() : "";
                    }

                    @Override
                    public JsonNode fromString(String string) {
                        return null; // Not needed for this use case
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading trains: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to load trains: " + e.getMessage());
        }
    }

    private void loadSchedules() {
        try {
            JsonNode schedules = scheduleService.getAllSchedules();
            if (schedules != null && schedules.isArray()) {
                schedulesList.clear();
                schedules.forEach(schedulesList::add);
            }
        } catch (Exception e) {
            System.err.println("Error loading schedules: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to load schedules: " + e.getMessage());
        }
    }

    private void setupFilters() {
        trainFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        departureFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        destinationFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateFilterPicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        try {
            JsonNode selectedTrain = trainFilterCombo.getValue();
            String departure = departureFilterField.getText().toLowerCase();
            String destination = destinationFilterField.getText().toLowerCase();
            LocalDate selectedDate = dateFilterPicker.getValue();

            JsonNode schedules = scheduleService.getAllSchedules();
            schedulesList.clear();
            
            if (schedules != null && schedules.isArray()) {
                schedules.forEach(schedule -> {
                    boolean matchTrain = selectedTrain == null || 
                        schedule.get("train").get("id").asInt() == selectedTrain.get("id").asInt();
                    boolean matchDeparture = departure.isEmpty() || 
                        schedule.get("departure").asText().toLowerCase().contains(departure);
                    boolean matchDestination = destination.isEmpty() || 
                        schedule.get("destination").asText().toLowerCase().contains(destination);
                    
                    boolean matchDate = true;
                    if (selectedDate != null) {
                        LocalDateTime scheduleDateTime = LocalDateTime.parse(schedule.get("departureTime").asText());
                        matchDate = scheduleDateTime.toLocalDate().equals(selectedDate);
                    }

                    if (matchTrain && matchDeparture && matchDestination && matchDate) {
                        schedulesList.add(schedule);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error applying filters: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to apply filters: " + e.getMessage());
        }
    }

    @FXML
    private void onAddScheduleClicked() {
        try {
            showScheduleDialog(null);
        } catch (Exception e) {
            System.err.println("Error showing add schedule dialog: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to show add schedule dialog: " + e.getMessage());
        }
    }

    private void editSchedule(JsonNode schedule) {
        try {
            showScheduleDialog(schedule);
        } catch (Exception e) {
            System.err.println("Error showing edit schedule dialog: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to show edit schedule dialog: " + e.getMessage());
        }
    }

    private void deleteSchedule(JsonNode schedule) {
        try {
            Optional<ButtonType> result = CustomAlert.showConfirmation(
                "Delete Schedule", 
                "Are you sure you want to delete this schedule?"
            );
            if (result.isPresent() && result.get() == ButtonType.OK) {
                scheduleService.deleteSchedule(schedule.get("id").asInt());
                loadSchedules();
                CustomAlert.showSuccess("Success", "Schedule deleted successfully");
            }
        } catch (Exception e) {
            System.err.println("Error deleting schedule: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to delete schedule: " + e.getMessage());
        }
    }

    private void showScheduleDialog(JsonNode schedule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/ScheduleDialog.fxml"));
            Parent root = loader.load();
            
            ScheduleDialogController controller = loader.getController();
            controller.setSchedule(schedule);
            controller.setOnSave(() -> {
                loadSchedules();
                CustomAlert.showSuccess("Success", schedule == null ? "Schedule added successfully" : "Schedule updated successfully");
            });

            Stage dialogStage = new Stage();
            dialogStage.setTitle(schedule == null ? "Add Schedule" : "Edit Schedule");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("Error showing schedule dialog: " + e.getMessage());
            CustomAlert.showError("Error", "Failed to show schedule dialog: " + e.getMessage());
        }
    }

    @FXML
    private void onClearFiltersClicked() {
        trainFilterCombo.setValue(null);
        departureFilterField.clear();
        destinationFilterField.clear();
        dateFilterPicker.setValue(null);
    }

    @FXML
    private void onHomeClicked() {
        navigateToPage("Admin/MainPage.fxml");
    }

    @FXML
    private void onTrainsClicked() {
        navigateToPage("Admin/TrainsPage.fxml");
    }

    @FXML
    private void onSchedulesClicked() {
        navigateToPage("Admin/SchedulesPage.fxml");
    }

    @FXML
    private void onBookingsClicked() {
        navigateToPage("Admin/BookingsPage.fxml");
    }

    @FXML
    private void onStatisticsClicked() {
        navigateToPage("Admin/StatisticsPage.fxml");
    }

    @FXML
    private void onLogoutClicked() {
        try {
            Stage currentStage = (Stage) logoutIcon.getScene().getWindow();
            currentStage.close();
            ViewsFactory.showWindow("Login.fxml", "Login");
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            CustomAlert.showError("Logout Error", "Could not return to login page: " + e.getMessage());
        }
    }

    private void navigateToPage(String fxmlFile) {
        try {
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            URL cssResource = getClass().getResource("/styles/MainPage.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error navigating to page: " + e.getMessage());
            CustomAlert.showError("Navigation Error", "Could not navigate to " + fxmlFile + ": " + e.getMessage());
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        if (type == Alert.AlertType.CONFIRMATION) {
            CustomAlert.showConfirmation(title, content);
        } else {
            CustomAlert.showInformation(title, content);
        }
    }
}
