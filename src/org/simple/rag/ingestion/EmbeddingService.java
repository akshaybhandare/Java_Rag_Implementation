package org.simple.rag.ingestion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
import org.simple.rag.Activator;
import org.simple.rag.config.EmbeddingConfig;

/**
 * Embedding Service for generating text embeddings via remote API.
 * 
 * Converts text into dense vector representations that capture semantic meaning.
 * These embeddings are essential for the RAG pipeline's similarity search.
 * 
 * Features:
 * - Automatic retry logic with exponential backoff
 * - Support for batch and async embedding generation
 * - Health checks and diagnostics
 * - Configurable timeouts and endpoints
 * 
 * Thread-safe for concurrent requests.
 */
public class EmbeddingService {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final EmbeddingConfig config;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /**
     * Create an embedding service with default configuration.
     */
    public EmbeddingService() {
        this.config = new EmbeddingConfig();
    }

    /**
     * Create an embedding service with custom configuration.
     * 
     * @param config The embedding configuration
     * @throws NullPointerException if config is null
     */
    public EmbeddingService(EmbeddingConfig config) {
        this.config = Objects.requireNonNull(config, "EmbeddingConfig cannot be null");
    }

    /**
     * Generate embedding for a single text chunk with automatic retry logic.
     * 
     * Retries up to MAX_RETRIES times with exponential backoff on failure.
     * 
     * @param text The text to embed
     * @return Float array representing the embedding vector
     * @throws IOException if all retries fail
     * @throws InterruptedException if interrupted during request
     * @throws IllegalArgumentException if text is null or empty
     */
    public float[] generateEmbedding(String text) throws IOException, InterruptedException {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        if (!config.isEnabled()) {
            Activator.getDefault().getLog().warn("Embedding service is disabled");
            throw new IOException("Embedding service is disabled");
        }

        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return generateEmbeddingInternal(text);
            } catch (IOException e) {
                lastException = e;
                Activator.getDefault().getLog().warn(
                        "Embedding request attempt " + attempt + "/" + MAX_RETRIES + " failed: " + e.getMessage());

                if (attempt < MAX_RETRIES) {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                }
            }
        }

        // If all retries fail, throw the last exception
        throw lastException;
    }

    /**
     * Internal method to generate embedding (single attempt, no retry).
     */
    private float[] generateEmbeddingInternal(String text) throws IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", config.getModel());
        requestBody.put("input", text);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getEndpoint()))
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        long startTime = System.currentTimeMillis();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long elapsed = System.currentTimeMillis() - startTime;
            Activator.getDefault().getLog().info("Embedding request completed in " + elapsed + "ms");

            if (response.statusCode() != 200) {
                throw new IOException("Embedding service returned status " + response.statusCode() +
                        ": " + response.body());
            }

            return parseEmbeddingResponse(response.body());
        } catch (InterruptedException e) {
            Activator.getDefault().getLog().error("Embedding request interrupted after " +
                    (System.currentTimeMillis() - startTime) + "ms");
            throw e;
        }
    }

    /**
     * Generate embeddings for multiple text chunks sequentially.
     * 
     * @param texts List of texts to embed
     * @return List of embedding vectors in the same order as input texts
     * @throws IOException if embedding fails
     * @throws InterruptedException if interrupted
     */
    public List<float[]> generateEmbeddings(List<String> texts) throws IOException, InterruptedException {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        List<float[]> embeddings = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            try {
                embeddings.add(generateEmbedding(texts.get(i)));
            } catch (Exception e) {
                Activator.getDefault().getLog().warn("Failed to embed chunk " + i + ": " + e.getMessage());
                throw e;
            }
        }

        return embeddings;
    }

    /**
     * Generate embeddings for multiple text chunks asynchronously.
     * 
     * Offers better performance for large batches by processing in parallel.
     * 
     * @param texts List of texts to embed
     * @return List of embedding vectors in the same order as input texts
     * @throws InterruptedException if interrupted while waiting for completion
     * @throws RuntimeException wrapping any embedding generation errors
     */
    public List<float[]> generateEmbeddingsAsync(List<String> texts) throws InterruptedException {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        List<float[]> embeddings = new ArrayList<>();
        List<CompletableFuture<float[]>> futures = new ArrayList<>();

        for (String text : texts) {
            CompletableFuture<float[]> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return generateEmbedding(text);
                } catch (Exception e) {
                    Activator.getDefault().getLog().error("Async embedding failed: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }

        // Wait for all to complete
        for (CompletableFuture<float[]> future : futures) {
            embeddings.add(future.join());
        }

        return embeddings;
    }

    /**
     * Parse the embedding response from the API.
     */
    private float[] parseEmbeddingResponse(String response) {
        JSONObject json = new JSONObject(response);
        JSONArray data = json.getJSONArray("data");
        JSONObject embedding = data.getJSONObject(0);
        JSONArray vector = embedding.getJSONArray("embedding");

        float[] result = new float[vector.length()];
        for (int i = 0; i < vector.length(); i++) {
            result[i] = (float) vector.getDouble(i);
        }

        return result;
    }

    /**
     * Check if the embedding service is reachable and responsive.
     * 
     * @return true if service responds to a test request, false otherwise
     */
    public boolean isHealthy() {
        try {
            float[] result = generateEmbeddingInternal("health check");
            return result != null && result.length > 0;
        } catch (Exception e) {
            Activator.getDefault().getLog().error("Health check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get diagnostic information about the service.
     * 
     * @return Diagnostic information string
     */
    public String getDiagnostics() {
        StringBuilder diag = new StringBuilder();
        diag.append("=== Embedding Service Diagnostics ===\n");
        diag.append("Endpoint: ").append(config.getEndpoint()).append("\n");
        diag.append("Model: ").append(config.getModel()).append("\n");
        diag.append("Connection Timeout: ").append(config.getConnectTimeoutSeconds()).append("s\n");
        diag.append("Request Timeout: ").append(config.getTimeoutSeconds()).append("s\n");
        diag.append("Enabled: ").append(config.isEnabled()).append("\n");

        long start = System.currentTimeMillis();
        boolean healthy = isHealthy();
        long elapsed = System.currentTimeMillis() - start;

        diag.append("Service Status: ").append(healthy ? "OK" : "FAILED").append("\n");
        diag.append("Health Check Time: ").append(elapsed).append("ms\n");

        return diag.toString();
    }

    // ========== Configuration Accessors ==========

    public String getEmbeddingModel() {
        return config.getModel();
    }

    public String getEmbeddingEndpoint() {
        return config.getEndpoint();
    }

    public EmbeddingConfig getConfig() {
        return config;
    }
}
