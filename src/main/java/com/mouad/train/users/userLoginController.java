package com.mouad.train.users;

import com.mouad.train.Producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
public class userLoginController {

    public final KafkaProducer kafkaProducer;
    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Generate logs with unique context data for each event
    private String generateLog(String logLevel, String loggerName, String message, String contextData) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String threadName = Thread.currentThread().getName();
        return String.format("%s [%s] %s %s: %s - %s",
                timestamp,
                logLevel,
                threadName,
                loggerName,
                message,
                contextData != null ? contextData : "N/A");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        String logMessage;

        // Validate email
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            logMessage = generateLog("INFO", "LoginController", "Login attempt failed: Email is required", null);
            kafkaProducer.sendMessage(logMessage);
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        }

        // Validate password
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            logMessage = generateLog("INFO", "LoginController", "Login attempt failed: Password is required", null);
            kafkaProducer.sendMessage(logMessage);
            return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
        }

        // Find user by email
        User existingUser = repo.findByEmail(user.getEmail());

        // Register new user if not found
        if (existingUser == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setAdmin(false);
            repo.save(user);
            logMessage = generateLog("INFO", "LoginController", "User registered successfully", "email=" + user.getEmail());
            kafkaProducer.sendMessage(logMessage);
            return new ResponseEntity<>("You were not found, but you have been registered. Welcome!", HttpStatus.CREATED);
        }

        // Validate password for existing user
        if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            logMessage = generateLog("WARN", "LoginController", "Login attempt failed: Invalid password", "email=" + user.getEmail());
            kafkaProducer.sendMessage(logMessage);
            return new ResponseEntity<>("Invalid password", HttpStatus.UNAUTHORIZED);
        }

        // Check if user is admin
        if (existingUser.getAdmin()) {
            logMessage = generateLog("INFO", "LoginController", "Admin logged in successfully", "email=" + user.getEmail());
            kafkaProducer.sendMessage(logMessage);
            return new ResponseEntity<>("Welcome Admin", HttpStatus.OK);
        }

        // Successful user login
        logMessage = generateLog("INFO", "LoginController", "User logged in successfully", "email=" + user.getEmail());
        kafkaProducer.sendMessage(logMessage);
        return new ResponseEntity<>("Welcome User", HttpStatus.OK);
    }
}
