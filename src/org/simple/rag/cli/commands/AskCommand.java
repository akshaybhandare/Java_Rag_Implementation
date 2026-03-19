package org.simple.rag.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import java.util.List;
import java.util.Scanner;
import org.simple.rag.Logger;
import org.simple.rag.cli.FolderManager;
import org.simple.rag.config.EmbeddingConfig;
import org.simple.rag.config.LLMConfig;
import org.simple.rag.ingestion.EmbeddedChunk;
import org.simple.rag.ingestion.EmbeddingService;
import org.simple.rag.rag.RAGPipeline;

/**
 * Ask command - Answers questions based on ingested documents.
 */
@Command(
    name = "ask",
    description = "Ask a question to the RAG system"
)
public class AskCommand implements Runnable {
    
    private static final Logger logger = Logger.getInstance();
    
    @Parameters(index = "0..*", description = "The question to ask", arity = "0..*")
    String[] questionWords;
    
    @Override
    public void run() {
        try {
            logger.info("\n========================================");
            logger.info("Simple RAG - Question Answering Mode");
            logger.info("========================================\n");
            
            // Reconstruct question from parameters
            String question = null;
            if (questionWords != null && questionWords.length > 0) {
                question = String.join(" ", questionWords);
            }
            
            // Initialize services
            logger.info("Initializing services...");
            EmbeddingConfig embeddingConfig = new EmbeddingConfig();
            LLMConfig llmConfig = new LLMConfig();
            EmbeddingService embeddingService = new EmbeddingService(embeddingConfig);
            
            // Load knowledge base
            List<EmbeddedChunk> knowledgeBase = loadKnowledgeBase();
            if (knowledgeBase.isEmpty()) {
                logger.warn("⚠ Knowledge base is empty!");
                logger.info("Please run 'rag ingest' first to ingest documents");
                System.exit(1);
            }
            
            logger.info("✓ Services initialized. Knowledge base: " + knowledgeBase.size() + " chunks\n");
            
            // Create RAG pipeline
            RAGPipeline pipeline = new RAGPipeline(embeddingService, knowledgeBase);
            
            // Interactive mode if no question provided, otherwise answer the question
            if (question == null || question.trim().isEmpty()) {
                startInteractiveMode(pipeline);
            } else {
                answerQuestion(pipeline, question);
            }
            
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage(), e);
            System.exit(1);
        }
    }
    
    private void startInteractiveMode(RAGPipeline pipeline) {
        logger.info("Interactive Mode - Type 'quit' to exit\n");
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                logger.info("Goodbye!");
                break;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            answerQuestion(pipeline, input);
            System.out.println();
        }
        scanner.close();
    }
    
    private void answerQuestion(RAGPipeline pipeline, String question) {
        try {
            logger.info("Processing: " + question + "\n");
            // Query the RAG pipeline
            String answer = pipeline.query(question);
            logger.info("\n--- Answer ---\n");
            logger.info(answer + "\n");
            
        } catch (Exception e) {
            logger.error("Failed to answer question: " + e.getMessage(), e);
        }
    }
    
    private List<EmbeddedChunk> loadKnowledgeBase() {
        // Load embedded chunks from the vector store
        List<EmbeddedChunk> chunks = FolderManager.loadEmbeddedChunks("knowledge_base");
        if (chunks.isEmpty()) {
            logger.warn("No embedded chunks found. Please run 'rag ingest' first to ingest documents.");
        }
        return chunks;
    }
}
