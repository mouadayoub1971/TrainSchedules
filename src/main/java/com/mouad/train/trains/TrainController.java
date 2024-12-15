package com.mouad.train.trains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/trains")
public class TrainController {

    @Autowired
    private TrainRepository trainRepository;

    // Get all trains
    @GetMapping
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    // Get a train by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTrainById(@PathVariable Integer id) {
        try {
            Train train = trainRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
            return ResponseEntity.ok(train);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // Add a new train
    @PostMapping
    public ResponseEntity<?> addTrain(@RequestBody Train train) {
        try {
            // Validate required fields
            if (train.getTrainName() == null || train.getTrainName().trim().isEmpty()) {
                throw new IllegalArgumentException("Train name is required");
            }
            if (train.getTrainType() == null || train.getTrainType().trim().isEmpty()) {
                throw new IllegalArgumentException("Train type is required");
            }
            if (train.getCapacity() == null || train.getCapacity() <= 0) {
                throw new IllegalArgumentException("Valid capacity is required");
            }

            // Check if a train with the same name and type already exists
            if (!trainRepository.findByTrainNameAndTrainType(train.getTrainName(), train.getTrainType()).isEmpty()) {
                throw new IllegalArgumentException("Train with the same name and type already exists");
            }

            // Set default status to "active" if not provided
            if (train.getStatus() == null || train.getStatus().trim().isEmpty()) {
                train.setStatus("active");
            }

            Train savedTrain = trainRepository.save(train);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTrain);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to create train: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Update an existing train
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrain(@PathVariable Integer id, @RequestBody Train updatedTrain) {
        try {
            Train train = trainRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));

            // Update fields if provided
            if (updatedTrain.getTrainName() != null) {
                train.setTrainName(updatedTrain.getTrainName());
            }
            if (updatedTrain.getTrainType() != null) {
                train.setTrainType(updatedTrain.getTrainType());
            }
            if (updatedTrain.getCapacity() != null) {
                train.setCapacity(updatedTrain.getCapacity());
            }
            if (updatedTrain.getStatus() != null) {
                train.setStatus(updatedTrain.getStatus());
            }

            Train savedTrain = trainRepository.save(train);
            return ResponseEntity.ok(savedTrain);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to update train: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Delete a train
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrain(@PathVariable Integer id) {
        try {
            if (!trainRepository.existsById(id)) {
                throw new RuntimeException("Train not found with id: " + id);
            }
            trainRepository.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Train deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to delete train: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get all active trains
    @GetMapping("/active")
    public List<Train> getActiveTrains() {
        return trainRepository.findByStatus("active");
    }

    // Change train status
    @PutMapping("/{id}/status")
    public Train changeTrainStatus(@PathVariable Integer id, @RequestParam String status) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));

        train.setStatus(status);
        return trainRepository.save(train);
    }

    // Get total number of trains
    @GetMapping("/stats/count")
    public ResponseEntity<Integer> getTotalTrains() {
        try {
            int totalTrains = trainRepository.findAll().size();
            return ResponseEntity.ok(totalTrains);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }
}
