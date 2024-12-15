package com.mouad.frontend.Controllers.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.net.URL;
import com.mouad.frontend.Views.ViewsFactory;

public class StatisticsController {

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
        } catch (Exception e) {
            System.err.println("Error initializing StatisticsController: " + e.getMessage());
            e.printStackTrace();
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
