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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    private final String API_URL = "http://localhost:8080/login";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LoginController() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @FXML
    public void onLoginClick(ActionEvent actionEvent) {
        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            // Validate input
            if (email.isEmpty()) {
                errorLabel.setText("Please enter your email");
                return;
            }
            if (password.isEmpty()) {
                errorLabel.setText("Please enter your password");
                return;
            }

            // Create request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("email", email);
            requestBody.put("password", password);

            // Create request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                // Clear error message
                errorLabel.setText("");
                
                // Debug: Print response body
                System.out.println("Response body: " + response.body());
                
                try {
                    // Parse the response to get the admin status
                    ObjectNode responseBody = objectMapper.readValue(response.body(), ObjectNode.class);
                    System.out.println("Parsed response: " + responseBody.toString());
                    
                    boolean isAdmin = responseBody.get("admin").asBoolean();
                    String message = responseBody.get("message").asText();
                    System.out.println("Is admin: " + isAdmin);
                    System.out.println("Message: " + message);
                    
                    // Get the stage from any control (e.g., emailField)
                    Stage stage = (Stage) emailField.getScene().getWindow();

                    // Load the appropriate scene based on admin status
                    String fxmlPath = isAdmin ? 
                        "/Fxml/Admin/Admin.fxml" : 
                        "/Fxml/Client/Client.fxml";
                    
                    System.out.println("Loading FXML: " + fxmlPath);
                    
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    
                    // Set window title based on user type
                    stage.setTitle(isAdmin ? "Admin Dashboard" : "Client Dashboard");
                    stage.setScene(scene);
                    stage.show();
                } catch (Exception e) {
                    System.err.println("Error parsing response or loading FXML: " + e.getMessage());
                    e.printStackTrace();
                    errorLabel.setText("Error loading dashboard: " + e.getMessage());
                }
            } else {
                // Handle error response
                String errorMessage;
                try {
                    ObjectNode errorBody = objectMapper.readValue(response.body(), ObjectNode.class);
                    errorMessage = errorBody.get("message").asText();
                } catch (Exception e) {
                    errorMessage = response.body();
                }
                errorLabel.setText(errorMessage);
                System.err.println("Login error: " + errorMessage);
            }
        } catch (Exception e) {
            errorLabel.setText("Error connecting to server: " + e.getMessage());
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
