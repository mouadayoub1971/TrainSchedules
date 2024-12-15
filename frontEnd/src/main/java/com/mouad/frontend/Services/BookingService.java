package com.mouad.frontend.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mouad.frontend.Utils.ApiClient;

public class BookingService {
    private static volatile BookingService instance;
    private final ApiClient apiClient;

    private BookingService() {
        this.apiClient = ApiClient.getInstance();
    }

    public static BookingService getInstance() {
        if (instance == null) {
            synchronized (BookingService.class) {
                if (instance == null) {
                    instance = new BookingService();
                }
            }
        }
        return instance;
    }

    public JsonNode getAllBookings() throws Exception {
        String response = apiClient.get("/bookings");
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode getBooking(Long id) throws Exception {
        String response = apiClient.get("/bookings/" + id);
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode getUserBookings(Long userId) throws Exception {
        String response = apiClient.get("/bookings/user/" + userId);
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode createBooking(JsonNode bookingData) throws Exception {
        String response = apiClient.post("/bookings", bookingData);
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode updateBooking(JsonNode bookingData) throws Exception {
        String response = apiClient.put("/bookings/" + bookingData.get("id").asInt(), bookingData);
        return apiClient.getObjectMapper().readTree(response);
    }

    public void deleteBooking(int id) throws Exception {
        String response = apiClient.delete("/bookings/" + id);
        // No need to parse response for 204 No Content
    }

    public JsonNode getAvailableSchedules() throws Exception {
        String response = apiClient.get("/schedules");
        return apiClient.getObjectMapper().readTree(response);
    }

    public int getTotalBookings() throws Exception {
        JsonNode bookings = getAllBookings();
        return bookings.size();
    }

    public int getActiveBookings() throws Exception {
        JsonNode bookings = getAllBookings();
        int count = 0;
        for (JsonNode booking : bookings) {
            if (booking.has("status") && "CONFIRMED".equals(booking.get("status").asText())) {
                count++;
            }
        }
        return count;
    }
}
