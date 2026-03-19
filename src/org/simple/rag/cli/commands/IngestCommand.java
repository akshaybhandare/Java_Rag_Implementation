package org.simple.rag.cli.commands;

import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.simple.rag.Logger;
import org.simple.rag.cli.FolderManager;
import org.simple.rag.config.EmbeddingConfig;
import org.simple.rag.ingestion.EmbeddedChunk;
import org.simple.rag.ingestion.PDFFileIngester;

/**
 * Ingest command - Ingests documents from the documents folder and generates embeddings.
 */
@Command(
    name = "ingest",
    description = "Ingest documents from ./data/documents folder"
)
public class IngestCommand implements Runnable {
    
    private static final Logger logger = Logger.getInstance();
    
    @Option(names = {"-f", "--folder"}, description = "Folder containing documents to ingest",
            defaultValue = "./data/documents")
    String docFolder;
    
    @Override
    public void run() {
        try {
            logger.info("\n========================================");
            logger.info("Starting Document Ingestion");
            logger.info("========================================\n");
            
            logger.info("Documents folder: " + docFolder);
            
            // Count and display documents
            FolderManager.printDocumentsInDir();
            logger.info("");
            
            int docCount = FolderManager.countDocumentsInDir();
            if (docCount == 0) {
                logger.warn("No documents found to ingest!");
                logger.info("Please add PDF or TXT files to: " + docFolder);
                System.exit(1);
            }
            
            // Initialize ingester
            logger.info("Initializing embedding service...");
            EmbeddingConfig embeddingConfig = new EmbeddingConfig();
            PDFFileIngester ingester = new PDFFileIngester();
            logger.info("✓ Embedding service initialized\n");
            
            // Process documents and save embeddings
            logger.info("Processing documents...");
            List<EmbeddedChunk> chunks = ingester.processAndGetChunks();
            if (chunks != null && !chunks.isEmpty()) {
                FolderManager.saveEmbeddedChunks(chunks, "knowledge_base");
                logger.info("✓ Saved " + chunks.size() + " embedded chunks to vector store");
            } else {
                logger.warn("No chunks were generated from the documents");
            }
            
            logger.info("\n========================================");
            logger.info("✓ Documents ingested successfully!");
            logger.info("========================================");
            logger.info("\nNext: Run 'rag ask \"Your question\"' to query the documents\n");
            
        } catch (Exception e) {
            logger.error("Ingestion failed: " + e.getMessage(), e);
            System.exit(1);
        }
    }
}
