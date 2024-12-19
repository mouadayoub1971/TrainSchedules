package com.mouad.frontend.Controllers;

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
import com.mouad.frontend.Services.AdminService;
import com.mouad.frontend.Services.UserService;
import com.mouad.frontend.Models.User;
import java.net.URL;

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
    private final UserService userService;

    public LoginController() {
        this.adminService = AdminService.getInstance();
        this.userService = UserService.getInstance();
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
            // Try login and get response
            AdminService.LoginResponse loginResponse = adminService.login(email, password);
            System.out.println("this is the login response " + loginResponse);
            
            if (loginResponse == null || !loginResponse.getMessage().equals("Login successful")) {
                errorLabel.setText("Invalid email or password");
                return;
            }

            // At this point, login was successful
            errorLabel.setText("");
            Stage stage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader;
            Scene scene;
            
            if (loginResponse.isAdmin()) {
                // Admin user - load admin dashboard
                System.out.println("Loading admin dashboard for: " + loginResponse.getFirstName());
                loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/MainPage.fxml"));
                scene = new Scene(loader.load());
                stage.setTitle("Admin Dashboard - " + loginResponse.getFirstName());
            } else {
                // Regular user - set up user service and load client dashboard
                System.out.println("Loading client dashboard for: " + loginResponse.getFirstName());
                
                // Set up user information in UserService
                User user = new User();
                user.setId(Long.valueOf(loginResponse.getUserId())); // Convert to Long
                user.setEmail(loginResponse.getEmail());
                user.setFirstName(loginResponse.getFirstName());
                user.setLastName(loginResponse.getSecondName());
                userService.setCurrentUser(user);
                
                // Load client dashboard
                loader = new FXMLLoader(getClass().getResource("/Fxml/Client/MainPage.fxml"));
                scene = new Scene(loader.load());
                
                // Add CSS for client dashboard
                URL cssResource = getClass().getResource("/styles/Client/MainPage.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                }
                
                stage.setTitle("Train Schedules - " + user.getFirstName());
            }
            
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("An error occurred during login");
        }
    }

    @FXML
    void onCloseClick() {
        Stage stage = (Stage) closeIcon.getScene().getWindow();
        stage.close();
    }
}
