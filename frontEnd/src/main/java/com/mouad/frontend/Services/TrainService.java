package com.mouad.frontend.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mouad.frontend.Utils.ApiClient;

public class TrainService {
    private static volatile TrainService instance;
    private final ApiClient apiClient;

    private TrainService() {
        this.apiClient = ApiClient.getInstance();
    }

    public static TrainService getInstance() {
        if (instance == null) {
            synchronized (TrainService.class) {
                if (instance == null) {
                    instance = new TrainService();
                }
            }
        }
        return instance;
    }

    public JsonNode getAllTrains() throws Exception {
        String response = apiClient.get("/trains");
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode getTrain(Long id) throws Exception {
        String response = apiClient.get("/trains/" + id);
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode getTrainSchedules(Long trainId) throws Exception {
        String response = apiClient.get("/trains/" + trainId + "/schedules");
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode createTrain(JsonNode train) throws Exception {
        String response = apiClient.post("/trains", train);
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode updateTrain(Long id, JsonNode train) throws Exception {
        String response = apiClient.put("/trains/" + id, train);
        return apiClient.getObjectMapper().readTree(response);
    }

    public void deleteTrain(Long id) throws Exception {
        apiClient.delete("/trains/" + id);
    }
}
