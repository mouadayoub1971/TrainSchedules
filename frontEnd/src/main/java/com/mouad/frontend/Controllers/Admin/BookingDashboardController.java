package com.mouad.frontend.Controllers.Admin;


import com.mouad.frontend.Models.*;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import java.time.Duration;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BookingDashboardController {
    @FXML private TableView<UserBooking> mostBookingUsersTable;
    @FXML private TableColumn<UserBooking, String> userEmailColumn;
    @FXML private TableColumn<UserBooking, Integer> bookingCountColumn;

    @FXML private TableView<RouteMetrics> mostReservedRoutesTable;
    @FXML private TableColumn<RouteMetrics, String> routeColumn;
    @FXML private TableColumn<RouteMetrics, Integer> routeCountColumn;

    @FXML private TableView<DurationMetrics> mostReservedDurationsTable;
    @FXML private TableColumn<DurationMetrics, String> durationColumn;
    @FXML private TableColumn<DurationMetrics, Integer> durationCountColumn;

    @FXML private TableView<DestinationMetrics> popularDestinationsTable;
    @FXML private TableColumn<DestinationMetrics, String> destinationColumn;
    @FXML private TableColumn<DestinationMetrics, Integer> destinationCountColumn;

    @FXML private TableView<DepartureMetrics> popularDeparturesTable;
    @FXML private TableColumn<DepartureMetrics, String> departureColumn;
    @FXML private TableColumn<DepartureMetrics, Integer> departureCountColumn;

    private KafkaConsumer<String, String> consumer;
    private Map<String, Integer> userBookingCounts = new HashMap<>();
    private Map<String, Integer> routeCounts = new HashMap<>();
    private Map<String, Integer> durationCounts = new HashMap<>();
    private Map<String, Integer> destinationCounts = new HashMap<>();
    private Map<String, Integer> departureCounts = new HashMap<>();

    @FXML
    public void initialize() {
        setupKafkaConsumer();
        setupTableColumns();
        startKafkaConsumption();
    }

    private void setupTableColumns() {
        // User Bookings
        userEmailColumn.setCellValueFactory(cellData -> cellData.getValue().userEmailProperty());
        bookingCountColumn.setCellValueFactory(cellData -> cellData.getValue().bookingCountProperty().asObject());

        // Routes
        routeColumn.setCellValueFactory(cellData -> cellData.getValue().routeProperty());
        routeCountColumn.setCellValueFactory(cellData -> cellData.getValue().countProperty().asObject());

        // Durations
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
        durationCountColumn.setCellValueFactory(cellData -> cellData.getValue().countProperty().asObject());

        // Destinations
        destinationColumn.setCellValueFactory(cellData -> cellData.getValue().destinationProperty());
        destinationCountColumn.setCellValueFactory(cellData -> cellData.getValue().countProperty().asObject());

        // Departures
        departureColumn.setCellValueFactory(cellData -> cellData.getValue().departureProperty());
        departureCountColumn.setCellValueFactory(cellData -> cellData.getValue().countProperty().asObject());
    }

    private void setupKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "firstGroup");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(java.util.Collections.singletonList("spark-out-put"));
    }

    private void startKafkaConsumption() {
        new Thread(() -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record.value());
                }
                updateTables();
            }
        }).start();
    }

    private void processRecord(String message) {
        if (message.contains("BookingController")) {
            int logCount = extractLogCount(message);
            if (message.contains("user email")) {
                String userEmail = extractUserEmail(message);
                userBookingCounts.put(userEmail, userBookingCounts.getOrDefault(userEmail, 0) + logCount);
            }

            if (message.contains("trip:")) {
                String route = extractRoute(message);
                routeCounts.put(route, routeCounts.getOrDefault(route, 0) + logCount);
            }

            if (message.contains("timing:")) {
                String duration = extractDuration(message);
                durationCounts.put(duration, durationCounts.getOrDefault(duration, 0) + logCount);
            }

            if (message.contains("arrive:")) {
                String destination = extractDestination(message, "arrive:");
                destinationCounts.put(destination, destinationCounts.getOrDefault(destination, 0) + logCount);
            }

            if (message.contains("depart:")) {
                String departure = extractDestination(message, "depart:");
                departureCounts.put(departure, departureCounts.getOrDefault(departure, 0) + logCount);
            }
        }
    }

    private void updateTables() {
        // Most Booking Users
        mostBookingUsersTable.setItems(FXCollections.observableArrayList(
                userBookingCounts.entrySet().stream()
                        .map(entry -> new UserBooking(entry.getKey(), entry.getValue()))
                        .sorted((a, b) -> b.getBookingCount() - a.getBookingCount())
                        .collect(Collectors.toList())
        ));

        // Most Reserved Routes
        mostReservedRoutesTable.setItems(FXCollections.observableArrayList(
                routeCounts.entrySet().stream()
                        .map(entry -> new RouteMetrics(entry.getKey(), entry.getValue()))
                        .sorted((a, b) -> b.getCount() - a.getCount())
                        .collect(Collectors.toList())
        ));

        // Most Reserved Durations
        mostReservedDurationsTable.setItems(FXCollections.observableArrayList(
                durationCounts.entrySet().stream()
                        .map(entry -> new DurationMetrics(entry.getKey(), entry.getValue()))
                        .sorted((a, b) -> b.getCount() - a.getCount())
                        .collect(Collectors.toList())
        ));

        // Popular Destinations
        popularDestinationsTable.setItems(FXCollections.observableArrayList(
                destinationCounts.entrySet().stream()
                        .map(entry -> new DestinationMetrics(entry.getKey(), entry.getValue()))
                        .sorted((a, b) -> b.getCount() - a.getCount())
                        .collect(Collectors.toList())
        ));

        // Popular Departures
        popularDeparturesTable.setItems(FXCollections.observableArrayList(
                departureCounts.entrySet().stream()
                        .map(entry -> new DepartureMetrics(entry.getKey(), entry.getValue()))
                        .sorted((a, b) -> b.getCount() - a.getCount())
                        .collect(Collectors.toList())
        ));
    }
    private int extractLogCount(String message) {
        int startIndex = message.indexOf("\"logCount\":") + 11;
        int endIndex = message.indexOf("}", startIndex);
        return Integer.parseInt(message.substring(startIndex, endIndex).trim());
    }

    // Existing extraction methods remain the same
    private String extractUserEmail(String message) {
        int startIndex = message.indexOf("email") + 6;
        int endIndex = message.indexOf(" ", startIndex);
        return message.substring(startIndex, endIndex != -1 ? endIndex : message.length());
    }

    private String extractRoute(String message) {
        int startIndex = message.indexOf("trip:") + 5;
        return message.substring(startIndex).trim();
    }

    private String extractDuration(String message) {
        int startIndex = message.indexOf("timing:") + 7;
        return message.substring(startIndex).trim();
    }

    private String extractDestination(String message, String prefix) {
        int startIndex = message.indexOf(prefix) + prefix.length();
        return message.substring(startIndex).trim();
    }

}