package com.mouad.frontend.Controllers.Client;


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

public class Reservation {
    @FXML
    private TableView<Booking> bookingsTableView;

    @FXML
    private TableColumn<Booking, String> trainNameColumn;

    @FXML
    private TableColumn<Booking, String> departureColumn;

    @FXML
    private TableColumn<Booking, String> destinationColumn;

    @FXML
    private TableColumn<Booking, String> departureTimeColumn;

    @FXML
    private TableColumn<Booking, String> numberOfSeatsColumn;

    @FXML
    private TableColumn<Booking, String> bookingTimeColumn;

    @FXML
    private TableColumn<Booking, String> statusColumn;

    @FXML
    private Label userNameLabel;
    private String firstName;
    private String email;

    private ObservableList<Booking> bookingsList = FXCollections.observableArrayList();
    public void setUserInfo(String firstName, String email) {
        System.out.println("Setting user info - firstName: " + firstName + ", email: " + email );
        this.firstName = firstName;
        this.email = email;

    }

    @FXML
    public void initialize() {
        // Setup table columns
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

        // Fetch bookings
        fetchUserBookings();
    }

    private void fetchUserBookings() {
        try {
            // Fetch ALL users
            String usersJsonResponse = BackendService.fetchData("/users");
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse JSON directly as List of Maps
            List<Map<String, Object>> users = objectMapper.readValue(usersJsonResponse, new TypeReference<List<Map<String, Object>>>(){});

            // Extract ID where email matches firstName
            Integer matchedUserId = users.stream()
                    .filter(user ->
                            user.get("email") != null &&
                                    user.get("email").equals(firstName)
                    )
                    .map(user -> (Integer) user.get("id"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No user found with email: " + firstName));

            // Fetch ALL bookings
            String bookingsJsonResponse = BackendService.fetchData("/bookings");

            // Configure ObjectMapper
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.registerModule(new JavaTimeModule());

            // Read all bookings
            List<Booking> allBookings = objectMapper.readValue(bookingsJsonResponse, new TypeReference<List<Booking>>() {});

            // Filter bookings for matched user ID
            List<Booking> userBookings = allBookings.stream()
                    .filter(booking -> booking.getUser().getId().equals(matchedUserId))
                    .collect(Collectors.toList());

            // Update UI on JavaFX Application Thread
            Platform.runLater(() -> {
                bookingsList.clear();
                bookingsList.addAll(userBookings);

                // Update user name label if possible
                if (!userBookings.isEmpty()) {
                    userNameLabel.setText(userBookings.get(0).getUser().getFirstName() + " " +
                            userBookings.get(0).getUser().getSecondName());
                } else {
                    // Handle case where no bookings are found
                    userNameLabel.setText("No bookings found");
                }
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
        private User user;
        private Schedule schedule;
        private int numberOfSeats;
        private LocalDateTime bookingTime;
        private String status;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
        public Schedule getSchedule() { return schedule; }
        public void setSchedule(Schedule schedule) { this.schedule = schedule; }
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
        private Train train;
        private String departure;
        private String destination;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private double cost;
        private boolean available;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Train getTrain() { return train; }
        public void setTrain(Train train) { this.train = train; }
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
}
