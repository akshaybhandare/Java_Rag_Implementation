package org.simple.rag;

import java.util.List;
import java.util.Scanner;

import org.simple.rag.config.EmbeddingConfig;
import org.simple.rag.config.LLMConfig;
import org.simple.rag.ingestion.EmbeddedChunk;
import org.simple.rag.ingestion.EmbeddingService;
import org.simple.rag.ingestion.PDFFileIngester;
import org.simple.rag.rag.RAGPipeline;

/**
 * Main entry point for the Simple RAG Application.
 * 
 * This application implements a Retrieval-Augmented Generation pipeline that:
 * 1. Ingests documents (PDFs) and generates embeddings
 * 2. Stores embeddings in a knowledge base
 * 3. Allows users to query the knowledge base
 * 4. Generates context-aware responses using an LLM
 * 
 * Usage:
 *  java -jar org.simple.rag-1.0.0.jar
 */
public class RAGApplication {

    private static final Logger logger = Logger.getInstance();
    private RAGPipeline pipeline;
    private Scanner scanner;

    /**
     * Main method - application entry point.
     */
    public static void main(String[] args) {
        try {
            RAGApplication app = new RAGApplication();
            app.run(args);
        } catch (Exception e) {
            logger.error("Application failed to start: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Initialize and run the application.
     */
    private void run(String[] args) throws Exception {
        logger.info("========================================");
        logger.info("Simple RAG Application v1.0.0");
        logger.info("========================================");
        logger.info("");

        try {
            // Initialize services
            init();

            // Show diagnostics
            showDiagnostics();

            // Ingest documents
            if (shouldIngestDocuments(args)) {
                ingestDocuments();
            }

            // Start interactive mode
            if (pipeline.getKnowledgeBase().isEmpty()) {
                logger.warn("Knowledge base is empty. Please ingest documents first.");
                return;
            }

            startInteractiveMode();

        } finally {
            cleanup();
        }
    }

    /**
     * Initialize services and pipeline.
     */
    private void init() throws Exception {
        logger.info("Initializing services...");

        EmbeddingConfig embeddingConfig = new EmbeddingConfig();
        LLMConfig llmConfig = new LLMConfig();

        logger.info("Embedding Config: " + embeddingConfig);
        logger.info("LLM Config: " + llmConfig);

        EmbeddingService embeddingService = new EmbeddingService(embeddingConfig);
        List<EmbeddedChunk> knowledgeBase = loadKnowledgeBase();

        this.pipeline = new RAGPipeline(embeddingService, knowledgeBase, llmConfig);
        this.scanner = new Scanner(System.in);

        logger.info("Services initialized successfully");
    }

    /**
     * Load knowledge base from storage (stub for future enhancement).
     */
    private List<EmbeddedChunk> loadKnowledgeBase() {
        // TODO: Implement persistence layer to load from database
        // For now, return empty list - will be populated during ingestion
        return new java.util.ArrayList<>();
    }

    /**
     * Check if documents should be ingested based on arguments or user input.
     */
    private boolean shouldIngestDocuments(String[] args) {
        for (String arg : args) {
            if (arg.equals("--ingest") || arg.equals("-i")) {
                return true;
            }
        }

        System.out.print("\nIngest documents from knowledge base? (y/n): ");
        String response = scanner.nextLine().toLowerCase().trim();
        return response.equals("y") || response.equals("yes");
    }

    /**
     * Ingest PDF documents from the knowledge base directory.
     */
    private void ingestDocuments() throws Exception {
        logger.info("");
        logger.info("Starting document ingestion...");

        PDFFileIngester ingester = new PDFFileIngester();
        try {
            ingester.processFile();
            logger.info("Documents ingested successfully");
            logger.info("Knowledge base now contains: " + pipeline.getKnowledgeBase().size() + " chunks");
        } catch (Exception e) {
            logger.error("Document ingestion failed: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Show service diagnostics information.
     */
    private void showDiagnostics() {
        System.out.println();
        System.out.println(pipeline.getDiagnostics());
    }

    /**
     * Start interactive query mode.
     */
    private void startInteractiveMode() {
        logger.info("");
        logger.info("========================================");
        logger.info("Interactive Mode - Enter your queries");
        logger.info("Type 'exit' or 'quit' to exit");
        logger.info("========================================");
        logger.info("");

        while (true) {
            try {
                System.out.print("\nQuery: ");
                String query = scanner.nextLine().trim();

                if (query.isEmpty()) {
                    continue;
                }

                if (isExitCommand(query)) {
                    logger.info("Exiting...");
                    break;
                }

                processQuery(query);

            } catch (Exception e) {
                logger.error("Query processing failed: " + e.getMessage());
            }
        }
    }

    /**
     * Process a user query through the RAG pipeline.
     */
    private void processQuery(String query) throws Exception {
        System.out.println();
        logger.info("Processing query...");

        try {
            String response = pipeline.query(query);
            System.out.println();
            System.out.println("Response:");
            System.out.println("----------");
            System.out.println(response);
            System.out.println("----------");
        } catch (Exception e) {
            logger.error("Failed to process query: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the input is an exit command.
     */
    private boolean isExitCommand(String input) {
        String lower = input.toLowerCase().trim();
        return lower.equals("exit") || lower.equals("quit") || lower.equals("q");
    }

    /**
     * Cleanup resources.
     */
    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
        logger.info("Application shutdown complete");
    }
}
