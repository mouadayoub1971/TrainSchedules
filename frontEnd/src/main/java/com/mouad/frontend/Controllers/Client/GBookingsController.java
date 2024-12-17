package com.mouad.frontend.Controllers.Client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GBookingsController implements Initializable {
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setActiveButton(bookingsBtn);
    }

    private void navigateToPage(String fxmlFile) {
        try {
            Stage stage = (Stage) homeBtn.getScene().getWindow();
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

    private void setActiveButton(HBox button) {
        homeBtn.getStyleClass().remove("active");
        searchBtn.getStyleClass().remove("active");
        profileBtn.getStyleClass().remove("active");
        bookingsBtn.getStyleClass().remove("active");
        logoutBtn.getStyleClass().remove("active");
        button.getStyleClass().add("active");
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
}
