package com.mouad.frontend.Controllers.Admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.mouad.frontend.Views.ViewsFactory;
import com.mouad.frontend.Services.TrainService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Callback;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.net.URL;
import com.mouad.frontend.Components.CustomAlert;
import java.util.Optional;
import javafx.scene.control.ButtonType;

public class TrainsController {
    private final TrainService trainService;
    private final ObjectMapper objectMapper;
    private ObservableList<JsonNode> trains;
    private JsonNode selectedTrain;

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

    // Train Management UI Components
    @FXML private TableView<JsonNode> trainsTable;
    @FXML private TableColumn<JsonNode, Integer> idColumn;
    @FXML private TableColumn<JsonNode, String> nameColumn;
    @FXML private TableColumn<JsonNode, String> typeColumn;
    @FXML private TableColumn<JsonNode, Integer> capacityColumn;
    @FXML private TableColumn<JsonNode, String> statusColumn;
    @FXML private TableColumn<JsonNode, Void> actionsColumn;

    @FXML private VBox trainFormContainer;
    @FXML private Label formTitle;
    @FXML private TextField trainNameField;
    @FXML private ComboBox<String> trainTypeCombo;
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    public TrainsController() {
        this.trainService = TrainService.getInstance();
        this.objectMapper = new ObjectMapper();
        this.trains = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadTrains();
        hideForm();
        
        // Set up navigation click handlers
        homeIcon.setOnMouseClicked(event -> onHomeClicked());
        trainsIcon.setOnMouseClicked(event -> onTrainsClicked());
        schedulesIcon.setOnMouseClicked(event -> onSchedulesClicked());
        bookingsIcon.setOnMouseClicked(event -> onBookingsClicked());
        statisticsIcon.setOnMouseClicked(event -> onStatisticsClicked());
        logoutIcon.setOnMouseClicked(event -> onLogoutClicked());
        
        trainsLabel.setOnMouseClicked(event -> onTrainsClicked());
        schedulesLabel.setOnMouseClicked(event -> onSchedulesClicked());
        bookingsLabel.setOnMouseClicked(event -> onBookingsClicked());
        statisticsLabel.setOnMouseClicked(event -> onStatisticsClicked());
    }

