package com.mouad.frontend.Controllers.Client;

import com.mouad.frontend.Models.User;
import com.mouad.frontend.Services.UserService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import com.mouad.frontend.Views.ViewsFactory;
import javafx.util.Duration;
import com.mouad.frontend.Components.CustomAlert;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
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

    @FXML
    private Label userNameLabel;
    @FXML
    private Label userEmailLabel;
    @FXML
    private Label memberSinceLabel;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private UserService userService;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            userService = UserService.getInstance();
            currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                // Load user info into form fields
                firstNameField.setText(currentUser.getFirstName());
                lastNameField.setText(currentUser.getLastName());
                emailField.setText(currentUser.getEmail());
                
                // Update header labels
                userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                userEmailLabel.setText(currentUser.getEmail());
                memberSinceLabel.setText("Member since: " + formatMemberSince(currentUser.getCreatedAt()));
            }
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showError("Error", "Failed to load user profile: " + e.getMessage());
        }
        
        // Set active button for profile page
        setActiveButton(profileIcon, profileLabel);
        
        // Set click handlers for icons
        homeIcon.setOnMouseClicked(event -> onHomeClicked());
        searchIcon.setOnMouseClicked(event -> onSearchClicked());
        profileIcon.setOnMouseClicked(event -> onProfileClicked());
        bookingsIcon.setOnMouseClicked(event -> onBookingsClicked());
        logoutIcon.setOnMouseClicked(event -> onLogoutClicked());

        // Set click handlers for buttons
        saveButton.setOnMouseClicked(event -> onSaveClicked());
        cancelButton.setOnMouseClicked(event -> onCancelClicked());
    }

    private void setActiveButton(ImageView icon, Label label) {
        // Remove active class from all icons and labels
        homeIcon.getStyleClass().remove("active");
        searchIcon.getStyleClass().remove("active");
        profileIcon.getStyleClass().remove("active");
        bookingsIcon.getStyleClass().remove("active");
        logoutIcon.getStyleClass().remove("active");

        homeLabel.getStyleClass().remove("active");
        searchLabel.getStyleClass().remove("active");
        profileLabel.getStyleClass().remove("active");
        bookingsLabel.getStyleClass().remove("active");
        logoutLabel.getStyleClass().remove("active");

        // Add active class to selected icon and label
        icon.getStyleClass().add("active");
        label.getStyleClass().add("active");
    }

    @FXML
    private void onHomeClicked() {
        setActiveButton(homeIcon, homeLabel);
        navigateToPage("Client/MainPage.fxml");
    }

    @FXML
    private void onSearchClicked() {
        setActiveButton(searchIcon, searchLabel);
        navigateToPage("Client/Search.fxml");
    }

    @FXML
    private void onProfileClicked() {
        setActiveButton(profileIcon, profileLabel);
        navigateToPage("Client/Profile.fxml");
    }

    @FXML
    private void onBookingsClicked() {
        setActiveButton(bookingsIcon, bookingsLabel);
        navigateToPage("Client/Bookings.fxml");
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
            CustomAlert.showError("Logout Error", "Could not return to login page: " + e.getMessage());
        }
    }

    private void navigateToPage(String fxmlFile) {
        try {
            System.out.println("Attempting to navigate to: " + fxmlFile);
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            
            // Debug the FXML resource location
            URL fxmlResource = getClass().getResource("/Fxml/" + fxmlFile);
            System.out.println("FXML Resource URL: " + fxmlResource);
            
            FXMLLoader loader = new FXMLLoader(fxmlResource);
            if (loader.getLocation() == null) {
                throw new IOException("Could not find FXML file: /Fxml/" + fxmlFile);
            }
            
            try {
                Parent root = loader.load();
                Scene scene = new Scene(root);
                
                // Get the CSS file name from the FXML file name
                String cssFileName = fxmlFile.substring(fxmlFile.lastIndexOf('/') + 1).replace(".fxml", ".css");
                URL cssResource = getClass().getResource("/styles/Client/" + cssFileName);
                System.out.println("CSS Resource URL: " + cssResource);
                
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                    System.out.println("Successfully loaded CSS from: /styles/Client/" + cssFileName);
                } else {
                    System.err.println("Warning: Could not load CSS file from: /styles/Client/" + cssFileName);
                }
                
                stage.setScene(scene);
            } catch (IOException e) {
                System.err.println("Error loading FXML content: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } catch (IOException e) {
            System.err.println("Error navigating to " + fxmlFile + ": " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            CustomAlert.showError("Navigation Error", "Could not load page: " + e.getMessage() + "\nCheck console for details.");
        }
    }

    private String formatMemberSince(LocalDateTime date) {
        if (date == null) return "19-12-2024";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return "Member since: " + date.format(formatter);
    }

    @FXML
    private void onSaveClicked() {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Validate inputs
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                CustomAlert.showError("Validation Error", "Please fill in all required fields.");
                return;
            }

            // Update user object
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setEmail(email);

            // Handle password change if provided
            if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                if (currentPassword.isEmpty()) {
                    CustomAlert.showError("Validation Error", "Please enter your current password");
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    CustomAlert.showError("Validation Error", "New passwords do not match");
                    return;
                }
                
                // Validate current password using UserService
                if (!userService.validatePassword(currentPassword)) {
                    CustomAlert.showError("Authentication Error", "Current password is incorrect");
                    return;
                }

                // Update password using UserService
                if (!userService.updatePassword(currentPassword, newPassword)) {
                    CustomAlert.showError("Error", "Failed to update password");
                    return;
                }
            }

            // Save changes
            if (userService.updateUser(currentUser)) {
                CustomAlert.showInfo("Success", "Profile updated successfully");
                // Clear password fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                // Update header labels
                userNameLabel.setText(firstName + " " + lastName);
                userEmailLabel.setText(email);
            } else {
                CustomAlert.showError("Error", "Failed to update profile");
            }
        } catch (Exception e) {
            System.err.println("Error saving profile: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showError("Error", "Failed to save profile changes: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelClicked() {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                firstNameField.setText(currentUser.getFirstName());
                lastNameField.setText(currentUser.getLastName());
                emailField.setText(currentUser.getEmail());
            }
            // Clear password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (Exception e) {
            CustomAlert.showError("Error", "Failed to reset form: " + e.getMessage());
        }
    }
}
