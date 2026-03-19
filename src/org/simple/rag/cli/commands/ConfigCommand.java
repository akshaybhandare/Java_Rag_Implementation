package org.simple.rag.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.IOException;
import org.simple.rag.Logger;
import org.simple.rag.cli.ConfigManager;

/**
 * Config command - Manage application configuration.
 */
@Command(
    name = "config",
    description = "Manage application configuration"
)
public class ConfigCommand implements Runnable {
    
    private static final Logger logger = Logger.getInstance();
    
    @Option(names = {"--show"}, description = "Show current configuration")
    boolean showConfig;
    
    @Option(names = {"--embedding-endpoint"}, description = "Set embedding service endpoint",
            paramLabel = "URL")
    String embeddingEndpoint;
    
    @Option(names = {"--embedding-model"}, description = "Set embedding model name",
            paramLabel = "MODEL")
    String embeddingModel;
    
    @Option(names = {"--llm-endpoint"}, description = "Set LLM service endpoint",
            paramLabel = "URL")
    String llmEndpoint;
    
    @Option(names = {"--llm-model"}, description = "Set LLM model name",
            paramLabel = "MODEL")
    String llmModel;
    
    @Option(names = {"--llm-timeout"}, description = "Set LLM timeout in seconds",
            paramLabel = "SECONDS")
    String llmTimeout;
    
    @Override
    public void run() {
        try {
            if (showConfig) {
                ConfigManager.displayConfig();
            } else if (embeddingEndpoint != null || embeddingModel != null) {
                logger.info("Updating embedding configuration...");
                ConfigManager.updateEmbeddingConfig(embeddingEndpoint, embeddingModel);
                logger.info("✓ Embedding configuration updated\n");
            } else if (llmEndpoint != null || llmModel != null || llmTimeout != null) {
                logger.info("Updating LLM configuration...");
                ConfigManager.updateLLMConfig(llmEndpoint, llmModel, llmTimeout);
                logger.info("✓ LLM configuration updated\n");
            } else {
                // Default to showing config
                ConfigManager.displayConfig();
            }
        } catch (IOException e) {
            logger.error("Configuration error: " + e.getMessage(), e);
            System.exit(1);
        }
    }
}
