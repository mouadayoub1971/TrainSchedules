package com.mouad.frontend.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mouad.frontend.Utils.ApiClient;

public class ScheduleService {
    private static volatile ScheduleService instance;
    private final ApiClient apiClient;

    private ScheduleService() {
        this.apiClient = ApiClient.getInstance();
    }

    public static ScheduleService getInstance() {
        if (instance == null) {
            synchronized (ScheduleService.class) {
                if (instance == null) {
                    instance = new ScheduleService();
                }
            }
        }
        return instance;
    }

    public JsonNode getAllSchedules() throws Exception {
        String response = apiClient.get("/schedules");
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode getSchedule(Integer id) throws Exception {
        String response = apiClient.get("/schedules/" + id);
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode createSchedule(JsonNode schedule) throws Exception {
        String response = apiClient.post("/schedules", schedule);
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode updateSchedule(Integer id, JsonNode schedule) throws Exception {
        String response = apiClient.put("/schedules/" + id, schedule);
        return apiClient.getObjectMapper().readTree(response);
    }

    public void deleteSchedule(Integer id) throws Exception {
        apiClient.delete("/schedules/" + id);
    }
}
