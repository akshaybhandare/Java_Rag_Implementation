package org.simple.rag.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

/**
 * Configuration for the Embedding Service.
 * 
 * Loads settings from three sources (in order of precedence):
 * 1. Environment variables (EMBEDDING_*, e.g., EMBEDDING_ENDPOINT)
 * 2. Property file (embedding.properties)
 * 3. Default hardcoded values
 * 
 * All values are validated on load to ensure correctness.
 */
public class EmbeddingConfig {

    private static final String CONFIG_FILE = "embedding.properties";
    private static final String DEFAULT_ENDPOINT = "http://192.168.10.11:1234/v1/embeddings";
    private static final String DEFAULT_MODEL = "text-embedding-mxbai-embed-large-v1";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
    private static final int MIN_TIMEOUT = 1;
    private static final int MAX_TIMEOUT = 300; // 5 minutes

    private final String endpoint;
    private final String model;
    private final int timeoutSeconds;
    private final int connectTimeoutSeconds;
    private final boolean enabled;

    /**
     * Create a new EmbeddingConfig by loading from properties and environment.
     */
    public EmbeddingConfig() {
        Properties props = loadPropertiesFile();
        
        this.endpoint = validateEndpoint(getProperty(props, "embedding.endpoint",
                System.getenv("EMBEDDING_ENDPOINT"), DEFAULT_ENDPOINT));
        this.model = validateModel(getProperty(props, "embedding.model",
                System.getenv("EMBEDDING_MODEL"), DEFAULT_MODEL));
        this.timeoutSeconds = validateTimeout(Integer.parseInt(getProperty(props, "embedding.timeout",
                System.getenv("EMBEDDING_TIMEOUT"), String.valueOf(DEFAULT_TIMEOUT_SECONDS))));
        this.connectTimeoutSeconds = validateTimeout(Integer.parseInt(getProperty(props, "embedding.connect.timeout",
                System.getenv("EMBEDDING_CONNECT_TIMEOUT"), String.valueOf(DEFAULT_CONNECT_TIMEOUT_SECONDS))));
        this.enabled = Boolean.parseBoolean(getProperty(props, "embedding.enabled",
                System.getenv("EMBEDDING_ENABLED"), "true"));
    }

    /**
     * Load properties from the configuration file if it exists.
     */
    private Properties loadPropertiesFile() {
        Properties props = new Properties();
        Path configPath = Paths.get(CONFIG_FILE);
        
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("[WARN] Could not read config file: " + e.getMessage());
            }
        }
        
        return props;
    }

    /**
     * Get a property value from environment or properties file.
     */
    private String getProperty(Properties props, String propKey, String envValue, String defaultValue) {
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        String propValue = props.getProperty(propKey);
        return propValue != null ? propValue : defaultValue;
    }

    /**
     * Validate endpoint URL format.
     */
    private String validateEndpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "Endpoint cannot be null");
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            throw new IllegalArgumentException("Endpoint must start with http:// or https://");
        }
        return endpoint;
    }

    /**
     * Validate model name is not empty.
     */
    private String validateModel(String model) {
        Objects.requireNonNull(model, "Model cannot be null");
        if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be empty");
        }
        return model;
    }

    /**
     * Validate timeout is within acceptable range.
     */
    private int validateTimeout(int timeout) {
        if (timeout < MIN_TIMEOUT || timeout > MAX_TIMEOUT) {
            throw new IllegalArgumentException("Timeout must be between " + MIN_TIMEOUT +
                    " and " + MAX_TIMEOUT + " seconds, got: " + timeout);
        }
        return timeout;
    }

    // ========== Accessors ==========

    /**
     * Get the embedding API endpoint URL.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Get the embedding model name.
     */
    public String getModel() {
        return model;
    }

    /**
     * Get the request timeout in seconds.
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * Get the connection timeout in seconds.
     */
    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    /**
     * Check if the embedding service is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "EmbeddingConfig{" +
                "endpoint='" + endpoint + '\'' +
                ", model='" + model + '\'' +
                ", timeoutSeconds=" + timeoutSeconds +
                ", connectTimeoutSeconds=" + connectTimeoutSeconds +
                ", enabled=" + enabled +
                '}';
    }
}
