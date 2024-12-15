package com.mouad.frontend.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mouad.frontend.Utils.ApiClient;
import java.util.Map;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UserService {
    private static UserService instance;
    private final ApiClient apiClient;
    private JsonNode currentUser;

    private UserService() {
        this.apiClient = ApiClient.getInstance();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public JsonNode getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(JsonNode user) {
        this.currentUser = user;
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

    public void deleteUser(Long id) throws Exception {
        apiClient.delete("/api/users/" + id);
    }

    public JsonNode findUserByEmail(String email) throws Exception {
        // Get all users and find by email since there's no direct endpoint
        JsonNode users = getAllUsers();
        if (users != null && users.isArray()) {
            for (JsonNode user : users) {
                if (user.has("email") && email.equals(user.get("email").asText())) {
                    return user;
                }
            }
        }
        return null;
    }

    public JsonNode login(String email, String password) throws Exception {
        ObjectNode loginRequest = apiClient.getObjectMapper().createObjectNode();
        loginRequest.put("email", email);
        loginRequest.put("password", password);
        
        String response = apiClient.post("/login", loginRequest);
        JsonNode user = apiClient.getObjectMapper().readTree(response);
        setCurrentUser(user);
        return user;
    }

    public void logout() {
        this.currentUser = null;
    }
}
