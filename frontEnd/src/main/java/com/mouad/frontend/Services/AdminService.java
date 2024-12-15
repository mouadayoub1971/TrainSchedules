package com.mouad.frontend.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mouad.frontend.Utils.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AdminService {
    private static AdminService instance;
    private static String currentAdminName;
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    private AdminService() {
        this.apiClient = ApiClient.getInstance();
        this.objectMapper = apiClient.getObjectMapper();
    }

    public static AdminService getInstance() {
        if (instance == null) {
            instance = new AdminService();
        }
        return instance;
    }

    // Booking related methods
    public JsonNode getAllBookings() throws Exception {
        String response = apiClient.get("/bookings");
        return objectMapper.readTree(response);
    }

    public int getTotalBookings() throws Exception {
        JsonNode bookings = getAllBookings();
        return bookings.size();
    }

    public int getActiveBookings() throws Exception {
        JsonNode bookings = getAllBookings();
        int count = 0;
        for (JsonNode booking : bookings) {
            if ("CONFIRMED".equals(booking.get("status").asText())) {
                count++;
            }
        }
        return count;
    }

    public JsonNode createBooking(JsonNode booking) throws Exception {
        String response = apiClient.post("/bookings", booking);
        return objectMapper.readTree(response);
    }

    public JsonNode updateBooking(Integer id, JsonNode booking) throws Exception {
        String response = apiClient.put("/bookings/" + id, booking);
        return objectMapper.readTree(response);
    }

    public void deleteBooking(int bookingId) throws Exception {
        String url = apiClient.getBaseUrl() + "/bookings/" + bookingId;
        String response = apiClient.delete(url);
        
        if (response != null) {
            JsonNode node = objectMapper.readTree(response);
            if (!node.get("success").asBoolean()) {
                throw new Exception("Failed to delete booking: " + response);
            }
        } else {
            throw new Exception("Failed to delete booking");
        }
    }

    public int getBookedSeatsForSchedule(JsonNode schedule) throws Exception {
        String response = apiClient.get("/bookings/schedule/" + schedule.get("id").asText() + "/seats");
        JsonNode node = objectMapper.readTree(response);
        return node.get("bookedSeats").asInt();
    }

    public void cancelBooking(int bookingId) throws Exception {
        String response = apiClient.put("/bookings/" + bookingId + "/cancel", null);
        JsonNode node = objectMapper.readTree(response);
        if (!node.get("success").asBoolean()) {
            throw new Exception("Failed to cancel booking");
        }
    }

    // User related methods
    public JsonNode getAllUsers() throws Exception {
        String response = apiClient.get("/users");
        return objectMapper.readTree(response);
    }

    // Schedule related methods
    public JsonNode getSchedulesWithAvailableSeats() throws Exception {
        String response = apiClient.get("/schedules/available");
        return objectMapper.readTree(response);
    }

    // Login related inner classes and methods
    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private String message;
        private boolean admin;
        private String email;
        private String firstName;
        private String secondName;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public boolean isAdmin() { return admin; }
        public void setAdmin(boolean admin) { this.admin = admin; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getSecondName() { return secondName; }
        public void setSecondName(String secondName) { this.secondName = secondName; }
    }

    public LoginResponse login(String email, String password) {
        try {
            LoginRequest request = new LoginRequest(email, password);
            String response = apiClient.post("/login", request);
            
            if (response != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);

                if (loginResponse.getMessage() != null && 
                    loginResponse.getMessage().equals("Login successful")) {
                    if (loginResponse.isAdmin()) {
                        currentAdminName = loginResponse.getFirstName();
                    }
                }
                return loginResponse;
            }
        } catch (Exception e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage("Error: " + e.getMessage());
            return errorResponse;
        }
        return null;
    }

    public int getTotalTrains() {
        try {
            String responseStr = apiClient.get("/trains/stats/count");
            if (responseStr != null) {
                JsonNode response = apiClient.getObjectMapper().readTree(responseStr);
                return response.asInt();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error getting total trains: " + e.getMessage());
            return 0;
        }
    }

    public int getTotalSchedules() {
        try {
            String responseStr = apiClient.get("/schedules/stats/count");
            if (responseStr != null) {
                JsonNode response = apiClient.getObjectMapper().readTree(responseStr);
                return response.asInt();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error getting total schedules: " + e.getMessage());
            return 0;
        }
    }

    public String getCurrentAdminName() {
        return currentAdminName;
    }

    public void clearCurrentAdmin() {
        currentAdminName = null;
        try {
            apiClient.close();
        } catch (Exception e) {
            System.err.println("Error closing ApiClient: " + e.getMessage());
            e.printStackTrace();
        }
    }
}