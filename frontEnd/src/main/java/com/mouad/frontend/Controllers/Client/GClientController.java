package com.mouad.frontend.Controllers.Client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GClientController implements Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private HBox homeBtn;
    @FXML
    private HBox searchBtn;
    @FXML
    private HBox profileBtn;
    @FXML
    private HBox bookingsBtn;
    @FXML
    private HBox logoutBtn;
    @FXML
    private ImageView homeIcon;
    @FXML
    private ImageView searchIcon;
    @FXML
    private ImageView profileIcon;
    @FXML
    private ImageView bookingsIcon;
    @FXML
    private ImageView logoutIcon;
    @FXML
    private Label homeLabel;
    @FXML
    private Label searchLabel;
    @FXML
    private Label profileLabel;
    @FXML
    private Label bookingsLabel;
    @FXML
    private Label logoutLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set click handlers for icons
        homeIcon.setOnMouseClicked(event -> home());
        searchIcon.setOnMouseClicked(event -> search());
        profileIcon.setOnMouseClicked(event -> profile());
        bookingsIcon.setOnMouseClicked(event -> bookings());
        logoutIcon.setOnMouseClicked(event -> logout());

        // Set click handlers for labels
        homeLabel.setOnMouseClicked(event -> home());
        searchLabel.setOnMouseClicked(event -> search());
        profileLabel.setOnMouseClicked(event -> profile());
        bookingsLabel.setOnMouseClicked(event -> bookings());
        logoutLabel.setOnMouseClicked(event -> logout());
    }

    private void navigateToPage(String fxmlFile) {
        try {
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
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
    private void home() {
        setActiveButton(homeBtn);
        navigateToPage("MainPage.fxml");
    }

    @FXML
    private void search() {
        setActiveButton(searchBtn);
        navigateToPage("Search.fxml");
    }

    @FXML
    private void profile() {
        setActiveButton(profileBtn);
        navigateToPage("Profile.fxml");
    }

    @FXML
    private void bookings() {
        setActiveButton(bookingsBtn);
        navigateToPage("Bookings.fxml");
    }

    @FXML
    private void logout() {
        setActiveButton(logoutBtn);
        navigateToPage("login.fxml");
    }

    private void setActiveButton(HBox button) {
        homeBtn.getStyleClass().remove("active");
        searchBtn.getStyleClass().remove("active");
        profileBtn.getStyleClass().remove("active");
        bookingsBtn.getStyleClass().remove("active");
        logoutBtn.getStyleClass().remove("active");
        button.getStyleClass().add("active");

    }
}
