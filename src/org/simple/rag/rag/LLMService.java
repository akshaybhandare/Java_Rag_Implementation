package org.simple.rag.rag;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

import org.json.JSONObject;
import org.simple.rag.config.LLMConfig;

/**
 * LLM Service for interacting with language models via Ollama API.
 * 
 * Handles HTTP communication with the Ollama server and manages
 * model inference with configurable timeouts and endpoints.
 * 
 * Thread-safe for concurrent requests.
 */
public class LLMService {

    private LLMConfig config;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /**
     * Create LLM service with custom configuration.
     * 
     * @param config The LLM configuration
     * @throws NullPointerException if config is null
     */
    public LLMService(LLMConfig config) {
        this.config = Objects.requireNonNull(config, "LLMConfig cannot be null");
    }

    /**
     * Create LLM service with default configuration.
     */
    public LLMService() {
        this.config = new LLMConfig();
    }

    /**
     * Generate response from LLM for the given prompt.
     * 
     * @param prompt The prompt to send to the LLM
     * @return The generated response text
     * @throws IOException if the request fails or service is disabled
     * @throws InterruptedException if the request is interrupted
     */
    public String generate(String prompt) throws IOException, InterruptedException {
        if (!config.isEnabled()) {
            throw new IOException("LLM service is disabled");
        }

        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", config.getModel());
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getEndpoint() + "/api/generate"))
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("LLM service returned status " + response.statusCode() +
                        ": " + response.body());
            }

            JSONObject json = new JSONObject(response.body());
            return json.getString("response");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    /**
     * Check if LLM service is reachable and healthy.
     * 
     * @return true if service responds to health check, false otherwise
     */
    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getEndpoint() + "/api/tags"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get diagnostic information about the LLM service.
     * 
     * @return Diagnostic information string
     */
    public String getDiagnostics() {
        StringBuilder sb = new StringBuilder();
        sb.append("LLM Service Diagnostics:\n");
        sb.append("- Endpoint: ").append(config.getEndpoint()).append("\n");
        sb.append("- Model: ").append(config.getModel()).append("\n");
        sb.append("- Enabled: ").append(config.isEnabled()).append("\n");
        sb.append("- Timeout: ").append(config.getTimeoutSeconds()).append("s\n");
        sb.append("- Healthy: ").append(isHealthy()).append("\n");
        return sb.toString();
    }

    // ========== Configuration Accessors ==========

    /**
     * Get the current configuration.
     * 
     * @return The LLMConfig
     */
    public LLMConfig getConfig() {
        return config;
    }

    /**
     * Set a new configuration.
     * 
     * @param config The new LLMConfig
     * @throws NullPointerException if config is null
     */
    public void setConfig(LLMConfig config) {
        this.config = Objects.requireNonNull(config, "Config cannot be null");
    }
}
