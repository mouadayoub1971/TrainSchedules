package com.mouad.frontend.Controllers.Admin;

import com.mouad.frontend.Controllers.KafkaLogConsumerService;
import com.mouad.frontend.Models.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import com.mouad.frontend.Models.*;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONObject;
import javafx.scene.control.cell.PropertyValueFactory;

import com.mouad.frontend.Views.ViewsFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.JSONObject;

public class StatisticsController {

    public Button switchButton;
    public VBox DashboardTableView;
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
    @FXML
    private TableView<LogEntry> logsTableView;

    @FXML
    private TableColumn<LogEntry, String> timestampColumn;
    @FXML
    private TableColumn<LogEntry, String> logLevelColumn;
    @FXML
    private TableColumn<LogEntry, String> threadColumn;
    @FXML
    private TableColumn<LogEntry, String> loggerNameColumn;
    @FXML
    private TableColumn<LogEntry, String> messageColumn;
    @FXML
    private TableColumn<LogEntry, String> contextColumn;

    private ObservableList<LogEntry> logEntries;
    private KafkaLogConsumerService kafkaConsumerService;
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

    // Kafka Consumer
    private KafkaConsumer<String, String> consumer;

    // Tracking maps with unique message keys
    private Map<StatisticsController.MessageKey, Integer> userBookingCounts = new HashMap<>();
    private Map<StatisticsController.MessageKey, Integer> routeCounts = new HashMap<>();
    private Map<StatisticsController.MessageKey, Integer> durationCounts = new HashMap<>();
    private Map<StatisticsController.MessageKey, Integer> destinationCounts = new HashMap<>();
    private Map<StatisticsController.MessageKey, Integer> departureCounts = new HashMap<>();

    // Inner class for creating unique message keys
    private static class MessageKey {
        private final String loggerName;
        private final String message;

        public MessageKey(String loggerName, String message) {
            this.loggerName = loggerName;
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StatisticsController.MessageKey messageKey = (StatisticsController.MessageKey) o;
            return loggerName.equals(messageKey.loggerName) && message.equals(messageKey.message);
        }

        @Override
        public int hashCode() {
            int result = loggerName.hashCode();
            result = 31 * result + message.hashCode();
            return result;
        }
    }

    @FXML
    public void initialize() {
        try {
            // Setup table columns
            setupTableColumns();

            // Setup Kafka consumer
            setupKafkaConsumer();

            // Start Kafka message consumption
            startKafkaConsumption();
            // Initialize the ObservableList
            logEntries = FXCollections.observableArrayList();

            // Bind the columns to the LogEntry properties
            timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());
            logLevelColumn.setCellValueFactory(cellData -> cellData.getValue().logLevelProperty());
            threadColumn.setCellValueFactory(cellData -> cellData.getValue().threadNameProperty());
            loggerNameColumn.setCellValueFactory(cellData -> cellData.getValue().loggerNameProperty());
            messageColumn.setCellValueFactory(cellData -> cellData.getValue().messageProperty());
            contextColumn.setCellValueFactory(cellData -> cellData.getValue().contextDataProperty());

            // Set the items to the table
            logsTableView.setItems(logEntries);

            // Initialize Kafka Consumer Service
            kafkaConsumerService = new KafkaLogConsumerService(logEntries, "logsTopic");
            kafkaConsumerService.startConsuming();
            // Set up click handlers for both icons and labels
            homeIcon.setOnMouseClicked(event -> onHomeClicked());
            trainsIcon.setOnMouseClicked(event -> onTrainsClicked());
            schedulesIcon.setOnMouseClicked(event -> onSchedulesClicked());
            bookingsIcon.setOnMouseClicked(event -> onBookingsClicked());
            statisticsIcon.setOnMouseClicked(event -> onStatisticsClicked());
            logoutIcon.setOnMouseClicked(event -> onLogoutClicked());
            
            // Set up click handlers for labels
            trainsLabel.setOnMouseClicked(event -> onTrainsClicked());
            schedulesLabel.setOnMouseClicked(event -> onSchedulesClicked());
            bookingsLabel.setOnMouseClicked(event -> onBookingsClicked());
            statisticsLabel.setOnMouseClicked(event -> onStatisticsClicked());

            DashboardTableView.setVisible(false);
            DashboardTableView.setManaged(false);
            logsTableView.setVisible(true);
            logsTableView.setManaged(true);
            switchButton.setOnAction(this::toggleVisibility);
        } catch (Exception e) {
            System.err.println("Error initializing StatisticsController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void toggleVisibility(ActionEvent event) {
        // Toggle visibility of dashboard table and logs table
        boolean isDashboardVisible = DashboardTableView.isVisible();

        DashboardTableView.setVisible(!isDashboardVisible);
        DashboardTableView.setManaged(!isDashboardVisible);

        logsTableView.setVisible(isDashboardVisible);
        logsTableView.setManaged(isDashboardVisible);

        // Update button text (optional)
        switchButton.setText(isDashboardVisible ? "Show Logs" : "Show Dashboard");
    }

    // Setup table column value factories
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

    // Configure Kafka consumer
//    private void setupKafkaConsumer() {
//        Properties props = new Properties();
//        props.put("bootstrap.servers", "localhost:9092");
//        props.put("group.id", "firstGroup");
//        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put("value.deserializer", "org.apache.kafk  a.common.serialization.StringDeserializer");
//
//
//        // Set to read from the earliest available message
//        props.put("auto.offset.reset", "earliest");
//
//
//
//        consumer = new KafkaConsumer<>(props);
//        consumer.subscribe(java.util.Collections.singletonList("spark-out-put"));
//    }

    // Start Kafka message consumption in a separate thread
//    private void startKafkaConsumption() {
//        Thread kafkaThread = new Thread(() -> {
//            try {
//                while (!Thread.currentThread().isInterrupted()) {
//                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
//
//                    // Process records
//                    for (ConsumerRecord<String, String> record : records) {
//                        processRecord(record.value());
//                    }
//
//                    // Update UI on JavaFX Application Thread
//                    Platform.runLater(this::updateTables);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (consumer != null) {
//                    consumer.close();
//                }
//            }
//        });
//        kafkaThread.setDaemon(true);
//        kafkaThread.start();
//    }

    private void setupKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "firstGroup");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // Critical settings
        props.put("auto.offset.reset", "earliest");  // Always start from the beginning
        props.put("enable.auto.commit", "false");    // Disable auto offset commit

