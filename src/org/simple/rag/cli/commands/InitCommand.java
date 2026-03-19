package org.simple.rag.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.IOException;
import org.simple.rag.Logger;
import org.simple.rag.cli.ConfigManager;
import org.simple.rag.cli.FolderManager;

/**
 * Init command - Initializes the application with default configuration.
 * Creates necessary folders and config files.
 */
@Command(
    name = "init",
    description = "Initialize the RAG application with default configuration"
)
public class InitCommand implements Runnable {
    
    private static final Logger logger = Logger.getInstance();
    
    @Option(names = {"-e", "--embedding-endpoint"}, description = "Embedding service endpoint") 
    String embeddingEndpoint;
    
    @Option(names = {"-m", "--embedding-model"}, description = "Embedding model name")
    String embeddingModel;
    
    @Option(names = {"-l", "--llm-endpoint"}, description = "LLM service endpoint")
    String llmEndpoint;
    
    @Option(names = {"--llm-model"}, description = "LLM model name")
    String llmModel;
    
    @Override
    public void run() {
        try {
            logger.info("\n========================================");
            logger.info("Initializing Simple RAG Application");
            logger.info("========================================\n");
            
            // Create folder structure
            logger.info("Setting up folder structure...");
            FolderManager.initializeDirectories();
            
            // Initialize and update configuration
            logger.info("\nSetting up configuration files...");
            ConfigManager.initializeConfig();
            if (embeddingEndpoint != null || embeddingModel != null) {
                ConfigManager.updateEmbeddingConfig(embeddingEndpoint, embeddingModel);
            }
            if (llmEndpoint != null || llmModel != null) {
                ConfigManager.updateLLMConfig(llmEndpoint, llmModel, null);
            }
            
            // Display information
            logger.info("\n");
            FolderManager.displayFolderStructure();
            ConfigManager.displayConfig();
            
            logger.info("✓ Initialization complete!\n");
            logger.info("Next steps:");
            logger.info("  1. Place your PDF/TXT files in: " + FolderManager.getDocumentsDir());
            logger.info("  2. Run: rag ingest");
            logger.info("  3. Run: rag ask \"Your question here\"\n");
            
        } catch (IOException e) {
            logger.error("Initialization failed: " + e.getMessage(), e);
            System.exit(1);
        }
    }
}
