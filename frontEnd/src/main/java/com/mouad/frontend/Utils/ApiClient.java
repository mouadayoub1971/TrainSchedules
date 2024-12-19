package com.mouad.frontend.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class ApiClient implements AutoCloseable {
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private static ApiClient instance;

    private ApiClient() {
        this.baseUrl = "http://localhost:8080";
        this.objectMapper = new ObjectMapper();
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    public <T> T post(String endpoint, Object requestBody, Class<T> responseType) throws Exception {
        try (CloseableHttpClient client = getHttpClient()) {
            // Remove leading slash if present to avoid double slashes
            endpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
            String url = baseUrl + "/" + endpoint;
            
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                int statusCode = response.getCode();

                if (statusCode >= 200 && statusCode < 300) {
                    return objectMapper.readValue(responseBody, responseType);
                } else {
                    throw new RuntimeException("API call failed with status code: " + statusCode + ", response: " + responseBody);
                }
            }
        }
    }

    public String post(String endpoint, Object requestBody) throws Exception {
        try (CloseableHttpClient client = getHttpClient()) {
            // Remove leading slash if present to avoid double slashes
            endpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
            String url = baseUrl + "/" + endpoint;
            
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            System.out.println("Making POST request to: " + baseUrl + endpoint);
            System.out.println("Request body: " + jsonBody);
            
            StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                int statusCode = response.getCode();
                
                System.out.println("Response status code: " + statusCode);
                System.out.println("Response body: " + responseBody);

                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException(responseBody);
                }
            }
        }
    }

    public String get(String endpoint) throws Exception {
        try (CloseableHttpClient client = getHttpClient()) {
            HttpGet httpGet = new HttpGet(baseUrl + endpoint);
            httpGet.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                int statusCode = response.getCode();

                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException("API call failed with status code: " + statusCode + ", response: " + responseBody);
                }
            }
        }
    }

    public String put(String endpoint, Object requestBody) throws Exception {
        try (CloseableHttpClient client = getHttpClient()) {
            // Remove leading slash if present to avoid double slashes
            endpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
            String url = baseUrl + "/" + endpoint;
            
            HttpPut httpPut = new HttpPut(url);
            httpPut.setHeader("Content-Type", "application/json");

            if (requestBody != null) {
                String jsonBody = objectMapper.writeValueAsString(requestBody);
                StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
                httpPut.setEntity(entity);
            }

            try (CloseableHttpResponse response = client.execute(httpPut)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                int statusCode = response.getCode();

                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException("API call failed with status code: " + statusCode + ", response: " + responseBody);
                }
            }
        }
    }

    public String delete(String endpoint) throws Exception {
        try (CloseableHttpClient client = getHttpClient()) {
            // Remove leading slash if present to avoid double slashes
            endpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
            String url = baseUrl + "/" + endpoint;
            
            System.out.println("Making DELETE request to: " + url);
            
            HttpDelete httpDelete = new HttpDelete(url);
            httpDelete.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(httpDelete)) {
                int statusCode = response.getCode();
                System.out.println("Response status code: " + statusCode);
                
                if (statusCode >= 200 && statusCode < 300) {
                    // For 204 No Content, return empty string
                    if (statusCode == 204 || response.getEntity() == null) {
                        return "";
                    }
                    
                    String responseBody = EntityUtils.toString(response.getEntity());
                    System.out.println("Response body: " + responseBody);
                    return responseBody;
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    System.out.println("Error response body: " + responseBody);
                    throw new RuntimeException(responseBody);
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        // No need to close the HTTP client as it's created for each request
    }
}
