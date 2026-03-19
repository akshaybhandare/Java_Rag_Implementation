package org.simple.rag.ingestion;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a text chunk with its corresponding embedding vector and metadata.
 * This is a core data structure for the RAG pipeline.
 * 
 * Each chunk contains:
 * - The original text content
 * - A vector representation (embedding) of that text
 * - Positional information (chunk index, page number)
 * - Optional metadata (source file, timestamp, etc.)
 */
public class EmbeddedChunk {

    private final String text;
    private final float[] vector;
    private final int chunkIndex;
    private final int page;
    private final Map<String, Object> metadata;

    /**
     * Create a new embedded chunk.
     * 
     * @param text The text content of the chunk
     * @param vector The embedding vector for this text
     * @param chunkIndex The index of this chunk (0-based)
     * @param page The page number where this chunk originated (1-based)
     */
    public EmbeddedChunk(String text, float[] vector, int chunkIndex, int page) {
        this.text = Objects.requireNonNull(text, "Text cannot be null");
        this.vector = Objects.requireNonNull(vector, "Vector cannot be null");
        this.chunkIndex = chunkIndex;
        this.page = page;
        this.metadata = new HashMap<>();
    }

    /**
     * Get the text content of this chunk.
     * 
     * @return The text content
     */
    public String getText() {
        return text;
    }

    /**
     * Get the embedding vector for this chunk.
     * 
     * @return The float array representing the embedding vector
     */
    public float[] getVector() {
        return vector;
    }

    /**
     * Get the index of this chunk within its document.
     * 
     * @return The chunk index (0-based)
     */
    public int getChunkIndex() {
        return chunkIndex;
    }

    /**
     * Get the page number where this chunk originated.
     * 
     * @return The page number (1-based)
     */
    public int getPage() {
        return page;
    }

    /**
     * Get all metadata associated with this chunk.
     * 
     * @return A map of metadata key-value pairs
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Add a metadata entry to this chunk.
     * 
     * @param key The metadata key
     * @param value The metadata value
     */
    public void addMetadata(String key, Object value) {
        if (key != null && !key.isEmpty()) {
            metadata.put(key, value);
        }
    }

    @Override
    public String toString() {
        return "EmbeddedChunk{" +
                "text='" + (text.length() > 50 ? text.substring(0, 50) + "..." : text) + '\'' +
                ", vectorDim=" + vector.length +
                ", chunkIndex=" + chunkIndex +
                ", page=" + page +
                ", metadata=" + metadata +
                '}';
    }
}
