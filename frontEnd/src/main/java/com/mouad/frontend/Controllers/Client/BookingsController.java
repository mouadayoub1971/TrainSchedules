package com.mouad.frontend.Controllers.Client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import com.mouad.frontend.Views.ViewsFactory;
import com.mouad.frontend.Services.UserService;
import com.mouad.frontend.Services.BookingService;
import com.mouad.frontend.Components.CustomAlert;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BookingsController implements Initializable {
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

    @FXML private TableView<Map<String, Object>> bookingsTable;
    @FXML private TableColumn<Map<String, Object>, String> bookingIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> trainNumberColumn;
    @FXML private TableColumn<Map<String, Object>, String> departureColumn;
    @FXML private TableColumn<Map<String, Object>, String> destinationColumn;
    @FXML private TableColumn<Map<String, Object>, String> departureTimeColumn;
    @FXML private TableColumn<Map<String, Object>, String> arrivalTimeColumn;
    @FXML private TableColumn<Map<String, Object>, Number> seatsColumn;
    @FXML private TableColumn<Map<String, Object>, Number> costColumn;
    @FXML private TableColumn<Map<String, Object>, String> statusColumn;
    @FXML private TableColumn<Map<String, Object>, Button> actionColumn;

    private UserService userService;
    private BookingService bookingService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = UserService.getInstance();
        bookingService = BookingService.getInstance();
        
        // Set active button for bookings page
        setActiveButton(bookingsIcon, bookingsLabel);

        // Set table properties
        bookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bookingsTable.setStyle("-fx-background-color: transparent;");

        // Initialize table columns
        initializeColumns();
        
        // Load bookings
        loadBookings();
    }

    private void initializeColumns() {
        // Initialize booking ID column
        bookingIdColumn.setCellValueFactory(data -> {
            try {
                return new SimpleStringProperty(data.getValue().get("id").toString());
            } catch (Exception e) {
                System.err.println("Error getting booking ID: " + e.getMessage());
                return new SimpleStringProperty("");
            }
        });
        
        // Initialize train number column
        trainNumberColumn.setCellValueFactory(data -> {
            try {
                Map<String, Object> schedule = (Map<String, Object>) data.getValue().get("schedule");
                if (schedule == null) {
                    System.err.println("Schedule is null");
                    return new SimpleStringProperty("");
                }
                
                System.out.println("Schedule data: " + schedule);
                
                Map<String, Object> train = (Map<String, Object>) schedule.get("train");
                if (train == null) {
                    System.err.println("Train is null");
                    return new SimpleStringProperty("");
                }
                
                System.out.println("Train data: " + train);
                
                Object trainNumber = train.get("trainNumber");
                if (trainNumber == null) {
                    trainNumber = train.get("trainName"); // Fallback to trainName if trainNumber is null
                }
                
                if (trainNumber == null) {
                    System.err.println("Both trainNumber and trainName are null");
                    return new SimpleStringProperty("");
                }
                
                return new SimpleStringProperty(trainNumber.toString());
            } catch (Exception e) {
                System.err.println("Error getting train number: " + e.getMessage());
                e.printStackTrace();
                return new SimpleStringProperty("");
            }
        });
        
        // Initialize departure column
        departureColumn.setCellValueFactory(data -> {
            try {
                Map<String, Object> schedule = (Map<String, Object>) data.getValue().get("schedule");
                return new SimpleStringProperty(schedule.get("departure").toString());
            } catch (Exception e) {
                System.err.println("Error getting departure: " + e.getMessage());
                return new SimpleStringProperty("");
            }
        });
        
        // Initialize destination column
        destinationColumn.setCellValueFactory(data -> {
            try {
                Map<String, Object> schedule = (Map<String, Object>) data.getValue().get("schedule");
                return new SimpleStringProperty(schedule.get("destination").toString());
            } catch (Exception e) {
                System.err.println("Error getting destination: " + e.getMessage());
                return new SimpleStringProperty("");
            }
        });
        
        // Initialize departure time column
        departureTimeColumn.setCellValueFactory(data -> {
            try {
                Map<String, Object> schedule = (Map<String, Object>) data.getValue().get("schedule");
                return new SimpleStringProperty(schedule.get("departureTime").toString());
            } catch (Exception e) {
                System.err.println("Error getting departure time: " + e.getMessage());
                return new SimpleStringProperty("");
            }
        });
        
        // Initialize arrival time column
        arrivalTimeColumn.setCellValueFactory(data -> {
            try {
                Map<String, Object> schedule = (Map<String, Object>) data.getValue().get("schedule");
                return new SimpleStringProperty(schedule.get("arrivalTime").toString());
            } catch (Exception e) {
                System.err.println("Error getting arrival time: " + e.getMessage());
                return new SimpleStringProperty("");
            }
        });
        
        // Initialize seats column
        seatsColumn.setCellValueFactory(data -> {
            try {
                return new SimpleIntegerProperty(Integer.parseInt(data.getValue().get("numberOfSeats").toString()));
            } catch (Exception e) {
                System.err.println("Error getting number of seats: " + e.getMessage());
                return new SimpleIntegerProperty(0);
            }
        });
        
        // Initialize cost column
        costColumn.setCellValueFactory(data -> {
            try {
                Map<String, Object> schedule = (Map<String, Object>) data.getValue().get("schedule");
                double costPerSeat = Double.parseDouble(schedule.get("cost").toString());
                int seats = Integer.parseInt(data.getValue().get("numberOfSeats").toString());
                return new SimpleDoubleProperty(costPerSeat * seats);
            } catch (Exception e) {
                System.err.println("Error calculating cost: " + e.getMessage());
                return new SimpleDoubleProperty(0.0);
            }
        });
        
        // Initialize status column
        statusColumn.setCellValueFactory(data -> {
            try {
                return new SimpleStringProperty(data.getValue().get("status").toString());
            } catch (Exception e) {
                System.err.println("Error getting status: " + e.getMessage());
                return new SimpleStringProperty("");
            }
        });
        
        // Setup action column with cancel button for active bookings
        actionColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Button>() {
            private final Button cancelButton = new Button("Cancel");
            {
                cancelButton.setStyle("-fx-background-color: #da9020; -fx-text-fill: white;");
                cancelButton.setOnAction(event -> {
                    Map<String, Object> booking = getTableView().getItems().get(getIndex());
                    
                    // Create confirmation alert
                    Optional<ButtonType> result = CustomAlert.showConfirmation(
                        "Confirm Cancellation",
                        "Are you sure you want to cancel this booking?"
                    );

                    if (result.isPresent() && result.get() == ButtonType.OK) {  
                        try {
                            // Add debug logging
                            System.out.println("Attempting to cancel booking with ID: " + booking.get("id"));
                            
                            String bookingId = booking.get("id").toString();
                            bookingService.deleteBooking(Integer.parseInt(bookingId));
                            
                            // Add success message and refresh
                            CustomAlert.showSuccess("Success", "Booking cancelled successfully");
                            loadBookings(); // Refresh the table
                            
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid booking ID format: " + e.getMessage());
                            CustomAlert.showError("Error", "Invalid booking ID format");
                        } catch (Exception e) {
                            System.err.println("Error cancelling booking: " + e.getMessage());
                            CustomAlert.showError("Error", "Failed to cancel booking: " + e.getMessage());
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Map<String, Object> booking = getTableRow().getItem();
                    String status = booking.get("status").toString();
                    if ("CONFIRMED".equals(status)) {
                        setGraphic(cancelButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadBookings() {
        try {
            Long userId = userService.getCurrentUserId();
            if (userId == null) {
                CustomAlert.showError("Error", "User not logged in");
                return;
            }

            List<Map<String, Object>> bookings = userService.getUserBookings();
            if (bookings != null && !bookings.isEmpty()) {
                // Debug: Print the first booking's data structure
                System.out.println("First booking data structure:");
                bookings.get(0).forEach((key, value) -> {
                    System.out.println(key + " = " + value);
                });
                
                bookingsTable.setItems(FXCollections.observableArrayList(bookings));
                System.out.println("Loaded " + bookings.size() + " bookings");
            } else {
                System.out.println("No bookings found or empty list returned");
                bookingsTable.setItems(FXCollections.observableArrayList());
                CustomAlert.showInfo("No Bookings", "You don't have any bookings yet.");
            }
        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showError("Error", "Failed to load bookings: " + e.getMessage());
        }
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
        setActiveButton(homeIcon, homeLabel);
        navigateToPage("Client/MainPage.fxml");
    }

    @FXML
    private void onSearchClicked() {
        setActiveButton(searchIcon, searchLabel);
        navigateToPage("Client/Search.fxml");
    }

    @FXML
    private void onProfileClicked() {
        setActiveButton(profileIcon, profileLabel);
        navigateToPage("Client/Profile.fxml");
    }

    @FXML
    private void onBookingsClicked() {
        setActiveButton(bookingsIcon, bookingsLabel);
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
