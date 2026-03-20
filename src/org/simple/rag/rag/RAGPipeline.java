package org.simple.rag.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.simple.rag.Activator;
import org.simple.rag.ingestion.EmbeddedChunk;
import org.simple.rag.ingestion.EmbeddingService;
import org.simple.rag.config.LLMConfig;

public class RAGPipeline {

    private final EmbeddingService embeddingService;
    private final VectorSimilaritySearch vectorSearch;
    private final LLMService llmService;
    private final List<EmbeddedChunk> knowledgeBase;

    private static final int DEFAULT_TOP_K = 3;
    private static final String DEFAULT_SYSTEM_PROMPT =
            "You are a helpful assistant. Use the provided context to answer questions accurately. " +
                    "If the context doesn't contain relevant information, say so.";

    public RAGPipeline(EmbeddingService embeddingService, List<EmbeddedChunk> knowledgeBase) {
        this(embeddingService, knowledgeBase, new LLMConfig());
    }

    public RAGPipeline(EmbeddingService embeddingService, List<EmbeddedChunk> knowledgeBase, LLMConfig llmConfig) {
        this.embeddingService = Objects.requireNonNull(embeddingService, "EmbeddingService cannot be null");
        this.knowledgeBase = Objects.requireNonNull(knowledgeBase, "Knowledge base cannot be null");
        this.vectorSearch = new VectorSimilaritySearch(knowledgeBase);
        this.llmService = new LLMService(Objects.requireNonNull(llmConfig, "LLMConfig cannot be null"));
    }

    public String query(String query, int topK) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            return "Please provide a valid question.";
        }

        Activator.getDefault().getLog().info("RAG Query: " + query);
        Activator.getDefault().getLog().info("Retrieving " + topK + " relevant chunks...");

        // Step 1: Generate embedding for query
        float[] queryEmbedding = embeddingService.generateEmbedding(query);

        // Step 2: Find similar chunks
        List<VectorSimilaritySearch.SearchResult> results = vectorSearch.search(queryEmbedding, topK);

        if (results.isEmpty()) {
            Activator.getDefault().getLog().warn("No relevant chunks found for query");
            return "No relevant information found in the knowledge base.";
        }

        Activator.getDefault().getLog().info("Found " + results.size() + " relevant chunks");
        for (int i = 0; i < results.size(); i++) {
            VectorSimilaritySearch.SearchResult result = results.get(i);
            Activator.getDefault().getLog().info(
                    String.format("  [%d] Similarity: %.4f", i + 1, result.similarity));
        }

        // Step 3: Build context from retrieved chunks
        String context = buildContext(results);

        // Step 4: Generate prompt with context
        String prompt = buildPrompt(query, context);

        // Step 5: Call LLM
        Activator.getDefault().getLog().info("Generating response from LLM...");
        String response = llmService.generate(prompt);

        Activator.getDefault().getLog().info("Response generated successfully");
        return response;
    }

    public String query(String query) throws Exception {
        return query(query, DEFAULT_TOP_K);
    }

    private String buildContext(List<VectorSimilaritySearch.SearchResult> results) {
        StringBuilder context = new StringBuilder();
        context.append("CONTEXT:\n");
        context.append("----------\n");

        for (int i = 0; i < results.size(); i++) {
            EmbeddedChunk chunk = results.get(i).chunk;
            context.append("[Source ").append(i + 1).append("]\n");
            context.append(chunk.getText()).append("\n");
            if (chunk.getMetadata().containsKey("source")) {
                context.append("(Source: ").append(chunk.getMetadata().get("source")).append(")\n");
            }
            context.append("\n");
        }

        context.append("----------\n");
        return context.toString();
    }

    private String buildPrompt(String query, String context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(DEFAULT_SYSTEM_PROMPT).append("\n\n");
        prompt.append(context).append("\n");
        prompt.append("QUESTION: ").append(query).append("\n");
        prompt.append("ANSWER:");
        return prompt.toString();
    }

    public void addChunks(List<EmbeddedChunk> chunks) {
        if (chunks != null && !chunks.isEmpty()) {
            this.knowledgeBase.addAll(chunks);
            // Rebuild search index with new chunks
            // Note: Consider using a more efficient index update in future versions
        }
    }

    public String getDiagnostics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RAG Pipeline Diagnostics ===\n");
        sb.append("Knowledge Base Size: ").append(knowledgeBase.size()).append(" chunks\n");
        sb.append("\nEmbedding Service:\n").append(embeddingService.getDiagnostics());
        sb.append("\nLLM Service:\n").append(llmService.getDiagnostics());
        return sb.toString();
    }

    public LLMService getLLMService() {
        return llmService;
    }

    public VectorSimilaritySearch getVectorSearch() {
        return vectorSearch;
    }

    public List<EmbeddedChunk> getKnowledgeBase() {
        return new ArrayList<>(knowledgeBase);
    }

    public EmbeddingService getEmbeddingService() {
        return embeddingService;
    }
}
