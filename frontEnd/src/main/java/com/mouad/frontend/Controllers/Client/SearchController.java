package com.mouad.frontend.Controllers.Client;

import com.mouad.frontend.Components.CustomAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.util.Map;

import javafx.fxml.Initializable;
import javafx.scene.control.cell.MapValueFactory;
import javafx.util.StringConverter;
import com.mouad.frontend.Views.ViewsFactory;
import com.mouad.frontend.Services.UserService;
import com.mouad.frontend.Models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchController implements Initializable {
    @FXML private ImageView homeIcon;
    @FXML private ImageView searchIcon;
    @FXML private ImageView profileIcon;
    @FXML private ImageView bookingsIcon;
    @FXML private ImageView logoutIcon;
    @FXML private Label homeLabel;
    @FXML private Label searchLabel;
    @FXML private Label profileLabel;
    @FXML private Label bookingsLabel;
    @FXML private Label logoutLabel;
    
    // Search Form Components
    @FXML private ComboBox<String> fromStationCombo;
    @FXML private ComboBox<String> toStationCombo;
    @FXML private DatePicker travelDate;
    @FXML private Button searchButton;
    @FXML private Button clearFiltersButton;
    
    // Results Table
    @FXML private TableView<Map<String, Object>> schedulesTable;
    @FXML private TableColumn<Map<String, Object>, String> scheduleIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> trainNumberColumn;
    @FXML private TableColumn<Map<String, Object>, String> departureColumn;
    @FXML private TableColumn<Map<String, Object>, String> destinationColumn;
    @FXML private TableColumn<Map<String, Object>, String> departureTimeColumn;
    @FXML private TableColumn<Map<String, Object>, String> arrivalTimeColumn;
    @FXML private TableColumn<Map<String, Object>, Number> costColumn;
    @FXML private TableColumn<Map<String, Object>, Number> seatsColumn;
    @FXML private TableColumn<Map<String, Object>, Button> actionColumn;
    
    // Booking Form Components
    @FXML private VBox bookingForm;
    @FXML private Label selectedTrainLabel;
    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private Label totalPriceLabel;
    @FXML private Button confirmBookingButton;
    @FXML private Button cancelBookingButton;
    
    private UserService userService;
    private Map<String, Object> selectedSchedule;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = UserService.getInstance();
        setActiveButton(searchIcon, searchLabel);
        
        // Initialize departure stations
        List<String> departures = userService.getAllDepartures();
        fromStationCombo.setItems(FXCollections.observableArrayList(departures));
        
        // Add listener to fromStationCombo to update destinations when departure is selected
        fromStationCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                List<String> destinations = userService.getDestinationsForDeparture(newValue);
                toStationCombo.setItems(FXCollections.observableArrayList(destinations));
                toStationCombo.setValue(null); // Clear previous selection
            }
        });
        
        // Initialize date picker with today's date
        travelDate.setValue(LocalDate.now());
        
        // Initialize table columns with proper widths
        scheduleIdColumn.setPrefWidth(100);
        trainNumberColumn.setPrefWidth(100);
        departureColumn.setPrefWidth(120);
        destinationColumn.setPrefWidth(120);
        departureTimeColumn.setPrefWidth(150);
        arrivalTimeColumn.setPrefWidth(150);
        costColumn.setPrefWidth(80);
        seatsColumn.setPrefWidth(100);
        actionColumn.setPrefWidth(100);
        
        // Setup cell value factories
        scheduleIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().get("scheduleId"))));
        trainNumberColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().get("trainNumber"))));
        departureColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().get("from"))));
        destinationColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().get("to"))));
        departureTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().get("departureTime"))));
        arrivalTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().get("arrivalTime"))));
        costColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("cost");
            if (value != null) {
                return new SimpleObjectProperty<>(Double.valueOf(String.valueOf(value)));
            }
            return new SimpleObjectProperty<>(0.0);
        });
        seatsColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("availableSeats");
            if (value != null) {
                return new SimpleObjectProperty<>(Integer.valueOf(String.valueOf(value)));
            }
            return new SimpleObjectProperty<>(0);
        });
        
        // Setup action column with book buttons
        actionColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Button>() {
            private final Button bookButton = new Button("Book");
            {
                bookButton.setStyle("-fx-background-color: #da9020; -fx-text-fill: white;");
                bookButton.setOnAction(event -> {
                    Map<String, Object> schedule = getTableView().getItems().get(getIndex());
                    showBookingForm(schedule);
                });
            }
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(bookButton);
                }
            }
        });
        
        // Setup seats spinner listener
        seatsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (selectedSchedule != null) {
                updateTotalPrice();
            }
        });
    }
    
    @FXML
    private void onSearchSchedules() {
        String from = fromStationCombo.getValue();
        String to = toStationCombo.getValue();
        LocalDate date = travelDate.getValue();
        
        // Validate inputs
        if (from == null || to == null || date == null) {
            CustomAlert.showError("Invalid Input", "Please fill in all search fields");
            return;
        }
        
        if (from.equals(to)) {
            CustomAlert.showError("Invalid Station Selection", "Departure and destination stations cannot be the same");
            return;
        }
        
        try {
            List<Map<String, Object>> schedules = userService.searchSchedules(from, to, date);
            
            // Clear existing items
            schedulesTable.getItems().clear();
            
            if (schedules.isEmpty()) {
                CustomAlert.showInfo("No Results", "No schedules found for the selected criteria");
                return;
            }
            
            // Process and add schedules to the table
            ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList();
            
            for (Map<String, Object> schedule : schedules) {
                // Create a new map for each schedule to ensure all required fields are present
                Map<String, Object> tableSchedule = new HashMap<>();
                
                // Copy and validate all required fields
                tableSchedule.put("scheduleId", schedule.getOrDefault("scheduleId", ""));
                tableSchedule.put("trainNumber", schedule.getOrDefault("trainNumber", ""));
                tableSchedule.put("from", schedule.getOrDefault("departure", ""));
                tableSchedule.put("to", schedule.getOrDefault("destination", ""));
                tableSchedule.put("departureTime", schedule.getOrDefault("departureTime", ""));
                tableSchedule.put("arrivalTime", schedule.getOrDefault("arrivalTime", ""));
                tableSchedule.put("cost", schedule.getOrDefault("cost", 0.0));
                tableSchedule.put("availableSeats", schedule.getOrDefault("availableSeats", 0));
                
                // Debug print
                System.out.println("Processed schedule: " + tableSchedule);
                
                tableData.add(tableSchedule);
            }
            
            schedulesTable.setItems(tableData);
            
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showError("Error", "Failed to search schedules: " + e.getMessage());
        }
    }
    
    @FXML
    private void onClearFilters() {
        fromStationCombo.setValue(null);
        toStationCombo.setValue(null);
        travelDate.setValue(null);
        schedulesTable.getItems().clear();
    }
    
    private void showBookingForm(Map<String, Object> schedule) {
        try {
            // Debug print
            System.out.println("Opening booking dialog for schedule: " + schedule);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/BookingDialog.fxml"));
            Parent root = loader.load();
            
            BookingDialogController controller = loader.getController();
            controller.setScheduleData(new HashMap<>(schedule)); // Pass a copy of the schedule data
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/BookingDialog.css").toExternalForm());
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(scene);
            
            // Center the dialog on the screen
            dialogStage.centerOnScreen();
            
            // Debug print
            System.out.println("Showing booking dialog...");
            
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showError("Error", "Could not open booking form: " + e.getMessage());
        }
    }
    
    private void updateTotalPrice() {
        double totalPrice = ((Number)selectedSchedule.get("cost")).doubleValue() * seatsSpinner.getValue();
        totalPriceLabel.setText(String.format("$%.2f", totalPrice));
    }
    
    @FXML
    private void onConfirmBooking() {
        if (selectedSchedule == null) return;
        
        try {
            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("scheduleId", selectedSchedule.get("scheduleId"));
            bookingData.put("numSeats", seatsSpinner.getValue());
            bookingData.put("totalCost", ((Number)selectedSchedule.get("cost")).doubleValue() * seatsSpinner.getValue());
            bookingData.put("bookingTime", LocalDateTime.now().toString());
            
            // Get current user ID from UserService
            Long userId = userService.getCurrentUserId();
            if (userId == null) {
                CustomAlert.showError("Error", "User not logged in. Please log in and try again.");
                return;
            }
            bookingData.put("userId", userId);
            
            boolean success = userService.createBooking(bookingData);
            if (success) {
                CustomAlert.showSuccess("Booking Successful", "Your train ticket has been booked successfully!");
                bookingForm.setVisible(false);
                onSearchSchedules(); // Refresh schedule list
            } else {
                CustomAlert.showError("Booking Error", "Failed to create booking. Please try again.");
            }
        } catch (Exception e) {
            CustomAlert.showError("Error", "An error occurred: " + e.getMessage());
        }
    }
    
    @FXML
    private void onCancelBooking() {
        bookingForm.setVisible(false);
        selectedSchedule = null;
    }
    
    private void setActiveButton(ImageView icon, Label label) {
        // Remove active class from all icons and labels
        homeIcon.getStyleClass().remove("active");
        searchIcon.getStyleClass().remove("active");
        profileIcon.getStyleClass().remove("active");
        bookingsIcon.getStyleClass().remove("active");
        logoutIcon.getStyleClass().remove("active");

        homeLabel.getStyleClass().remove("active");
        searchLabel.getStyleClass().remove("active");
        profileLabel.getStyleClass().remove("active");
        bookingsLabel.getStyleClass().remove("active");
        logoutLabel.getStyleClass().remove("active");

        // Add active class to selected icon and label
        icon.getStyleClass().add("active");
        label.getStyleClass().add("active");
    }

    @FXML
    private void onHomeClicked() {
        navigateToPage("Client/MainPage.fxml");
    }

    @FXML
    private void onSearchClicked() {
        navigateToPage("Client/Search.fxml");
    }

    @FXML
    private void onProfileClicked() {
        navigateToPage("Client/Profile.fxml");
    }

    @FXML
    private void onBookingsClicked() {
        navigateToPage("Client/Bookings.fxml");
    }

    @FXML
    private void onLogoutClicked() {
        try {
            // Close current window
            Stage currentStage = (Stage) logoutIcon.getScene().getWindow();
            currentStage.close();
            
            // Show login window
            ViewsFactory.showWindow("Login.fxml", "Login");
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showError("Logout Error", "Could not return to login page: " + e.getMessage());
        }
    }

    private void navigateToPage(String fxmlFile) {
        try {
            System.out.println("Attempting to navigate to: " + fxmlFile);
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            
            // Debug the FXML resource location
            URL fxmlResource = getClass().getResource("/Fxml/" + fxmlFile);
            System.out.println("FXML Resource URL: " + fxmlResource);
            
            FXMLLoader loader = new FXMLLoader(fxmlResource);
            if (loader.getLocation() == null) {
                throw new IOException("Could not find FXML file: /Fxml/" + fxmlFile);
            }
            
            try {
                Parent root = loader.load();
                Scene scene = new Scene(root);
                
                // Get the CSS file name from the FXML file name
                String cssFileName = fxmlFile.substring(fxmlFile.lastIndexOf('/') + 1).replace(".fxml", ".css");
                URL cssResource = getClass().getResource("/styles/Client/" + cssFileName);
                System.out.println("CSS Resource URL: " + cssResource);
                
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                    System.out.println("Successfully loaded CSS from: /styles/Client/" + cssFileName);
                } else {
                    System.err.println("Warning: Could not load CSS file from: /styles/Client/" + cssFileName);
                }
                
                stage.setScene(scene);
            } catch (IOException e) {
                System.err.println("Error loading FXML content: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } catch (IOException e) {
            System.err.println("Error navigating to " + fxmlFile + ": " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            CustomAlert.showError("Navigation Error", "Could not load page: " + e.getMessage() + "\nCheck console for details.");
        }
    }
}
