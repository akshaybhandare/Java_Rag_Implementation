package org.simple.rag.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import org.simple.rag.Logger;

/**
 * ConfigManager handles reading, writing, and validating application configuration.
 * Supports easy CLI-based configuration updates.
 */
public class ConfigManager {
    
    private static final Logger logger = Logger.getInstance();
    private static final String CONFIG_DIR = "./config";
    private static final String EMBEDDING_CONFIG = CONFIG_DIR + "/embedding.properties";
    private static final String LLM_CONFIG = CONFIG_DIR + "/llm.properties";
    
    public static void initializeConfig() throws IOException {
        // Create config directory if it doesn't exist
        File configDirectory = new File(CONFIG_DIR);
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
            logger.info("Created config directory at: " + CONFIG_DIR);
        }
        
        // Create default embedding config if it doesn't exist
        if (!new File(EMBEDDING_CONFIG).exists()) {
            createDefaultEmbeddingConfig();
        }
        
        // Create default LLM config if it doesn't exist
        if (!new File(LLM_CONFIG).exists()) {
            createDefaultLLMConfig();
        }
    }
    
    private static void createDefaultEmbeddingConfig() throws IOException {
        Properties props = new Properties();
        props.setProperty("embedding.endpoint", "http://localhost:1234/v1/embeddings");
        props.setProperty("embedding.model", "text-embedding-mxbai-embed-large-v1");
        props.setProperty("embedding.timeout", "30");
        props.setProperty("embedding.connect.timeout", "10");
        props.setProperty("embedding.enabled", "true");
        
        try (FileOutputStream fos = new FileOutputStream(EMBEDDING_CONFIG)) {
            props.store(fos, "Embedding Service Configuration\n# Update endpoints and model names below");
        }
        logger.info("Created default embedding.properties at: " + EMBEDDING_CONFIG);
    }
    
    private static void createDefaultLLMConfig() throws IOException {
        Properties props = new Properties();
        props.setProperty("llm.endpoint", "http://localhost:11434");
        props.setProperty("llm.model", "mistral:7b");
        props.setProperty("llm.timeout", "120");
        props.setProperty("llm.enabled", "true");
        
        try (FileOutputStream fos = new FileOutputStream(LLM_CONFIG)) {
            props.store(fos, "LLM Service Configuration\n# Update endpoints and model names below");
        }
        logger.info("Created default llm.properties at: " + LLM_CONFIG);
    }
    
    public static void updateEmbeddingConfig(String endpoint, String model) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(EMBEDDING_CONFIG));
        
        if (endpoint != null && !endpoint.isEmpty()) {
            props.setProperty("embedding.endpoint", endpoint);
            logger.info("Updated embedding endpoint: " + endpoint);
        }
        if (model != null && !model.isEmpty()) {
            props.setProperty("embedding.model", model);
            logger.info("Updated embedding model: " + model);
        }
        
        try (FileOutputStream fos = new FileOutputStream(EMBEDDING_CONFIG)) {
            props.store(fos, "Embedding Service Configuration");
        }
        logger.info("Embedding configuration saved");
    }
    
    public static void updateLLMConfig(String endpoint, String model, String timeout) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(LLM_CONFIG));
        
        if (endpoint != null && !endpoint.isEmpty()) {
            props.setProperty("llm.endpoint", endpoint);
            logger.info("Updated LLM endpoint: " + endpoint);
        }
        if (model != null && !model.isEmpty()) {
            props.setProperty("llm.model", model);
            logger.info("Updated LLM model: " + model);
        }
        if (timeout != null && !timeout.isEmpty()) {
            props.setProperty("llm.timeout", timeout);
            logger.info("Updated LLM timeout: " + timeout);
        }
        
        try (FileOutputStream fos = new FileOutputStream(LLM_CONFIG)) {
            props.store(fos, "LLM Service Configuration");
        }
        logger.info("LLM configuration saved");
    }
    
    public static void displayConfig() throws IOException {
        logger.info("\n========== CURRENT CONFIGURATION ==========\n");
        
        logger.info("--- Embedding Service ---");
        Properties embeddingProps = new Properties();
        embeddingProps.load(new FileInputStream(EMBEDDING_CONFIG));
        embeddingProps.forEach((key, value) -> 
            logger.info(key + ": " + value)
        );
        
        logger.info("\n--- LLM Service ---");
        Properties llmProps = new Properties();
        llmProps.load(new FileInputStream(LLM_CONFIG));
        llmProps.forEach((key, value) -> 
            logger.info(key + ": " + value)
        );
        
        logger.info("\n==========================================\n");
    }
    
    public static String getEmbeddingConfigPath() {
        return EMBEDDING_CONFIG;
    }
    
    public static String getLLMConfigPath() {
        return LLM_CONFIG;
    }
    
    public static boolean configExists() {
        return new File(EMBEDDING_CONFIG).exists() && new File(LLM_CONFIG).exists();
    }
}
