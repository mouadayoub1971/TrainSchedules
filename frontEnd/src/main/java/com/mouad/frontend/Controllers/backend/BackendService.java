package com.mouad.frontend.Controllers.backend;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BackendService {
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final ObjectMapper objectMapper = new ObjectMapper(); // For JSON parsing

    // Example GET request
    public static String fetchData(String endpoint) throws Exception {
        String url = BackendConfig.getBaseUrl() + endpoint;
        HttpGet request = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() == 200) {
                return new String(response.getEntity().getContent().readAllBytes());
            } else {
                throw new RuntimeException("Failed to fetch data. HTTP Code: " + response.getCode());
            }
        }
    }

    // Example POST request
    public static String postData(String endpoint, Object payload) throws Exception {
        String url = BackendConfig.getBaseUrl() + endpoint;
        HttpPost request = new HttpPost(url);
        String json = objectMapper.writeValueAsString(payload);
        request.setEntity(new StringEntity(json));
        request.setHeader("Content-Type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() == 200 || response.getCode() == 201) {
                return new String(response.getEntity().getContent().readAllBytes());
            } else {
                throw new RuntimeException("Failed to post data. HTTP Code: " + response.getCode());
            }
        }
    }
}

