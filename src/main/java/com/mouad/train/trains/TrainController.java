package com.mouad.train.trains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/trains")
public class TrainController {
    @Autowired
    private TrainRepository trainRepository;

    @GetMapping
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @GetMapping("/{id}")
    public Train getTrainById(@PathVariable Integer id) {
        return trainRepository.findById(id).orElseThrow(() -> new RuntimeException("Train not found"));
    }

    @PostMapping
    public Train addTrain(@RequestBody Train train) {
        return trainRepository.save(train);
    }

    @PutMapping("/{id}")
    public Train updateTrain(@PathVariable Integer id, @RequestBody Train updatedTrain) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new RuntimeException("Train not found"));
        train.setTrainName(updatedTrain.getTrainName());
        train.setTrainType(updatedTrain.getTrainType());
        train.setCapacity(updatedTrain.getCapacity());
        train.setActive(updatedTrain.getActive());
        return trainRepository.save(train);
    }

    @DeleteMapping("/{id}")
    public void deleteTrain(@PathVariable Integer id) {
        trainRepository.deleteById(id);
    }
}
