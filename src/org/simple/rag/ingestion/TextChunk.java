package org.simple.rag.ingestion;

/**
 * Represents a text chunk extracted from a document.
 * Contains the text content, chunk index, and page number.
 * 
 * Note: This class is currently unused. Consider using EmbeddedChunk instead.
 */
public class TextChunk {

    private final String text;
    private final int chunkIndex;
    private final int page;

    public TextChunk(String text, int chunkIndex, int page) {
        this.text = text;
        this.chunkIndex = chunkIndex;
        this.page = page;
    }

    public String getText() {
        return text;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public int getPage() {
        return page;
    }

    @Override
    public String toString() {
        return "TextChunk{" +
                "text='" + (text != null && text.length() > 50 ? text.substring(0, 50) + "..." : text) + '\'' +
                ", chunkIndex=" + chunkIndex +
                ", page=" + page +
                '}';
    }
}