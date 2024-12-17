package com.mouad.frontend.Controllers.Admin;

import com.mouad.frontend.Controllers.KafkaLogConsumerService;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.net.URL;
import com.mouad.frontend.Views.ViewsFactory;

import com.mouad.frontend.Models.LogEntry;

public class GStatisticsController {

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
    @FXML private Button showStatisticsButton;
    
    @FXML private TableView<LogEntry> logsTableView;
    @FXML private TableColumn<LogEntry, String> timestampColumn;
    @FXML private TableColumn<LogEntry, String> logLevelColumn;
    @FXML private TableColumn<LogEntry, String> threadColumn;
    @FXML private TableColumn<LogEntry, String> loggerNameColumn;
    @FXML private TableColumn<LogEntry, String> messageColumn;
    @FXML private TableColumn<LogEntry, String> contextColumn;

    @FXML
    private Label totalLogsLabel;
    @FXML
    private Label infoLogsLabel;
    @FXML
    private Label errorLogsLabel;
    @FXML
    private Label errorRateLabel;

    @FXML
    private Button showDetailsButton;

    private ObservableList<LogEntry> logEntries;
    private KafkaLogConsumerService kafkaConsumerService;

    private IntegerProperty totalLogs = new SimpleIntegerProperty(0);
    private IntegerProperty infoLogs = new SimpleIntegerProperty(0);
    private IntegerProperty errorLogs = new SimpleIntegerProperty(0);
    private DoubleProperty errorRate = new SimpleDoubleProperty(0.0);

    @FXML
    public void initialize() {
        try {
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

            // Initialize logs table
            initializeLogsTable();
            
            // Initialize log metrics
            initializeLogMetrics();
            
        } catch (Exception e) {
            System.err.println("Error initializing StatisticsController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeLogsTable() {
        // Initialize the ObservableList
        logEntries = FXCollections.observableArrayList();

        // Bind the columns to the LogEntry properties
        timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());
        logLevelColumn.setCellValueFactory(cellData -> cellData.getValue().logLevelProperty());
        threadColumn.setCellValueFactory(cellData -> cellData.getValue().threadNameProperty());
        loggerNameColumn.setCellValueFactory(cellData -> cellData.getValue().loggerNameProperty());
        messageColumn.setCellValueFactory(cellData -> cellData.getValue().messageProperty());
        contextColumn.setCellValueFactory(cellData -> cellData.getValue().contextDataProperty());

        // Configure column resize policies
        timestampColumn.setMinWidth(150);
        logLevelColumn.setMinWidth(80);
        threadColumn.setMinWidth(100);
        loggerNameColumn.setMinWidth(150);
        messageColumn.setMinWidth(300);
        contextColumn.setMinWidth(150);

        // Make message column take remaining space
        messageColumn.prefWidthProperty().bind(
            logsTableView.widthProperty()
                .subtract(timestampColumn.widthProperty())
                .subtract(logLevelColumn.widthProperty())
                .subtract(threadColumn.widthProperty())
                .subtract(loggerNameColumn.widthProperty())
                .subtract(contextColumn.widthProperty())
                .subtract(2) // Account for borders
        );

        // Set table to automatically resize columns
        logsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Set the items to the table
        logsTableView.setItems(logEntries);

        // Initialize Kafka Consumer Service
        kafkaConsumerService = new KafkaLogConsumerService(logEntries, "logsTopic");
        kafkaConsumerService.startConsuming();
    }

    private void initializeLogMetrics() {
        // Bind the labels to the properties
        totalLogsLabel.textProperty().bind(totalLogs.asString());
        infoLogsLabel.textProperty().bind(infoLogs.asString());
        errorLogsLabel.textProperty().bind(errorLogs.asString());
        errorRateLabel.textProperty().bind(Bindings.createStringBinding(
            () -> String.format("%.1f%%", errorRate.get()),
            errorRate
        ));

        // Update metrics when logs are added
        logsTableView.getItems().addListener((ListChangeListener<LogEntry>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    updateMetrics();
                }
            }
        });
    }

    private void animateCounter(IntegerProperty property, int targetValue) {
        int startValue = property.get();
        int duration = 1000; // Animation duration in milliseconds
        int steps = 30; // Number of steps in the animation
        int stepDuration = duration / steps;
        int increment = (targetValue - startValue) / steps;

        new Thread(() -> {
            for (int i = 0; i < steps; i++) {
                try {
                    Thread.sleep(stepDuration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                final int value = startValue + increment * i;
                javafx.application.Platform.runLater(() -> property.set(value));
            }
            javafx.application.Platform.runLater(() -> property.set(targetValue));
        }).start();
    }

    private void updateMetrics() {
        ObservableList<LogEntry> logs = logsTableView.getItems();
        int totalSize = logs.size();
        
        long infoCount = logs.stream()
            .filter(log -> "INFO".equals(log.getLogLevel()))
            .count();

        long errorCount = logs.stream()
            .filter(log -> "ERROR".equals(log.getLogLevel()) || "WARNING".equals(log.getLogLevel()))
            .count();

        // Calculate error rate first
        if (totalSize > 0) {
            errorRate.set((errorCount * 100.0) / totalSize);
        } else {
            errorRate.set(0.0);
        }

        // Then animate the counters
        animateCounter(totalLogs, totalSize);
        animateCounter(infoLogs, (int) infoCount);
        animateCounter(errorLogs, (int) errorCount);
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

    @FXML
    private void showBookingDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/BookingDetailsPopup.fxml"));
            AnchorPane popupContent = loader.load();
            
            Stage popupStage = new Stage();
            popupStage.initStyle(StageStyle.TRANSPARENT);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(popupContent);
            scene.setFill(null);
            
            // Add CSS
            URL cssResource = getClass().getResource("/styles/BookingDetailsStyle.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }
            
            popupStage.setScene(scene);
            
            // Get the primary screen's bounds
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            
            // Calculate the center position
            double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - popupContent.getPrefWidth()) / 2;
            double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - popupContent.getPrefHeight()) / 2;
            
            // Set the position
            popupStage.setX(centerX);
            popupStage.setY(centerY);
            
            // Show the popup
            popupStage.show();
            
        } catch (IOException e) {
            System.err.println("Error showing booking details: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not show booking details: " + e.getMessage());
        }
    }

    @FXML
    private void showStatisticsDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/StatisticsDetailsPage.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Statistics Details");
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Center the window on the screen
            stage.centerOnScreen();
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load statistics details");
            alert.setContentText("An error occurred while loading the statistics details window.");
            alert.showAndWait();
        }
    }

    public void shutdown() {
        if (kafkaConsumerService != null) {
            kafkaConsumerService.stopConsuming();
        }
    }
}
