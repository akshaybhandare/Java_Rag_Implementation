package org.simple.rag.ingestion;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EmbeddedChunk {

    private final String text;
    private final float[] vector;
    private final int chunkIndex;
    private final int page;
    private final Map<String, Object> metadata;

    public EmbeddedChunk(String text, float[] vector, int chunkIndex, int page) {
        this.text = Objects.requireNonNull(text, "Text cannot be null");
        this.vector = Objects.requireNonNull(vector, "Vector cannot be null");
        this.chunkIndex = chunkIndex;
        this.page = page;
        this.metadata = new HashMap<>();
    }

    public String getText() {
        return text;
    }

    public float[] getVector() {
        return vector;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public int getPage() {
        return page;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

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
