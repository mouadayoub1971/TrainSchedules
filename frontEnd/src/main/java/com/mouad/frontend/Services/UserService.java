package com.mouad.frontend.Services;

import com.mouad.frontend.Models.User;
import com.mouad.frontend.Utils.ApiClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.net.URLEncoder;
import java.time.LocalDate;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

public class UserService {
    private static UserService instance;
    private final ApiClient apiClient;
    private User currentUser;
    private String baseUrl = "http://localhost:8080"; // Spring backend URL
    private CloseableHttpClient httpClient;
    
    private UserService() {
        this.apiClient = ApiClient.getInstance();
        this.httpClient = HttpClients.createDefault();
    }
    
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getEmail() : "";
    }
    
    public String getCurrentToken() {
        return "";
    }
    
    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public JsonNode getAllUsers() throws Exception {
        String response = apiClient.get("/users");
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode getUser(Long id) throws Exception {
        String response = apiClient.get("/users/" + id);
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode createUser(Map<String, Object> userData) throws Exception {
        String response = apiClient.post("/users", userData);
        return apiClient.getObjectMapper().readTree(response);
    }

    public JsonNode updateUser(Long id, Map<String, Object> userData) throws Exception {
        String response = apiClient.put("/users/" + id, userData);
        return apiClient.getObjectMapper().readTree(response);
    }

    public boolean updateUser(User user) {
        try {
            if (user == null) {
                System.err.println("Error: Cannot update null user");
                return false;
            }

            System.out.println("Updating user with ID: " + user.getId());
            System.out.println("Email: " + user.getEmail());
            System.out.println("First Name: " + user.getFirstName());
            System.out.println("Last Name: " + user.getLastName());

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("email", user.getEmail());
            updateData.put("firstName", user.getFirstName());
            updateData.put("secondName", user.getLastName());  // Note: backend uses secondName

            String url = "/users/update/" + user.getId();
            System.out.println("Making request to: " + url);
            System.out.println("Request body: " + apiClient.getObjectMapper().writeValueAsString(updateData));

            String response = apiClient.post(url, updateData);
            System.out.println("Response from server: " + response);

            JsonNode jsonNode = apiClient.getObjectMapper().readTree(response);
            boolean success = jsonNode.has("message") && jsonNode.get("message").asText().equals("User updated successfully");
            
            if (success) {
                // Update the current user with the new data
                currentUser.setEmail(user.getEmail());
                currentUser.setFirstName(user.getFirstName());
                currentUser.setLastName(user.getLastName());
                System.out.println("User updated successfully");
            } else {
                System.err.println("Failed to update user. Response message: " + jsonNode.get("message").asText());
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void deleteUser(Long id) throws Exception {
        apiClient.delete("/users/" + id);
    }

    public List<User> findUserByEmail(String email) throws Exception {
        String response = apiClient.get("/users/search?email=" + URLEncoder.encode(email, "UTF-8"));
        JsonNode jsonNode = apiClient.getObjectMapper().readTree(response);
        
        List<User> userList = new ArrayList<>();
        if (jsonNode.isArray()) {
            for (JsonNode node : jsonNode) {
                User user = apiClient.getObjectMapper().treeToValue(node, User.class);
                userList.add(user);
            }
        }
        return userList;
    }

    public boolean login(String email, String password) {
        try {
            String url = "/users/login";
            String requestBody = apiClient.getObjectMapper().writeValueAsString(
                Map.of("email", email, "password", password)
            );
            
            String response = apiClient.post(url, requestBody);
            JsonNode jsonResponse = apiClient.getObjectMapper().readTree(response);
            
            System.out.println("Login response received: " + jsonResponse.toString());
            
            // Check if login was successful by looking for success message
            if (jsonResponse.has("message") && jsonResponse.get("message").asText().equals("Login successful")) {
                User user = new User();
                
                // Set all user information from response
                user.setId(jsonResponse.get("userId").asLong());
                user.setEmail(jsonResponse.get("email").asText());
                user.setFirstName(jsonResponse.get("firstName").asText());
                user.setLastName(jsonResponse.get("secondName").asText()); // Note: backend uses 'secondName'
                
                // Store the user using the setter method
                setCurrentUser(user);
                System.out.println("User logged in and stored in service: " + 
                    user.getFirstName() + " " + user.getLastName() + 
                    " (ID: " + user.getId() + ", Email: " + user.getEmail() + ")");
                return true;
            }
            
            System.out.println("Login failed: " + jsonResponse.get("message").asText());
            return false;
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        try {
            String response = apiClient.post("/users/logout", "");
            JsonNode jsonResponse = apiClient.getObjectMapper().readTree(response);
            
            if (jsonResponse.has("success") && jsonResponse.get("success").asBoolean()) {
                this.currentUser = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.currentUser = null;
        }
    }

    public int getUserTotalBookings() {
        try {
            String url = "/users/bookings/total";
            String response = apiClient.get(url);
            JsonNode jsonNode = apiClient.getObjectMapper().readTree(response);
            return jsonNode.get("total").asInt();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getUserActiveBookings() {
        try {
            String url = "/users/bookings/active";
            String response = apiClient.get(url);
            JsonNode jsonNode = apiClient.getObjectMapper().readTree(response);
            return jsonNode.get("active").asInt();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getUserCompletedTrips() {
        try {
            String url = "/users/bookings/completed";
            String response = apiClient.get(url);
            JsonNode jsonNode = apiClient.getObjectMapper().readTree(response);
            return jsonNode.get("completed").asInt();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean validatePassword(String currentPassword) {
        try {
            if (currentUser == null) {
                return false;
            }

            String url = "/login";
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", currentUser.getEmail());
            requestBody.put("password", currentPassword);

            String response = apiClient.post(url, requestBody);
            JsonNode jsonNode = apiClient.getObjectMapper().readTree(response);
            return jsonNode.has("message") && jsonNode.get("message").asText().equals("Login successful");
        } catch (Exception e) {
            System.err.println("Error validating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String currentPassword, String newPassword) {
        try {
            if (currentUser == null) {
                System.err.println("Error: Cannot update password for null user");
                return false;
            }

            // First validate current password
            if (!validatePassword(currentPassword)) {
                System.err.println("Current password validation failed");
                return false;
            }

            // If validation successful, update password
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("email", currentUser.getEmail());
            updateData.put("firstName", currentUser.getFirstName());
            updateData.put("secondName", currentUser.getLastName());
            updateData.put("password", newPassword);

            String url = "/users/update/" + currentUser.getId();
            System.out.println("Making password update request to: " + url);
            System.out.println("Request body (without password): " + 
                apiClient.getObjectMapper().writeValueAsString(
                    new HashMap<String, Object>() {{
                        put("email", currentUser.getEmail());
                        put("firstName", currentUser.getFirstName());
                        put("secondName", currentUser.getLastName());
                    }}
                )
            );

            String response = apiClient.post(url, updateData);
            System.out.println("Response from server: " + response);

            JsonNode jsonNode = apiClient.getObjectMapper().readTree(response);
            boolean success = jsonNode.has("message") && jsonNode.get("message").asText().equals("User updated successfully");
            
            if (!success) {
                System.err.println("Failed to update password. Response message: " + jsonNode.get("message").asText());
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllStations() {
        try {
            // Get departures
            String departuresResponse = apiClient.get("/schedules/departures");
            JsonNode departuresNode = apiClient.getObjectMapper().readTree(departuresResponse);
            
            // Get destinations
            String destinationsResponse = apiClient.get("/schedules/destinations");
            JsonNode destinationsNode = apiClient.getObjectMapper().readTree(destinationsResponse);
            
            // Use Set to automatically handle duplicates
            Set<String> stations = new HashSet<>();
            
            // Add all departures and destinations
            departuresNode.forEach(node -> stations.add(node.asText()));
            destinationsNode.forEach(node -> stations.add(node.asText()));
            
            // Convert to sorted list
            List<String> stationsList = new ArrayList<>(stations);
            Collections.sort(stationsList);
            return stationsList;
        } catch (Exception e) {
            System.err.println("Error fetching stations: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> getAllDepartures() {
        try {
            String response = apiClient.get("/schedules/departures");
            return apiClient.getObjectMapper().readValue(response, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching departures: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> getDestinationsForDeparture(String departure) {
        try {
            String encodedDeparture = URLEncoder.encode(departure, "UTF-8");
            String response = apiClient.get("/schedules/departures/" + encodedDeparture + "/destinations");
            return apiClient.getObjectMapper().readValue(response, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching destinations: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> searchSchedules(String from, String to, LocalDate date) {
        try {
            String encodedFrom = URLEncoder.encode(from, "UTF-8");
            String encodedTo = URLEncoder.encode(to, "UTF-8");
            
            System.out.println("\n=== Starting Schedule Search ===");
            System.out.println("From: " + from + " (encoded: " + encodedFrom + ")");
            System.out.println("To: " + to + " (encoded: " + encodedTo + ")");
            System.out.println("Date: " + date);
            
            // Convert date to start and end of day
            String startDate = date.toString();
            String endDate = date.toString();
            
            // Get schedules within date range
            String url = String.format("/schedules/departureTime?startDate=%s&endDate=%s", 
                URLEncoder.encode(startDate, "UTF-8"), 
                URLEncoder.encode(endDate, "UTF-8"));
                
            System.out.println("\nMaking API request to: " + url);
            String response = apiClient.get(url);
            
            System.out.println("\nAPI Response received:");
            System.out.println(response);
            
            if (response == null || response.isEmpty()) {
                System.err.println("Empty response from API");
                return new ArrayList<>();
            }
            
            JsonNode schedulesNode = apiClient.getObjectMapper().readTree(response);
            System.out.println("\nParsed " + schedulesNode.size() + " schedules from response");
            
            List<Map<String, Object>> schedules = new ArrayList<>();
            
            schedulesNode.forEach(node -> {
                try {
                    String departureStation = node.get("departure").asText();
                    String destinationStation = node.get("destination").asText();
                    String departureTime = node.get("departureTime").asText();
                    
                    System.out.println("\nProcessing schedule:");
                    System.out.println("- Departure: " + departureStation);
                    System.out.println("- Destination: " + destinationStation);
                    System.out.println("- Departure Time: " + departureTime);
                    
                    // Filter by departure and destination
                    if (departureStation.equals(from) && destinationStation.equals(to)) {
                        Map<String, Object> schedule = new HashMap<>();
                        schedule.put("scheduleId", node.get("id").asText());
                        schedule.put("trainNumber", node.get("train").get("trainNumber") != null ? 
                            node.get("train").get("trainNumber").asText() : 
                            node.get("train").get("trainName").asText());
                        schedule.put("departure", departureStation);
                        schedule.put("destination", destinationStation);
                        schedule.put("departureTime", formatDateTime(LocalDateTime.parse(departureTime)));
                        schedule.put("arrivalTime", formatDateTime(LocalDateTime.parse(node.get("arrivalTime").asText())));
                        schedule.put("cost", node.get("cost").asDouble());
                        // Use train capacity as available seats if availableSeats is not present
                        int availableSeats = node.get("availableSeats") != null ? 
                            node.get("availableSeats").asInt() : 
                            (node.get("available").asBoolean() ? node.get("train").get("capacity").asInt() : 0);
                        schedule.put("availableSeats", availableSeats);
                        schedules.add(schedule);
                        
                        System.out.println("Added schedule to results:");
                        System.out.println(schedule);
                    } else {
                        System.out.println("Schedule does not match search criteria (from=" + from + ", to=" + to + ")");
                    }
                } catch (Exception e) {
                    System.err.println("\nError processing schedule node: " + e.getMessage());
                    System.err.println("Node content: " + node.toString());
                    e.printStackTrace();
                }
            });
            
            System.out.println("\n=== Search Complete ===");
            System.out.println("Found " + schedules.size() + " matching schedules");
            return schedules;
            
        } catch (Exception e) {
            System.err.println("\n=== Search Failed ===");
            System.err.println("Error searching schedules: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    public boolean createBooking(Map<String, Object> bookingData) {
        try {
            BookingService bookingService = BookingService.getInstance();
            
            // First check seat availability
            Integer scheduleId = Integer.valueOf(bookingData.get("scheduleId").toString());
            Integer numSeats = Integer.valueOf(bookingData.get("numSeats").toString());
            
            JsonNode availabilityCheck = bookingService.checkAvailability(scheduleId, numSeats);
            if (!availabilityCheck.get("available").asBoolean()) {
                throw new RuntimeException("Selected seats are no longer available");
            }
            
            // Create a properly structured booking request
            Map<String, Object> request = new HashMap<>();
            request.put("bookingTime", bookingData.get("bookingTime"));
            request.put("numberOfSeats", numSeats);
            request.put("totalCost", bookingData.get("totalCost"));
            
            // Create user object
            Map<String, Object> user = new HashMap<>();
            user.put("id", bookingData.get("userId"));
            request.put("user", user);
            
            // Create schedule object
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("id", scheduleId);
            request.put("schedule", schedule);
            
            ObjectMapper mapper = apiClient.getObjectMapper();
            JsonNode bookingNode = mapper.valueToTree(request);
            JsonNode response = bookingService.createBooking(bookingNode);
            return response != null && response.has("id");
        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getUserBookings() throws Exception {
        if (currentUser == null || currentUser.getId() == null) {
            throw new Exception("No user logged in");
        }
        try {
            String response = apiClient.get("/bookings/user/" + currentUser.getId());
            return apiClient.getObjectMapper().readValue(response, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching user bookings: " + e.getMessage());
            throw new Exception("Failed to fetch user bookings", e);
        }
    }

    public int getTotalBookings() {
        try {
            if (currentUser == null) {
                System.err.println("Cannot get bookings for null user");
                return 0;
            }

            String url = "/bookings/user/" + currentUser.getId();
            String response = apiClient.get(url);
            JsonNode jsonNode = apiClient.getObjectMapper().readTree(response);

            if (jsonNode.isArray()) {
                return jsonNode.size();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error getting total bookings: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public int getActiveBookings() {
        try {
            if (currentUser == null) {
                System.err.println("Cannot get active bookings for null user");
                return 0;
            }

            String url = "/bookings/user/" + currentUser.getId() + "/active";
            String response = apiClient.get(url);
            JsonNode jsonNode = apiClient.getObjectMapper().readTree(response);

            if (jsonNode.isArray()) {
                return jsonNode.size();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error getting active bookings: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
