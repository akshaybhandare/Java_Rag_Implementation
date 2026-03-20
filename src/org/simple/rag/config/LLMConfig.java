package org.simple.rag.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

public class LLMConfig {

    private static final String CONFIG_FILE = "llm.properties";
    private static final String DEFAULT_ENDPOINT = "http://100.77.159.56:11434";
    private static final String DEFAULT_MODEL = "gemma3:4b";
    private static final int DEFAULT_TIMEOUT_SECONDS = 120;
    private static final int MIN_TIMEOUT = 10;
    private static final int MAX_TIMEOUT = 600;

    private final String endpoint;
    private final String model;
    private final int timeoutSeconds;
    private final boolean enabled;

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

    private String getProperty(Properties props, String key, String envValue, String defaultValue) {
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        String propValue = props.getProperty(key);
        return propValue != null ? propValue : defaultValue;
    }

    private String validateEndpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "Endpoint cannot be null");
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            throw new IllegalArgumentException("Endpoint must start with http:// or https://");
        }
        return endpoint;
    }

    private String validateModel(String model) {
        Objects.requireNonNull(model, "Model cannot be null");
        if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be empty");
        }
        return model;
    }

    private int validateTimeout(int timeout) {
        if (timeout < MIN_TIMEOUT || timeout > MAX_TIMEOUT) {
            throw new IllegalArgumentException("Timeout must be between " + MIN_TIMEOUT +
                    " and " + MAX_TIMEOUT + " seconds, got: " + timeout);
        }
        return timeout;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getModel() {
        return model;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

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
