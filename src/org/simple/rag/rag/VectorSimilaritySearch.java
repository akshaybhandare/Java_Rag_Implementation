package org.simple.rag.rag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.simple.rag.ingestion.EmbeddedChunk;

/**
 * Performs vector similarity search using cosine similarity metric.
 * 
 * Used to find the most semantically similar document chunks for a given query.
 * This is a core component of the RAG pipeline's retrieval phase.
 * 
 * The search ranks chunks by their cosine similarity to the query vector,
 * returning the top K most similar chunks.
 */
public class VectorSimilaritySearch {

    private final List<EmbeddedChunk> chunks;

    /**
     * Create a new vector similarity search index.
     * 
     * @param chunks The list of text chunks with embeddings to search
     * @throws NullPointerException if chunks is null
     */
    public VectorSimilaritySearch(List<EmbeddedChunk> chunks) {
        this.chunks = Objects.requireNonNull(chunks, "Chunks cannot be null");
    }

    /**
     * Search for similar chunks based on semantic similarity.
     * 
     * @param queryVector The embedding vector of the query
     * @param topK Number of top results to return
     * @return List of chunks ranked by similarity (descending order)
     * @throws IllegalArgumentException if vector dimensions don't match
     */
    public List<SearchResult> search(float[] queryVector, int topK) {
        if (queryVector == null || queryVector.length == 0) {
            return Collections.emptyList();
        }

        List<SearchResult> results = new ArrayList<>();

        for (EmbeddedChunk chunk : chunks) {
            float similarity = cosineSimilarity(queryVector, chunk.getVector());
            results.add(new SearchResult(chunk, similarity));
        }

        // Sort by similarity (descending)
        Collections.sort(results, (a, b) -> Float.compare(b.similarity, a.similarity));

        // Return top K results
        return results.subList(0, Math.min(topK, results.size()));
    }

    /**
     * Calculate cosine similarity between two vectors.
     * 
     * Formula: similarity = (a · b) / (||a|| * ||b||)
     * 
     * @param vec1 First vector
     * @param vec2 Second vector
     * @return Cosine similarity score between -1 and 1
     * @throws IllegalArgumentException if vectors have different dimensions
     */
    private float cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vectors must have the same dimension. " +
                "Got " + vec1.length + " and " + vec2.length);
        }

        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        float denominator = (float) Math.sqrt(norm1) * (float) Math.sqrt(norm2);
        if (denominator == 0.0f) {
            return 0.0f;
        }

        return dotProduct / denominator;
    }

    /**
     * Represents a search result with a chunk and its similarity score.
     */
    public static class SearchResult {

        public final EmbeddedChunk chunk;
        public final float similarity;

        /**
         * Create a search result.
         * 
         * @param chunk The embedded chunk
         * @param similarity The similarity score (0-1)
         */
        public SearchResult(EmbeddedChunk chunk, float similarity) {
            this.chunk = Objects.requireNonNull(chunk, "Chunk cannot be null");
            this.similarity = similarity;
        }

        @Override
        public String toString() {
            String textPreview = chunk.getText();
            if (textPreview.length() > 50) {
                textPreview = textPreview.substring(0, 50) + "...";
            }

            return "SearchResult{" +
                    "similarity=" + String.format("%.4f", similarity) +
                    ", page=" + chunk.getPage() +
                    ", text='" + textPreview + '\'' +
                    '}';
        }
    }
}
