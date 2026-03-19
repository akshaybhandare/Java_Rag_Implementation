package org.simple.rag.ingestion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.simple.rag.Activator;

public class PDFFileIngester implements IIngestFileProcessor {

    private static final int CHUNK_SIZE = 500;     // characters (~300 tokens)
    private static final int OVERLAP = 100;

    private EmbeddingService embeddingService;

    public PDFFileIngester() {
        this.embeddingService = new EmbeddingService();
    }

    @Override
    public void processFile() throws Exception {
        // This method is kept for backward compatibility but does not return chunks
        processAndGetChunks();
    }

    /**
     * Process files and return the embedded chunks.
     * @return List of embedded chunks
     * @throws Exception if processing fails
     */
    public List<EmbeddedChunk> processAndGetChunks() throws Exception {
        List<Path> pdfFiles = getIngestFiles("pdf");
        Activator.getDefault().getLog().info("Found " + pdfFiles.size() + " PDF files to process");

        List<EmbeddedChunk> allChunks = new ArrayList<>();

        for (Path pdfFile : pdfFiles) {
            try {
                List<EmbeddedChunk> chunks = processFile(pdfFile);
                logProcessingResults(chunks, pdfFile);
                allChunks.addAll(chunks);
            } catch (Exception e) {
                Activator.getDefault().getLog().error(
                    "Failed to process file: " + pdfFile.toAbsolutePath(), e);
            }
        }

        return allChunks;
    }

    private void logProcessingResults(List<EmbeddedChunk> chunks, Path sourceFile) {
        Activator.getDefault().getLog().info("Processed file: " + sourceFile.getFileName() +
            " - Generated " + chunks.size() + " embedded chunks");
    }

	private List<EmbeddedChunk>  processFile(Path pdfFile) throws IOException, InterruptedException {
		List<EmbeddedChunk> chunks = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile.toFile())) {

            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String rawText = stripper.getText(document);

                String cleaned = cleanText(rawText);

                
                List<String> paragraphs = splitIntoParagraphs(cleaned);

                List<String> pageChunks = chunkParagraphs(paragraphs);

                int index = 0;
                for (String chunkText : pageChunks) {
                    try {
                        float[] embedding = embeddingService.generateEmbedding(chunkText);
                        EmbeddedChunk embeddedChunk = new EmbeddedChunk(chunkText, embedding, index++, page);
                        embeddedChunk.addMetadata("source", pdfFile.getFileName().toString());
                        chunks.add(embeddedChunk);
                    } catch (Exception e) {
                        Activator.getDefault().getLog().warn("Failed to generate embedding for chunk " + index + " on page " + page + ": " + e.getMessage());
                    }
                }
            }
        }

        return chunks;
	}
	
    /**
     * Clean text by removing formatting artifacts and normalizing whitespace.
     */
    private String cleanText(String text) {
        return text
                .replaceAll("-\\n", "")      // fix broken words
                .replaceAll("\\n+", "\n")   // normalize newlines
                .replaceAll("\\s+", " ")    // normalize spaces
                .trim();
    }

    /**
     * Split cleaned text into paragraphs based on empty lines.
     */
    private List<String> splitIntoParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();

        String[] parts = text.split("\\n");

        StringBuilder current = new StringBuilder();

        for (String line : parts) {
            if (line.trim().isEmpty()) {
                if (current.length() > 0) {
                    paragraphs.add(current.toString().trim());
                    current.setLength(0);
                }
            } else {
                current.append(line).append(" ");
            }
        }

        if (current.length() > 0) {
            paragraphs.add(current.toString().trim());
        }

        return paragraphs;
    }

    /**
     * Create chunks from paragraphs with overlap to maintain context.
     */
    private List<String> chunkParagraphs(List<String> paragraphs) {
    	List<String> chunks = new ArrayList<>();

        StringBuilder buffer = new StringBuilder();

        for (String para : paragraphs) {

            // If single paragraph itself is too big → split it
            if (para.length() > CHUNK_SIZE) {
                flushBuffer(chunks, buffer);

                chunks.addAll(splitLargeParagraph(para));
                continue;
            }

            // If adding this paragraph exceeds limit → flush
            if (buffer.length() + para.length() + 1 > CHUNK_SIZE) {
                flushBuffer(chunks, buffer);
            }

            buffer.append(para).append(" ");
        }

        // Flush remaining
        flushBuffer(chunks, buffer);

        return applyOverlap(chunks);
    }
    
    private void flushBuffer(List<String> chunks, StringBuilder buffer) {
        if (buffer.length() > 0) {
            chunks.add(buffer.toString().trim());
            buffer.setLength(0);
        }
    }
    
    
    private List<String> splitLargeParagraph(String para) {
        List<String> result = new ArrayList<>();

        int start = 0;

        while (start < para.length()) {
            int end = Math.min(start + CHUNK_SIZE, para.length());
            result.add(para.substring(start, end));
            start = end;
        }

        return result;
    }
    
    private List<String> applyOverlap(List<String> chunks) {
        if (chunks.isEmpty()) return chunks;

        List<String> result = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String current = chunks.get(i);

            if (i == 0) {
                result.add(current);
                continue;
            }

            String prev = chunks.get(i - 1);

            String overlap = prev.substring(
                    Math.max(0, prev.length() - OVERLAP)
            );

            result.add(overlap + " " + current);
        }

        return result;
    }

}