        // Optional: Add unique group.id to reset consumer group
        props.put("group.id", "unique-group-" + System.currentTimeMillis());

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(java.util.Collections.singletonList("spark-out-put"));
    }

//    private void startKafkaConsumption() {
//        Thread kafkaThread = new Thread(() -> {
//            try {
//                while (!Thread.currentThread().isInterrupted()) {
//                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
//
//                    if (!records.isEmpty()) {
//                        // Clear existing data before processing new records
//                        userBookingCounts.clear();
//                        routeCounts.clear();
//                        durationCounts.clear();
//                        destinationCounts.clear();
//                        departureCounts.clear();
//
//                        // Process records
//                        for (ConsumerRecord<String, String> record : records) {
//                            processRecord(record.value());
//                        }
//
//                        // Update UI on JavaFX Application Thread
//                        Platform.runLater(this::updateTables);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (consumer != null) {
//                    consumer.close();
//                }
//            }
//        });
//        kafkaThread.setDaemon(true);
//        kafkaThread.start();
//    }

    private void startKafkaConsumption() {
        Thread kafkaThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    if (!records.isEmpty()) {
                        // Process records without clearing existing data
                        for (ConsumerRecord<String, String> record : records) {
                            processRecord(record.value());
                        }

                        // Update UI on JavaFX Application Thread
                        Platform.runLater(this::updateTables);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (consumer != null) {
                    consumer.close();
                }
            }
        });
        kafkaThread.setDaemon(true);
        kafkaThread.start();
    }

    // Process individual Kafka message
