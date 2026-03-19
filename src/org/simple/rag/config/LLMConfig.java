package org.simple.rag.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

/**
 * Configuration for the LLM (Language Model) Service.
 * 
 * Loads settings from three sources (in order of precedence):
 * 1. Environment variables (LLM_*, e.g., LLM_ENDPOINT)
 * 2. Property file (llm.properties)
 * 3. Default hardcoded values
 * 
 * All values are validated on load to ensure correctness.
 */
public class LLMConfig {

    private static final String CONFIG_FILE = "llm.properties";
    private static final String DEFAULT_ENDPOINT = "http://100.77.159.56:11434";
    private static final String DEFAULT_MODEL = "gemma3:4b";
    private static final int DEFAULT_TIMEOUT_SECONDS = 120;
    private static final int MIN_TIMEOUT = 10;
    private static final int MAX_TIMEOUT = 600; // 10 minutes

    private final String endpoint;
    private final String model;
    private final int timeoutSeconds;
    private final boolean enabled;

    /**
     * Create a new LLMConfig by loading from properties and environment.
     */
    public LLMConfig() {
        Properties props = loadPropertiesFile();

        this.endpoint = validateEndpoint(getProperty(props, "llm.endpoint",
                System.getenv("LLM_ENDPOINT"), DEFAULT_ENDPOINT));
        this.model = validateModel(getProperty(props, "llm.model",
                System.getenv("LLM_MODEL"), DEFAULT_MODEL));
        this.timeoutSeconds = validateTimeout(Integer.parseInt(getProperty(props, "llm.timeout",
                System.getenv("LLM_TIMEOUT"), String.valueOf(DEFAULT_TIMEOUT_SECONDS))));
        this.enabled = Boolean.parseBoolean(getProperty(props, "llm.enabled",
                System.getenv("LLM_ENABLED"), "true"));
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
                System.err.println("[WARN] Could not read LLM config file: " + e.getMessage());
            }
        }

        return props;
    }

    /**
     * Get a property value from environment or properties file.
     */
    private String getProperty(Properties props, String key, String envValue, String defaultValue) {
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        String propValue = props.getProperty(key);
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
     * Get the LLM API endpoint URL.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Get the model name.
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
     * Check if the LLM service is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "LLMConfig{" +
                "endpoint='" + endpoint + '\'' +
                ", model='" + model + '\'' +
                ", timeoutSeconds=" + timeoutSeconds +
                ", enabled=" + enabled +
                '}';
    }
}
