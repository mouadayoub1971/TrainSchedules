package com.mouad.frontend.Controllers.Client;

import com.mouad.frontend.Models.User;
import com.mouad.frontend.Services.UserService;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.mouad.frontend.Services.BookingService;
import com.mouad.frontend.Views.ViewsFactory;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import com.mouad.frontend.Components.CustomAlert;

public class ClientController implements javafx.fxml.Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label totalBookingsLabel;
    @FXML
    private Label activeBookingsLabel;
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
    private VBox menuVBox;

    private final UserService userService;
    private final BookingService bookingService;

    public ClientController() {
        this.userService = UserService.getInstance();
        this.bookingService = BookingService.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initializing ClientController...");
            
            // Set up click handlers for icons
            homeIcon.setOnMouseClicked(event -> onHomeClicked());
            searchIcon.setOnMouseClicked(event -> onSearchClicked());
            profileIcon.setOnMouseClicked(event -> onProfileClicked());
            bookingsIcon.setOnMouseClicked(event -> onBookingsClicked());
            logoutIcon.setOnMouseClicked(event -> onLogoutClicked());
            
            // Set up click handlers for labels
            homeLabel.setOnMouseClicked(event -> onHomeClicked());
            searchLabel.setOnMouseClicked(event -> onSearchClicked());
            profileLabel.setOnMouseClicked(event -> onProfileClicked());
            bookingsLabel.setOnMouseClicked(event -> onBookingsClicked());
            logoutLabel.setOnMouseClicked(event -> onLogoutClicked());

            // Set user information
            User currentUser = userService.getCurrentUser();
            System.out.println("Current user: " + (currentUser != null ? 
                currentUser.getFirstName() + " " + currentUser.getLastName() : "null"));
                
            if (currentUser != null && welcomeLabel != null) {
                String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
                System.out.println("Setting welcome label to: " + fullName);
                welcomeLabel.setText(fullName);
            } else {
                System.err.println("Warning: currentUser or welcomeLabel is null");
                if (currentUser == null) System.err.println("currentUser is null");
                if (welcomeLabel == null) System.err.println("welcomeLabel is null");
            }

            // Initialize booking counters
            updateBookingCounters();

            // Set up auto-refresh for booking counters (every 30 seconds)
            Timeline bookingsUpdateTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), event -> updateBookingCounters())
            );
            bookingsUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
            bookingsUpdateTimeline.play();

        } catch (Exception e) {
            System.err.println("Error in initialize: " + e.getMessage());
            e.printStackTrace();
            CustomAlert.showError("Initialization Error", "Failed to initialize: " + e.getMessage());
        }
    }

    @FXML
    private void onHomeClicked() {
        navigateToPage("Client/MainPage.fxml");
    }

    @FXML
    private void onSearchClicked() {
        navigateToPage("Client/Search.fxml");
    }

    @FXML
    private void onProfileClicked() {
        navigateToPage("Client/Profile.fxml");
    }

    @FXML
    private void onBookingsClicked() {
        navigateToPage("Client/Bookings.fxml");
    }

    @FXML
    private void onLogoutClicked() {
        try {
            userService.logout();
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
    
    private void updateBookingCounters() {
        try {
            // Get current values
            int currentTotal = totalBookingsLabel.getText().isEmpty() ? 0 : Integer.parseInt(totalBookingsLabel.getText());
            int currentActive = activeBookingsLabel.getText().isEmpty() ? 0 : Integer.parseInt(activeBookingsLabel.getText());

            // Get new values
            int newTotal = userService.getTotalBookings();
            int newActive = userService.getActiveBookings();

            // Animate total bookings counter
            if (currentTotal != newTotal) {
                animateCounter(totalBookingsLabel, currentTotal, newTotal);
            }

            // Animate active bookings counter
            if (currentActive != newActive) {
                animateCounter(activeBookingsLabel, currentActive, newActive);
            }

        } catch (Exception e) {
            e.printStackTrace();
            CustomAlert.showError("Error", "Failed to update booking counters: " + e.getMessage());
        }
    }

    private void animateCounter(Label label, int startValue, int endValue) {
        Duration duration = Duration.seconds(1);
        SimpleIntegerProperty intProperty = new SimpleIntegerProperty(startValue);
        
        intProperty.addListener((observable, oldValue, newValue) -> 
            Platform.runLater(() -> label.setText(String.valueOf(newValue.intValue())))
        );

        Timeline timeline = new Timeline(
            new KeyFrame(duration,
                new KeyValue(intProperty, endValue, Interpolator.EASE_BOTH)
            )
        );
        
        timeline.play();
    }

    private void loadBookingStats() {
        try {
            // Get booking statistics from service
            int totalBookings = userService.getUserTotalBookings();
            int activeBookings = userService.getUserActiveBookings();

            // Animate the counters
            animateCounter(totalBookingsLabel, 0, totalBookings);
            animateCounter(activeBookingsLabel, 0, activeBookings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