//    private void processRecord(String message) {
//        try {
//            JSONObject jsonMessage = new JSONObject(message);
//
//            // Extract logger name and message
//            String loggerName = jsonMessage.getString("loggerName");
//            String messageContent = jsonMessage.getString("message");
//            List<String> contextData = jsonMessage.getJSONArray("allContextData")
//                    .toList()
//                    .stream()
//                    .map(Object::toString)
//                    .collect(Collectors.toList());
//
//            // Ignore messages with "User Info" or "Schedule Info" context
//            if (loggerName.equals("BookingController") &&
//                    (contextData.contains("User Info") || contextData.contains("Schedule Info"))) {
//                return;  // Skip processing these messages
//            }
//
//            // Existing processing logic for other messages
//            if (loggerName.equals("BookingController")) {
//                MessageKey key;
//                if (messageContent.contains("user email")) {
//                    String userEmail = extractUserEmail(messageContent);
//                    key = new MessageKey(loggerName, userEmail);
//                    userBookingCounts.put(key, jsonMessage.getInt("logCount"));
//                }
//
//                if (messageContent.contains("trip:")) {
//                    String route = extractRoute(messageContent);
//                    key = new MessageKey(loggerName, route);
//                    routeCounts.put(key, jsonMessage.getInt("logCount"));
//                }
//
//                if (messageContent.contains("timing:")) {
//                    String duration = extractDuration(messageContent);
//                    key = new MessageKey(loggerName, duration);
//                    durationCounts.put(key, jsonMessage.getInt("logCount"));
//                }
//
//                if (messageContent.contains("arrive:")) {
//                    String destination = extractDestination(messageContent, "arrive:");
//                    key = new MessageKey(loggerName, destination);
//                    destinationCounts.put(key, jsonMessage.getInt("logCount"));
//                }
//
//                if (messageContent.contains("depart:")) {
//                    String departure = extractDestination(messageContent, "depart:");
//                    key = new MessageKey(loggerName, departure);
//                    departureCounts.put(key, jsonMessage.getInt("logCount"));
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    private void processRecord(String message) {
        try {
            JSONObject jsonMessage = new JSONObject(message);

            // Extract logger name and message
            String loggerName = jsonMessage.getString("loggerName");
            String messageContent = jsonMessage.getString("message");
            List<String> contextData = jsonMessage.getJSONArray("allContextData")
                    .toList()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

            // Ignore messages with "User Info" or "Schedule Info" context
            if (loggerName.equals("BookingController") &&
                    (contextData.contains("User Info") || contextData.contains("Schedule Info"))) {
                return;  // Skip processing these messages
            }

            // Existing processing logic for other messages
            if (loggerName.equals("BookingController")) {
                StatisticsController.MessageKey key;
                int logCount = jsonMessage.getInt("logCount");

                if (messageContent.contains("user email")) {
                    String userEmail = extractUserEmail(messageContent);
                    key = new StatisticsController.MessageKey(loggerName, userEmail);
                    // Update count if key exists, otherwise put new entry
                    userBookingCounts.put(key, logCount);
                }

                if (messageContent.contains("trip:")) {
                    String route = extractRoute(messageContent);
                    key = new StatisticsController.MessageKey(loggerName, route);
                    routeCounts.put(key, logCount);
                }

                if (messageContent.contains("timing:")) {
                    String duration = extractDuration(messageContent);
                    key = new StatisticsController.MessageKey(loggerName, duration);
                    durationCounts.put(key, logCount);
                }

                if (messageContent.contains("arrive:")) {
                    String destination = extractDestination(messageContent, "arrive:");
                    key = new StatisticsController.MessageKey(loggerName, destination);
                    destinationCounts.put(key, logCount);
                }

                if (messageContent.contains("depart:")) {
                    String departure = extractDestination(messageContent, "depart:");
                    key = new StatisticsController.MessageKey(loggerName, departure);
                    departureCounts.put(key, logCount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update tables with the latest data
    private void updateTables() {
        // Most Booking Users
        mostBookingUsersTable.setItems(FXCollections.observableArrayList(
                userBookingCounts.entrySet().stream()
                        .map(entry -> new UserBooking(entry.getKey().message, entry.getValue()))
                        .sorted((a, b) -> b.getBookingCount() - a.getBookingCount())
                        .collect(Collectors.toList())
        ));

        // Most Reserved Routes
        mostReservedRoutesTable.setItems(FXCollections.observableArrayList(
                routeCounts.entrySet().stream()
                        .map(entry -> new RouteMetrics(entry.getKey().message, entry.getValue()))
                        .sorted((a, b) -> b.getCount() - a.getCount())
                        .collect(Collectors.toList())
        ));

        // Most Reserved Durations
        mostReservedDurationsTable.setItems(FXCollections.observableArrayList(
                durationCounts.entrySet().stream()
                        .map(entry -> new DurationMetrics(entry.getKey().message, entry.getValue()))
                        .sorted((a, b) -> b.getCount() - a.getCount())
                        .collect(Collectors.toList())
        ));

        // Popular Destinations
        popularDestinationsTable.setItems(FXCollections.observableArrayList(
                destinationCounts.entrySet().stream()
                        .map(entry -> new DestinationMetrics(entry.getKey().message, entry.getValue()))
                        .sorted((a, b) -> b.getCount() - a.getCount())
                        .collect(Collectors.toList())
        ));

        // Popular Departures
        popularDeparturesTable.setItems(FXCollections.observableArrayList(
                departureCounts.entrySet().stream()
                        .map(entry -> new DepartureMetrics(entry.getKey().message, entry.getValue()))
                        .sorted((a, b) -> b.getCount() - a.getCount())
                        .collect(Collectors.toList())
        ));
    }

    // Extraction methods
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
            // Close current window
            Stage currentStage = (Stage) logoutIcon.getScene().getWindow();
            currentStage.close();
            
            // Show login window
            ViewsFactory.showWindow("Login.fxml", "Login");
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
            showAlert("Logout Error", "Could not return to login page: " + e.getMessage());
        }
    }

    private void navigateToPage(String fxmlFile) {
        try {
            System.out.println("Attempting to navigate to: " + fxmlFile);
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/" + fxmlFile));
            if (loader.getLocation() == null) {
                throw new IOException("Could not find FXML file: " + fxmlFile);
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            URL cssResource = getClass().getResource("/styles/MainPage.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                System.err.println("Warning: Could not load CSS file");
            }
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
}
