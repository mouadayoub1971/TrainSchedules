package com.mouad.train.users;

import com.mouad.train.Producer.KafkaProducer;
import com.mouad.train.Log.LogsGene;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;

@RestController
@RequiredArgsConstructor
public class userLoginController {

    private final KafkaProducer kafkaProducer;
    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LogsGene logsGene = new LogsGene();

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody User user) {
        try {
            // Validate email
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                String logMessage = logsGene.generateLog(
                        "INFO", "LoginController", "Login attempt failed: Email is required", null
                );
                kafkaProducer.sendMessage(logMessage);
                return ResponseEntity.badRequest().body(new LoginResponse("Email is required", false, null, null, null));
            }

            // Validate password
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                String logMessage = logsGene.generateLog(
                        "INFO", "LoginController", "Login attempt failed: Password is required", null
                );
                kafkaProducer.sendMessage(logMessage);
                return ResponseEntity.badRequest().body(new LoginResponse("Password is required", false, null, null, null));
            }

            // Find user by email
            User existingUser = repo.findByEmail(user.getEmail());

            if (existingUser == null) {
                // Register new user if not found
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setAdmin(false);
                existingUser = repo.save(user);

                // Send Kafka message for new registration
                String registrationMessage = logsGene.generateLog(
                        "INFO", 
                        "LoginController", 
                        String.format("New user registered - Email: %s", user.getEmail()),
                        null
                );
                kafkaProducer.sendMessage(registrationMessage);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new LoginResponse(
                                "User registered successfully",
                                existingUser.getAdmin(),
                                existingUser.getEmail(),
                                existingUser.getFirstName(),
                                existingUser.getSecondName()
                        ));
            }

            // Verify password for existing user
            if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                String logMessage = logsGene.generateLog(
                        "WARNING", 
                        "LoginController", 
                        String.format("Failed login attempt for user: %s - Invalid password", user.getEmail()),
                        null
                );
                kafkaProducer.sendMessage(logMessage);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse("Invalid password", false, null, null, null));
            }

            // Successful login
            String loginMessage = logsGene.generateLog(
                    "INFO", 
                    "LoginController", 
                    String.format("Successful login - User: %s", user.getEmail()),
                    null
            );
            kafkaProducer.sendMessage(loginMessage);
            
            return ResponseEntity.ok(new LoginResponse(
                "Login successful",
                existingUser.getAdmin(),
                existingUser.getEmail(),
                existingUser.getFirstName(),
                existingUser.getSecondName()
            ));

        } catch (Exception e) {
            String errorMessage = logsGene.generateLog(
                    "ERROR", 
                    "LoginController", 
                    String.format("Login error for user %s: %s", user.getEmail(), e.getMessage()),
                    null
            );
            kafkaProducer.sendMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse("An error occurred during login", false, null, null, null));
        }
    }

    // Utility method to create a JSON response
    private ResponseEntity<String> createJsonResponse(String message, HttpStatus status) {
        try {
            return ResponseEntity.status(status)
                    .body(objectMapper.writeValueAsString(Collections.singletonMap("message", message)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Failed to generate JSON response\"}");
        }
    }
}
