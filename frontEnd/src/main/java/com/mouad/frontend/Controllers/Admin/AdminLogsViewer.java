package com.mouad.frontend.Controllers.Admin;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import com.mouad.frontend.Models.LogEntry;
import com.mouad.frontend.Controllers.KafkaLogConsumerService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AdminLogsViewer {
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

    @FXML
    public void initialize() {
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
    }

    public void shutdown() {
        // Method to be called when the application is closing
        if (kafkaConsumerService != null) {
            kafkaConsumerService.stopConsuming();
        }
    }
}
