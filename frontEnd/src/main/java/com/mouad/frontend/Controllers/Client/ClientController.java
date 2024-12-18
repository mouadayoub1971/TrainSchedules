package com.mouad.frontend.Controllers.Client;

import com.mouad.frontend.Controllers.TrainSchedule;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import com.mouad.frontend.Services.UserService;
import com.mouad.frontend.Services.BookingService;
import com.mouad.frontend.Views.ViewsFactory;
import java.io.IOException;
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

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientController {
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
    private final UserService userService;
    private final BookingService bookingService;
    private String firstName;
    private String email;
    private Integer id;

    @FXML private Label welcomeLabel;
    @FXML private VBox contentArea;
    
    // Menu icons
    @FXML private ImageView homeIcon;
    @FXML private ImageView bookingsIcon;
    @FXML private ImageView logoutIcon;
    
    // Menu labels
    @FXML private Label homeLabel;
    @FXML private Label bookingsLabel;
    @FXML private Label logoutLabel;

    public ClientController() {
        this.userService = UserService.getInstance();
        this.bookingService = BookingService.getInstance();
    }

    @FXML
    public void initialize() {
        System.out.println("the name of the currrent useremail  " + firstName);
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

        System.out.println("Client dashboard initialized");
        if (firstName != null) {
            welcomeLabel.setText("Welcome,! " + firstName + "!!");
        }
        
        // Set initial active state
        setActiveMenu("home");
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
            Integer matchedUserId = null;
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
            try {
                String jsonResponse = BackendService.fetchData("/users");
                ObjectMapper objectMapper = new ObjectMapper();

// Parse JSON directly as List of Maps
                List<Map<String, Object>> users = objectMapper.readValue(jsonResponse, new TypeReference<List<Map<String, Object>>>(){});

// Extract ID where email matches firstName
                 matchedUserId = users.stream()
                        .filter(user ->
                                user.get("email") != null &&
                                        user.get("email").equals(firstName)
                        )
                        .map(user -> (Integer) user.get("id"))
                        .findFirst()
                        .orElse(null);
                System.out.println("the user id is where i fetched is " + matchedUserId);

            } catch (Exception e){
                System.out.println(e.toString());
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", matchedUserId); // Hardcoded user ID
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

    @FXML
    private void onHomeClicked() {
        System.out.println("Home clicked");
        setActiveMenu("home");
        System.out.println("View home clicked");
        try {
            System.out.println("Attempting to navigate to: " + "/Fxml/Client/Client.fxml");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/Client.fxml"));
            Parent root = loader.load();
            ClientController reservation = loader.getController();
            System.out.println("befor the reservation setUser info "+ firstName + "email" + email);


            reservation.setUserInfo(email, email);
            System.out.println("after the reservation " + reservation.firstName + " the email " + reservation.email);
            if (loader.getLocation() == null) {
                throw new IOException("Could not find FXML file: " + "/Fxml/Client/Client.fxml");
            }
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            System.out.println("the loader is " + loader.toString());
            System.out.println("the loader is " + loader.getController());

            stage.setTitle("Reservation Dashboard -___ " + firstName+ " " + email);
            Scene scene = new Scene(root);

            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error navigating to " + "/Fxml/Client/Client.fxml"+ ": " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load page: " + e.getMessage());
        }
    }

    @FXML
    private void onViewBookings() {
        System.out.println("View bookings clicked");
        setActiveMenu("bookings");
        try {
            System.out.println("Attempting to navigate to: " + "/Fxml/Client/ViewReservation.fxml");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/ViewReservation.fxml"));
            Parent root = loader.load();
            ViewReservation reservation = loader.getController();
            System.out.println("befor the reservation setUser info "+ firstName + "email" + email);


            reservation.setUserInfo(firstName, email);
            System.out.println("after the reservation " + reservation.FirstName() + " the email " + reservation.Email());
            if (loader.getLocation() == null) {
                throw new IOException("Could not find FXML file: " + "/Fxml/Client/ViewReservation.fxml");
            }
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            System.out.println("the loader is " + loader.toString());
            System.out.println("the loader is " + loader.getController());

            stage.setTitle("Reservation Dashboard -___ " + firstName+ " " + email);
            Scene scene = new Scene(root);

            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error navigating to " + "/Fxml/Client/ViewReservation.fxml"+ ": " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load page: " + e.getMessage());
        }
//        navigateToPage("/Fxml/Client/ViewReservation.fxml");
    }


    private void navigateToPage(String fxmlFile) {
        try {
            System.out.println("Attempting to navigate to: " + fxmlFile);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            ViewReservation reservation = loader.getController();
            System.out.println("befor the reservation setUser info "+ firstName + "email" + email);


            reservation.setUserInfo(firstName, email);
            System.out.println("after the reservation " + reservation.FirstName() + " the email " + reservation.Email());
            if (loader.getLocation() == null) {
                throw new IOException("Could not find FXML file: " + fxmlFile);
            }
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            System.out.println("the loader is " + loader.toString());
            System.out.println("the loader is " + loader.getController());

            stage.setTitle("Reservation Dashboard -___ " + firstName+ " " + email);
            Scene scene = new Scene(root);

            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error navigating to " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load page: " + e.getMessage());
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onLogout() {
        try {
            System.out.println("Logout clicked");
            // Close current window
            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            currentStage.close();
            
            // Show login window
            ViewsFactory.showWindow("Login.fxml", "Login");
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUserInfo(String firstName, String email) {
        System.out.println("Setting user info - firstName: " + firstName + ", email: " + email );
        this.firstName = firstName;
        this.email = email;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + firstName + "!!");
        }
    }

    private void setActiveMenu(String menu) {
        // Remove active class from all menu items
        homeIcon.getStyleClass().remove("active");
        bookingsIcon.getStyleClass().remove("active");
        homeLabel.getStyleClass().remove("active");
        bookingsLabel.getStyleClass().remove("active");

        // Add active class to selected menu
        switch (menu) {
            case "home":
                homeIcon.getStyleClass().add("active");
                homeLabel.getStyleClass().add("active");
                break;
            case "bookings":
                bookingsIcon.getStyleClass().add("active");
                bookingsLabel.getStyleClass().add("active");
                break;
        }
    }

    public void home(MouseEvent mouseEvent) {
    }
}
