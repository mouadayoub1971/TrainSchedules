package com.mouad.frontend.Controllers;

import com.mouad.frontend.Controllers.Client.GClientController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mouad.frontend.Services.AdminService;
import com.mouad.frontend.Controllers.Client.ClientController;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class LoginController {
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;

    @FXML
    private FontAwesomeIconView closeIcon;

    private final AdminService adminService;

    public LoginController() {
        this.adminService = AdminService.getInstance();
    }


    @FXML
    void onLoginClick(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password");
            return;
        }

        try {
            AdminService adminService = AdminService.getInstance();
            AdminService.LoginResponse loginResponse = adminService.login(email, password);
            System.out.println("Login response: " + loginResponse.getMessage() + 
                             ", admin=" + loginResponse.isAdmin() + 
                             ", firstName=" + loginResponse.getFirstName());

            if (loginResponse != null) {
                if (loginResponse.getMessage().equals("Login successful")) {
                    System.out.println("Login successful, loading dashboard...");
                    
                    // Clear error message
                    errorLabel.setText("");
                    
                    // Get the stage from any control (e.g., emailField)
                    Stage stage = (Stage) emailField.getScene().getWindow();

                    if (loginResponse.isAdmin()) {
                        // Load the admin dashboard
                        String fxmlPath = "/Fxml/Admin/MainPage.fxml";
                        System.out.println("Loading admin FXML: " + fxmlPath);
                        
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        
                        stage.setTitle("Admin Dashboard - " + loginResponse.getFirstName());
                        stage.setScene(scene);
                    } else {
                        // Load the client dashboard
                        String fxmlPath = "/Fxml/Client/GMainPage.fxml";
                        System.out.println("Loading client FXML: " + fxmlPath);
                        
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                        Parent root = loader.load();
                        
                        // Get the client controller and set the user info
                        GClientController clientController = loader.getController();
                        String displayName = loginResponse.getEmail();
                        clientController.setUserInfo(displayName, loginResponse.getEmail());
                        
                        Scene scene = new Scene(root);
                        stage.setTitle("GMain Dashboard - " + loginResponse.getEmail());
                        stage.setScene(scene);
                    }
                    stage.show();
                } else {
                    // Show error message from server
                    errorLabel.setText(loginResponse.getMessage());
                    System.err.println("Login failed: " + loginResponse.getMessage());
                }
            } else {
                errorLabel.setText("Error connecting to server");
                System.err.println("No response from server");
            }
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onCloseClick() {
        Stage stage = (Stage) closeIcon.getScene().getWindow();
        stage.close();
    }
}
