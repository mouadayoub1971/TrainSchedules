package com.mouad.frontend.Controllers.Client;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import com.mouad.frontend.Services.UserService;
import com.mouad.frontend.Services.BookingService;
import com.mouad.frontend.Views.ViewsFactory;
import java.io.IOException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mouad.frontend.Controllers.backend.BackendService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ViewReservation {
    @FXML
    private TableView<Reservation.Booking> bookingsTableView;

    @FXML
    private TableColumn<Reservation.Booking, String> trainNameColumn;

    @FXML
    private TableColumn<Reservation.Booking, String> departureColumn;

    @FXML
    private TableColumn<Reservation.Booking, String> destinationColumn;

    @FXML
    private TableColumn<Reservation.Booking, String> departureTimeColumn;

    @FXML
    private TableColumn<Reservation.Booking, String> numberOfSeatsColumn;

    @FXML
    private TableColumn<Reservation.Booking, String> bookingTimeColumn;

    @FXML
    private TableColumn<Reservation.Booking, String> statusColumn;

    @FXML
    private Label userNameLabel;

    private ObservableList<Reservation.Booking> bookingsList = FXCollections.observableArrayList();

    private final UserService userService;
    private final BookingService bookingService;
    private String firstName;
    private String email;
    public String Email() {
        return this.email;
    }
    public String FirstName() {
        return this.firstName;
    }

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
    public ViewReservation() {
        this.userService = UserService.getInstance();
        this.bookingService = BookingService.getInstance();
    }

    @FXML
    public void initialize() {
        System.out.println("Welcome, " + firstName + "!" + email);
        trainNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSchedule().getTrain().getTrainName()));

        departureColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSchedule().getDeparture()));

        destinationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSchedule().getDestination()));

        departureTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatDateTime(cellData.getValue().getSchedule().getDepartureTime())));

        numberOfSeatsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getNumberOfSeats())));

        bookingTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatDateTime(cellData.getValue().getBookingTime())));

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

        // Set the table's items
        bookingsTableView.setItems(bookingsList);
        System.out.println("before the fetch  " + firstName + "!" + email);
        // Fetch bookings
        System.out.println("Client dashboard initialized");
        if (firstName != null) {
            welcomeLabel.setText("Welcome, " + firstName + "!");
        }

        // Set initial active state
        setActiveMenu("bookings");
    }
    private void fetchUserBookings() {
        System.out.println("the email is " + firstName  + " " + email);
        try {
            // Fetch ALL users
            String usersJsonResponse = BackendService.fetchData("/users");
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse JSON directly as List of Maps
            List<Map<String, Object>> users = objectMapper.readValue(usersJsonResponse, new TypeReference<List<Map<String, Object>>>(){});

            // Extract ID where email matches firstName
            System.out.println("the email is " + this.firstName  + " " + this.email);
            Integer matchedUserId = users.stream()
                    .filter(user ->
                            user.get("email") != null &&
                                    user.get("email").equals(firstName)
                    )
                    .map(user -> (Integer) user.get("id"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No user found with email: " + firstName));


            System.out.println("the Id is  " + matchedUserId);

            // Fetch ALL bookings
            String bookingsJsonResponse = BackendService.fetchData("/bookings");

            System.out.println("the value of the json" + bookingsJsonResponse);

            // Configure ObjectMapper
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.registerModule(new JavaTimeModule());

            // Read all bookings
            List<Reservation.Booking> allBookings = objectMapper.readValue(bookingsJsonResponse, new TypeReference<List<Reservation.Booking>>() {});

            Long matchedUserIdAsLong = matchedUserId.longValue();
            // Filter bookings for matched user ID
            List<Reservation.Booking> userBookings = allBookings.stream()
                    .filter(booking -> booking.getUser() != null && booking.getUser().getId() != null)
                    .filter(booking -> booking.getUser().getId().equals(matchedUserIdAsLong))
                    .collect(Collectors.toList());

            System.out.println("the value of the list" + userBookings.toString());

            // Update UI on JavaFX Application Thread
            Platform.runLater(() -> {
                bookingsList.clear();
                bookingsList.addAll(userBookings);

//                // Update user name label if possible
//                if (!userBookings.isEmpty()) {
//                    userNameLabel.setText(userBookings.get(0).getUser().getFirstName() + " " +
//                            userBookings.get(0).getUser().getSecondName());
//                } else {
//                    // Handle case where no bookings are found
//                    userNameLabel.setText("No bookings found");
//                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            // Show error alert
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Booking Fetch Error");
                alert.setContentText("Unable to fetch bookings: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    // Helper method to format date and time
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // Inner classes to match the JSON structure
    public static class Booking {
        private Long id;
        private Reservation.User user;
        private Reservation.Schedule schedule;
        private int numberOfSeats;
        private LocalDateTime bookingTime;
        private String status;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Reservation.User getUser() { return user; }
        public void setUser(Reservation.User user) { this.user = user; }
        public Reservation.Schedule getSchedule() { return schedule; }
        public void setSchedule(Reservation.Schedule schedule) { this.schedule = schedule; }
        public int getNumberOfSeats() { return numberOfSeats; }
        public void setNumberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; }
        public LocalDateTime getBookingTime() { return bookingTime; }
        public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private Long id;
        private String firstName;
        private String secondName;
        private String email;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getSecondName() { return secondName; }
        public void setSecondName(String secondName) { this.secondName = secondName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class Schedule {
        private Long id;
        private Reservation.Train train;
        private String departure;
        private String destination;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private double cost;
        private boolean available;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Reservation.Train getTrain() { return train; }
        public void setTrain(Reservation.Train train) { this.train = train; }
        public String getDeparture() { return departure; }
        public void setDeparture(String departure) { this.departure = departure; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public LocalDateTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
        public LocalDateTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
        public double getCost() { return cost; }
        public void setCost(double cost) { this.cost = cost; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    public static class Train {
        private Long id;
        private String trainName;
        private String trainType;
        private int capacity;
        private String status;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { this.trainName = trainName; }
        public String getTrainType() { return trainType; }
        public void setTrainType(String trainType) { this.trainType = trainType; }
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @FXML
    private void onHomeClicked() {
        System.out.println("Home clicked");
        setActiveMenu("home");
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
            System.out.println("after the reservation " + reservation + " the email " + reservation);
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void onViewBookings() {
        System.out.println("View bookings clicked");
        setActiveMenu("bookings");
        // TODO: Load bookings content
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
        System.out.println("Setting user info - firstName: " + firstName + ", email: " + email);
        this.firstName = firstName;
        this.email = email;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + firstName + "!");
        }
        fetchUserBookings();
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
}