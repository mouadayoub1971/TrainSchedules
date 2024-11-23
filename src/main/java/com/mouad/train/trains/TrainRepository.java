package com.mouad.train.trains;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainRepository extends JpaRepository<Train, Integer> {
    // Find trains by name
    List<Train> findByTrainName(String trainName);

    // Find trains by type
    List<Train> findByTrainType(String trainType);

    // Find trains by both name and type
    List<Train> findByTrainNameAndTrainType(String trainName, String trainType);

    // Find active or inactive trains
    List<Train> findByActive(Boolean active);
}
