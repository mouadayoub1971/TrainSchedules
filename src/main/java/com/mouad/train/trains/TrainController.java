package com.mouad.train.trains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public Train getTrainById(@PathVariable Integer id) {
        return trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));
    }

    // Add a new train
    @PostMapping
    public Train addTrain(@RequestBody Train train) {
        // Check if a train with the same name and type already exists
        if (!trainRepository.findByTrainNameAndTrainType(train.getTrainName(), train.getTrainType()).isEmpty()) {
            throw new RuntimeException("Train with the same name and type already exists");
        }
        return trainRepository.save(train);
    }

    // Update an existing train
    @PutMapping("/{id}")
    public Train updateTrain(@PathVariable Integer id, @RequestBody Train updatedTrain) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));

        // Check if updating the train results in a duplicate
        List<Train> existingTrains = trainRepository.findByTrainNameAndTrainType(updatedTrain.getTrainName(), updatedTrain.getTrainType());
        if (!existingTrains.isEmpty() && !existingTrains.get(0).getId().equals(id)) {
            throw new RuntimeException("Another train with the same name and type already exists");
        }

        train.setTrainName(updatedTrain.getTrainName());
        train.setTrainType(updatedTrain.getTrainType());
        train.setCapacity(updatedTrain.getCapacity());
        train.setActive(updatedTrain.getActive());
        return trainRepository.save(train);
    }

    // Delete a train
    @DeleteMapping("/{id}")
    public void deleteTrain(@PathVariable Integer id) {
        trainRepository.deleteById(id);
    }
}
