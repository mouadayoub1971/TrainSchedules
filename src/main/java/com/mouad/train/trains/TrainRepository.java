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

    // Find trains by status
    List<Train> findByStatus(String status);

    // Find trains by multiple statuses (e.g., "active", "inactive")
    List<Train> findByStatusIn(List<String> statuses);
}
