package com.mouad.frontend.Controllers.Client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import com.mouad.frontend.Services.UserService;
import com.mouad.frontend.Services.BookingService;
import com.mouad.frontend.Views.ViewsFactory;
import java.io.IOException;

public class ClientController {
    private final UserService userService;
    private final BookingService bookingService;
    private String firstName;
    private String email;

    @FXML private Label welcomeLabel;
    @FXML private VBox contentArea;
    
    // Menu icons
    @FXML private ImageView homeIcon;
    @FXML private ImageView bookingsIcon;
    @FXML private ImageView logoutIcon;
    
    // Menu labels
    @FXML private Label homeLabel;
    @FXML private Label bookingsLabel;
    @FXML private Label logoutLabel;

    public ClientController() {
        this.userService = UserService.getInstance();
        this.bookingService = BookingService.getInstance();
    }

    @FXML
    public void initialize() {
        System.out.println("Client dashboard initialized");
        if (firstName != null) {
            welcomeLabel.setText("Welcome, " + firstName + "!");
        }
        
        // Set initial active state
        setActiveMenu("home");
    }

    @FXML
    private void onHomeClicked() {
        System.out.println("Home clicked");
        setActiveMenu("home");
        // TODO: Load home content
    }

    @FXML
    private void onViewBookings() {
        System.out.println("View bookings clicked");
        setActiveMenu("bookings");
        // TODO: Load bookings content
    }

    @FXML
    private void onLogout() {
        try {
            System.out.println("Logout clicked");
            // Close current window
            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            currentStage.close();
            
            // Show login window
            ViewsFactory.showWindow("Login.fxml", "Login");
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUserInfo(String firstName, String email) {
        System.out.println("Setting user info - firstName: " + firstName + ", email: " + email);
        this.firstName = firstName;
        this.email = email;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + firstName + "!");
        }
    }

    private void setActiveMenu(String menu) {
        // Remove active class from all menu items
        homeIcon.getStyleClass().remove("active");
        bookingsIcon.getStyleClass().remove("active");
        homeLabel.getStyleClass().remove("active");
        bookingsLabel.getStyleClass().remove("active");

        // Add active class to selected menu
        switch (menu) {
            case "home":
                homeIcon.getStyleClass().add("active");
                homeLabel.getStyleClass().add("active");
                break;
            case "bookings":
                bookingsIcon.getStyleClass().add("active");
                bookingsLabel.getStyleClass().add("active");
                break;
        }
    }
}