    private void setupTable() {
        // Setup cell value factories to work with JsonNode
        idColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().get("id").asInt()).asObject());
        
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("trainName").asText()));
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("trainType").asText()));
        
        capacityColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().get("capacity").asInt()).asObject());
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("status").asText()));
        
        setupActionsColumn();
        
        trainsTable.setItems(trains);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(10, editBtn, deleteBtn); // Increased spacing to 10

            {
                // Apply the new styles
                editBtn.getStyleClass().addAll("table-action-button", "edit");
                deleteBtn.getStyleClass().addAll("table-action-button", "delete");
                container.setAlignment(Pos.CENTER);
                
                editBtn.setOnAction(event -> {
                    JsonNode train = getTableView().getItems().get(getIndex());
                    showEditForm(train);
                });
                
                deleteBtn.setOnAction(event -> {
                    JsonNode train = getTableView().getItems().get(getIndex());
                    deleteTrain(train);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                    getStyleClass().add("actions-cell");
                }
            }
        });
    }

    private void loadTrains() {
        try {
            JsonNode trainsNode = trainService.getAllTrains();
            trains.clear();
            if (trainsNode.isArray()) {
                trainsNode.forEach(trains::add);
            }
        } catch (Exception e) {
            CustomAlert.showInformation("Error", "Failed to load trains: " + e.getMessage());
        }
    }

    @FXML
    private void onAddTrainClicked(ActionEvent event) {
        selectedTrain = null;
        showForm();
        clearForm();
        formTitle.setText("Add New Train");
    }

    private void showEditForm(JsonNode train) {
        selectedTrain = train;
        showForm();
        formTitle.setText("Edit Train");
        
        trainNameField.setText(train.get("trainName").asText());
        trainTypeCombo.setValue(train.get("trainType").asText());
        capacityField.setText(String.valueOf(train.get("capacity").asInt()));
        statusCombo.setValue(train.get("status").asText());
    }

    @FXML
    private void onSaveClicked(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            ObjectNode trainNode = objectMapper.createObjectNode()
                    .put("trainName", trainNameField.getText())
                    .put("trainType", trainTypeCombo.getValue())
                    .put("capacity", Integer.parseInt(capacityField.getText()))
                    .put("status", statusCombo.getValue());

            if (selectedTrain == null) {
                // Add new train
                trainService.createTrain(trainNode);
            } else {
                // Update existing train
                trainService.updateTrain(selectedTrain.get("id").asLong(), trainNode);
            }

            loadTrains();
            hideForm();
            CustomAlert.showInformation("Success", "Train " + (selectedTrain == null ? "added" : "updated") + " successfully!");
        } catch (Exception e) {
            CustomAlert.showInformation("Error", "Failed to " + (selectedTrain == null ? "add" : "update") + " train: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelClicked(ActionEvent event) {
        hideForm();
    }

    private void deleteTrain(JsonNode train) {
        Optional<ButtonType> result = CustomAlert.showConfirmation(
            "Delete Train",
            "Are you sure you want to delete this train?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                trainService.deleteTrain(train.get("id").asLong());
                loadTrains();
                CustomAlert.showSuccess("Success", "Train deleted successfully!");
            } catch (Exception e) {
                CustomAlert.showError("Error", "Failed to delete train: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        String name = trainNameField.getText();
        String type = trainTypeCombo.getValue();
        String capacity = capacityField.getText();
        String status = statusCombo.getValue();

        if (name == null || name.trim().isEmpty()) {
            CustomAlert.showInformation("Validation Error", "Train name is required");
            return false;
        }

        if (type == null) {
            CustomAlert.showInformation("Validation Error", "Train type is required");
            return false;
        }

        if (capacity == null || capacity.trim().isEmpty()) {
            CustomAlert.showInformation("Validation Error", "Capacity is required");
            return false;
        }

        try {
            int cap = Integer.parseInt(capacity);
            if (cap <= 0) {
                CustomAlert.showInformation("Validation Error", "Capacity must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            CustomAlert.showInformation("Validation Error", "Capacity must be a valid number");
            return false;
        }

        if (status == null) {
            CustomAlert.showInformation("Validation Error", "Status is required");
            return false;
        }

        return true;
    }

    private void showForm() {
        trainFormContainer.setVisible(true);
        trainFormContainer.setManaged(true);
    }

    private void hideForm() {
        trainFormContainer.setVisible(false);
        trainFormContainer.setManaged(false);
    }

    private void clearForm() {
        trainNameField.clear();
        trainTypeCombo.setValue(null);
        capacityField.clear();
        statusCombo.setValue("active");
    }

    private void showAlert(String title, String content) {
        CustomAlert.showInformation(title, content);
    }

    // Navigation methods
    @FXML private void onHomeClicked() {
        navigateToPage("Admin/MainPage.fxml");
    }

    @FXML private void onTrainsClicked() {
        navigateToPage("Admin/TrainsPage.fxml");
    }

    @FXML private void onSchedulesClicked() {
        navigateToPage("Admin/SchedulesPage.fxml");
    }

    @FXML private void onBookingsClicked() {
        navigateToPage("Admin/BookingsPage.fxml");
    }

    @FXML private void onStatisticsClicked() {
        navigateToPage("Admin/StatisticsPage.fxml");
    }

    @FXML private void onLogoutClicked() {
        try {
            // Close current window
            Stage currentStage = (Stage) trainsTable.getScene().getWindow();
            currentStage.close();
            
            // Show login window
            ViewsFactory.showWindow("Login.fxml", "Login");
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showInformation("Logout Error", "Could not return to login page.");
        }
    }

    private void navigateToPage(String fxmlFile) {
        try {
            System.out.println("Attempting to navigate to: " + fxmlFile);
            Stage stage = (Stage) trainsTable.getScene().getWindow();
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
            CustomAlert.showInformation("Navigation Error", "Could not load page: " + e.getMessage());
        }
    }
}
