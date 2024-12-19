package com.mouad.frontend.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mouad.frontend.Utils.ApiClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;

public class BookingService {
    private static BookingService instance;
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    private BookingService() {
        this.apiClient = ApiClient.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    public static synchronized BookingService getInstance() {
        if (instance == null) {
            instance = new BookingService();
        }
        return instance;
    }

    public JsonNode createBooking(JsonNode bookingData) {
        try {
            System.out.println("Sending booking data: " + bookingData.toString());
            String response = apiClient.post("/bookings", bookingData);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains("Not enough available seats")) {
                throw new RuntimeException(message.substring(message.indexOf("Not enough available seats")));
            }
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create booking", e);
        }
    }

    public JsonNode getUserBookings(String userId) {
        try {
            String response = apiClient.get("/bookings/user/" + userId);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            System.err.println("Error fetching user bookings: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch user bookings", e);
        }
    }

    public JsonNode getAllBookings() throws Exception {
        String response = apiClient.get("/bookings");
        return objectMapper.readTree(response);
    }

    public JsonNode getBooking(int id) throws Exception {
        String response = apiClient.get("/bookings/" + id);
        return objectMapper.readTree(response);
    }

    public JsonNode updateBooking(JsonNode bookingData) throws Exception {
        String response = apiClient.put("/bookings/" + bookingData.get("id").asInt(), bookingData);
        return objectMapper.readTree(response);
    }

    public void deleteBooking(int id) throws Exception {
        try {
            System.out.println("Deleting booking with ID: " + id);
            String response = apiClient.delete("/bookings/" + id);
            // If we get here, the deletion was successful (either with 204 No Content or 200 OK)
            System.out.println("Successfully deleted booking with ID: " + id);
        } catch (Exception e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public JsonNode getAvailableSchedules() throws Exception {
        String response = apiClient.get("/schedules");
        return objectMapper.readTree(response);
    }

    public int getTotalBookings() throws Exception {
        String response = apiClient.get("/bookings/stats/total");
        JsonNode node = objectMapper.readTree(response);
        return node.get("count").asInt();
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

    public int getTodayBookings() throws Exception {
        String response = apiClient.get("/bookings/stats/today");
        JsonNode node = objectMapper.readTree(response);
        return node.get("count").asInt();
    }

    public JsonNode searchSchedules(String from, String to, LocalDate date) throws Exception {
        String encodedFrom = URLEncoder.encode(from, StandardCharsets.UTF_8.toString());
        String encodedTo = URLEncoder.encode(to, StandardCharsets.UTF_8.toString());
        String response = apiClient.get(String.format("/schedules/search?from=%s&to=%s&date=%s", 
            encodedFrom, encodedTo, date.toString()));
        return objectMapper.readTree(response);
    }

    public JsonNode getScheduleDetails(Integer scheduleId) throws Exception {
        String response = apiClient.get("/schedules/" + scheduleId);
        return objectMapper.readTree(response);
    }

    public JsonNode checkAvailability(Integer scheduleId, Integer numSeats) throws Exception {
        // First get the schedule details
        String response = apiClient.get("/schedules/" + scheduleId);
        JsonNode schedule = objectMapper.readTree(response);
        
        // Create response object
        ObjectNode result = objectMapper.createObjectNode();
        
        if (schedule != null) {
            // Check if schedule is available
            boolean isAvailable = schedule.has("available") ? 
                schedule.get("available").asBoolean() : false;
                
            // Get capacity from train
            int capacity = schedule.get("train").get("capacity").asInt();
            
            // Check if we have enough seats
            result.put("available", isAvailable && capacity >= numSeats);
            result.put("remainingSeats", capacity);
        } else {
            result.put("available", false);
            result.put("remainingSeats", 0);
        }
        
        return result;
    }

    public void cancelBooking(int bookingId) throws Exception {
        String response = apiClient.post("/bookings/" + bookingId + "/cancel", new HashMap<>());
        JsonNode node = objectMapper.readTree(response);
        if (!node.has("status") || !"CANCELLED".equals(node.get("status").asText())) {
            throw new RuntimeException("Failed to cancel booking");
        }
    }

    public JsonNode updateBookingStatus(int id, String status) throws Exception {
        String response = apiClient.put("/bookings/" + id + "/status?status=" + status, null);
        return objectMapper.readTree(response);
    }
}
