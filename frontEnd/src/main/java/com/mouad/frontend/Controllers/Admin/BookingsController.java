package com.mouad.frontend.Controllers.Admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mouad.frontend.Services.AdminService;
import com.mouad.frontend.Services.BookingService;
import com.mouad.frontend.Services.CounterService;
import com.mouad.frontend.Components.CustomAlert;
import com.mouad.frontend.Views.ViewsFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class BookingsController {
    @FXML private ImageView homeIcon;
    @FXML private ImageView trainsIcon;
    @FXML private ImageView schedulesIcon;
    @FXML private ImageView bookingsIcon;
    @FXML private ImageView statisticsIcon;
    @FXML private ImageView logoutIcon;
    
    @FXML private TableView<JsonNode> bookingsTable;
    @FXML private TextField searchField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Button clearFiltersBtn;
    @FXML private TableColumn<JsonNode, String> scheduleColumn;
    @FXML private TableColumn<JsonNode, String> emailColumn;
    @FXML private TableColumn<JsonNode, String> dateColumn;
    @FXML private TableColumn<JsonNode, String> bookingTimeColumn;
    @FXML private TableColumn<JsonNode, String> seatsColumn;
    @FXML private TableColumn<JsonNode, String> statusColumn;
    @FXML private TableColumn<JsonNode, Void> actionsColumn;

    @FXML private ComboBox<String> statusFilter;

    @FXML
    private Label totalBookingsLabel;
    
    @FXML
    private Label activeBookingsLabel;
    
    @FXML
    private Label todayBookingsLabel;

    private static final String STATUS_ALL = "All";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELED = "CANCELED";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final AdminService adminService;
    private final BookingService bookingService;
    private ObservableList<JsonNode> bookings;

    public BookingsController() {
        this.adminService = AdminService.getInstance();
        this.bookingService = BookingService.getInstance();
        this.bookings = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        try {
            // Initialize status filter
            statusFilter.getItems().addAll(STATUS_ALL, STATUS_CONFIRMED, STATUS_CANCELED, STATUS_COMPLETED);
            statusFilter.setValue(STATUS_ALL);
            statusFilter.setStyle("-fx-background-color: #424344; -fx-text-fill: white;");
            
            // Style the search field and date pickers
            searchField.setStyle("-fx-background-color: #424344; -fx-text-fill: white; -fx-prompt-text-fill: #808080;");
            fromDatePicker.setStyle("-fx-background-color: #424344; -fx-text-fill: white;");
            toDatePicker.setStyle("-fx-background-color: #424344; -fx-text-fill: white;");
            
            setupNavigationHandlers();
            setupTableColumns();
            loadBookings();
            loadStatistics();
            setupFilters();
        } catch (Exception e) {
            System.err.println("Error initializing BookingsController: " + e.getMessage());
            CustomAlert.showInformation("Error", "Failed to initialize bookings page: " + e.getMessage());
        }
    }

    private void setupNavigationHandlers() {
        homeIcon.setOnMouseClicked(event -> onHomeClicked());
        trainsIcon.setOnMouseClicked(event -> onTrainsClicked());
        schedulesIcon.setOnMouseClicked(event -> onSchedulesClicked());
        bookingsIcon.setOnMouseClicked(event -> onBookingsClicked());
        statisticsIcon.setOnMouseClicked(event -> onStatisticsClicked());
        logoutIcon.setOnMouseClicked(event -> onLogoutClicked());
    }

    private void setupTableColumns() {
        setupRouteColumn();
        setupEmailColumn();
        setupDateColumns();
        setupSeatsColumn();
        setupStatusColumn();
        setupActionsColumn();
    }

    private void setupRouteColumn() {
        scheduleColumn.setCellValueFactory(data -> {
            String departure = data.getValue().get("schedule").get("departure").asText();
            String destination = data.getValue().get("schedule").get("destination").asText();
            return new SimpleStringProperty(departure + " → " + destination);
        });
        scheduleColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupEmailColumn() {
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("user").get("email").asText()));
        emailColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupDateColumns() {
        // Travel Date Column
        dateColumn.setCellValueFactory(data -> {
            String dateStr = data.getValue().get("schedule").get("departureTime").asText();
            try {
                LocalDateTime date = LocalDateTime.parse(dateStr);
                return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } catch (Exception e) {
                return new SimpleStringProperty("Invalid Date");
            }
        });
        dateColumn.setStyle("-fx-alignment: CENTER;");

        // Booking Time Column
        bookingTimeColumn.setCellValueFactory(data -> {
            String dateStr = data.getValue().get("bookingTime").asText();
            try {
                LocalDateTime date = LocalDateTime.parse(dateStr);
                return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } catch (Exception e) {
                return new SimpleStringProperty("Invalid Date");
            }
        });
        bookingTimeColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupSeatsColumn() {
        seatsColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get("numberOfSeats").asInt())));
        seatsColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupStatusColumn() {
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("status").asText()));
        statusColumn.setCellFactory(column -> new TableCell<JsonNode, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = getStatusColor(item);
                    setStyle(String.format("-fx-alignment: CENTER; -fx-text-fill: %s; -fx-font-weight: bold;", color));
                }
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<JsonNode, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonsBox = new HBox(20); // Increased spacing between buttons

            {
                // Style for edit button
                editButton.setStyle(
                    "-fx-background-color: #da9020;" + // Gold color
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-padding: 7 25;" +
                    "-fx-background-radius: 4;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-weight: bold;"
                );

                // Style for delete button
                deleteButton.setStyle(
                    "-fx-background-color: #242526;" + // Dark color
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-padding: 7 25;" +
                    "-fx-background-radius: 4;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-weight: bold;"
                );

                // Add hover effect for edit button
                editButton.setOnMouseEntered(e -> 
                    editButton.setStyle(
                        "-fx-background-color: #b67816;" + // Darker gold on hover
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 7 25;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
                    )
                );

                editButton.setOnMouseExited(e -> 
                    editButton.setStyle(
                        "-fx-background-color: #da9020;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 7 25;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
                    )
                );

                // Add hover effect for delete button
                deleteButton.setOnMouseEntered(e -> 
                    deleteButton.setStyle(
                        "-fx-background-color: #1a1b1c;" + // Darker black on hover
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 7 25;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
                    )
                );

                deleteButton.setOnMouseExited(e -> 
                    deleteButton.setStyle(
                        "-fx-background-color: #242526;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 7 25;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
                    )
                );

                buttonsBox.setAlignment(Pos.CENTER);
                buttonsBox.getChildren().addAll(editButton, deleteButton);

                editButton.setOnAction(event -> {
                    JsonNode booking = getTableView().getItems().get(getIndex());
                    onEditBooking(booking);
                });

                deleteButton.setOnAction(event -> {
                    JsonNode booking = getTableView().getItems().get(getIndex());
                    onDeleteBooking(booking);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void loadBookings() {
        try {
            JsonNode bookingsData = adminService.getAllBookings();
            bookings.clear();
            if (bookingsData != null && bookingsData.isArray()) {
                for (JsonNode booking : bookingsData) {
                    if (booking != null) {
                        bookings.add(booking);
                    }
                }
            }
            bookingsTable.setItems(bookings);
        } catch (IOException e) {
            System.err.println("Network error while loading bookings: " + e.getMessage());
            CustomAlert.showInformation("Network Error", "Could not connect to the server. Please check your connection and try again.");
        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
            CustomAlert.showInformation("Error", "An unexpected error occurred while loading bookings: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            // Get stats from services
            int totalBookings = bookingService.getTotalBookings();
            int activeBookings = bookingService.getActiveBookings();
            int todayBookings = bookingService.getTodayBookings();

            // Animate the counters
            CounterService.animateCounter(totalBookingsLabel, totalBookings, "Total Bookings");
            CounterService.animateCounter(activeBookingsLabel, activeBookings, "Active Bookings");
            CounterService.animateCounter(todayBookingsLabel, todayBookings, "Today's Bookings");
        } catch (Exception e) {
            System.err.println("Error loading booking statistics: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showInformation("Error", "Could not load booking statistics: " + e.getMessage());
        }
    }

    private void setupFilters() {
        // Setup date pickers
        fromDatePicker.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: white;");
        toDatePicker.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: white;");
        
        // Make the popup calendar text black for readability
        fromDatePicker.setOnShowing(e -> {
            fromDatePicker.getEditor().setStyle("-fx-text-fill: white;");
        });
        fromDatePicker.setOnHiding(e -> {
            fromDatePicker.getEditor().setStyle("-fx-text-fill: white;");
        });
        
        toDatePicker.setOnShowing(e -> {
            toDatePicker.getEditor().setStyle("-fx-text-fill: white;");
        });
        toDatePicker.setOnHiding(e -> {
            toDatePicker.getEditor().setStyle("-fx-text-fill: white;");
        });
        
        // Add listeners for filters
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        try {
            Predicate<JsonNode> datePredicate = booking -> {
                if (fromDatePicker.getValue() == null && toDatePicker.getValue() == null) {
                    return true;
                }
                JsonNode schedule = booking.get("schedule");
                if (schedule == null) return false;
                
                JsonNode departureTimeNode = schedule.get("departureTime");
                if (departureTimeNode == null) return false;
                
                String departureTime = departureTimeNode.asText();
                if (departureTime == null || departureTime.isEmpty() || !departureTime.contains("T")) {
                    return false;
                }
                
                LocalDateTime bookingDate = LocalDateTime.parse(departureTime);
                if (fromDatePicker.getValue() != null && bookingDate.toLocalDate().isBefore(fromDatePicker.getValue())) {
                    return false;
                }
                return toDatePicker.getValue() == null || !bookingDate.toLocalDate().isAfter(toDatePicker.getValue());
            };

            Predicate<JsonNode> searchPredicate = booking -> {
                if (searchField.getText() == null || searchField.getText().isEmpty()) {
                    return true;
                }
                String searchText = searchField.getText().toLowerCase();
                JsonNode user = booking.get("user");
                if (user == null) return false;
                
                JsonNode firstName = user.get("firstName");
                JsonNode lastName = user.get("lastName");
                JsonNode email = user.get("email");
                
                return (firstName != null && firstName.asText().toLowerCase().contains(searchText)) ||
                       (lastName != null && lastName.asText().toLowerCase().contains(searchText)) ||
                       (email != null && email.asText().toLowerCase().contains(searchText));
            };

            Predicate<JsonNode> statusPredicate = booking -> {
                if (STATUS_ALL.equals(statusFilter.getValue())) {
                    return true;
                }
                JsonNode statusNode = booking.get("status");
                return statusNode != null && statusNode.asText().equals(statusFilter.getValue());
            };

            ObservableList<JsonNode> filteredList = bookings.filtered(datePredicate.and(searchPredicate).and(statusPredicate));
            bookingsTable.setItems(filteredList);
        } catch (Exception e) {
            System.err.println("Error applying filters: " + e.getMessage());
            CustomAlert.showInformation("Error", "Error applying filters: " + e.getMessage());
        }
    }

    private void onDeleteBooking(JsonNode booking) {
        Optional<ButtonType> result = CustomAlert.showConfirmation(
            "Delete Booking",
            "Are you sure you want to delete this booking?"
        );
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                bookingService.deleteBooking(booking.get("id").asInt());
                loadBookings(); // Refresh the table
                CustomAlert.showSuccess("Success", "Booking deleted successfully");
            } catch (Exception e) {
                System.err.println("Error deleting booking: " + e.getMessage());
                CustomAlert.showError("Error", "Failed to delete booking: " + e.getMessage());
            }
        }
    }

    private void onEditBooking(JsonNode booking) {
        try {
            FXMLLoader loader = ViewsFactory.loadFXML("Admin/BookingEditDialog.fxml");
            Parent root = loader.load();
            
            BookingEditDialogController controller = loader.getController();
            controller.setBooking(booking);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Booking");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            
            dialogStage.showAndWait();
            
            // Refresh the table after dialog is closed
            loadBookings();
        } catch (Exception e) {
            System.err.println("Error showing edit dialog: " + e.getMessage());
            CustomAlert.showInformation("Error", "Could not open edit dialog: " + e.getMessage());
        }
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
            Stage currentStage = (Stage) bookingsTable.getScene().getWindow();
            currentStage.close();
            ViewsFactory.showWindow("Login.fxml", "Login");
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            CustomAlert.showInformation("Error", "Could not return to login page: " + e.getMessage());
        }
    }

    @FXML
    private void onClearFilters() {
        searchField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        statusFilter.setValue(STATUS_ALL);
        loadBookings();
    }

    private void navigateToPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/" + fxmlPath));
            Parent root = loader.load();
            Scene scene = homeIcon.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("Error loading page: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showInformation("Navigation Error", "Could not load the requested page: " + e.getMessage());
        }
    }

    private String getStatusColor(String status) {
        return switch (status.toUpperCase()) {
            case STATUS_CONFIRMED -> "green";
            case STATUS_CANCELED -> "orange";
            case STATUS_COMPLETED -> "blue";
            default -> "black";
        };
    }
}
