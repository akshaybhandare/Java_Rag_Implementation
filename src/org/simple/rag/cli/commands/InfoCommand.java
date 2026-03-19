package org.simple.rag.cli.commands;

import picocli.CommandLine.Command;
import org.simple.rag.Logger;
import org.simple.rag.cli.FolderManager;

/**
 * Info command - Display application information and folder structure.
 */
@Command(
    name = "info",
    description = "Display application information and folder structure"
)
public class InfoCommand implements Runnable {
    
    private static final Logger logger = Logger.getInstance();
    
    @Override
    public void run() {
        logger.info("\n========================================");
        logger.info("Simple RAG Application - v1.0.0");
        logger.info("========================================\n");
        
        logger.info("A Retrieval-Augmented Generation (RAG) application for");
        logger.info("document-based question answering.\n");
        
        logger.info("Quick Start:");
        logger.info("  1. rag init                          <- Initialize (run once)");
        logger.info("  2. Place PDFs in ./data/documents");
        logger.info("  3. rag ingest                        <- Ingest documents");
        logger.info("  4. rag ask \"Your question\"           <- Ask questions\n");
        
        logger.info("Other Commands:");
        logger.info("  rag config --show                    <- Show configuration");
        logger.info("  rag config --llm-endpoint URL        <- Set LLM endpoint");
        logger.info("  rag config --embedding-endpoint URL  <- Set embedding endpoint\n");
        
        FolderManager.displayFolderStructure();
        FolderManager.printDocumentsInDir();
    }
}
