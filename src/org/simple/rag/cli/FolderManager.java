package org.simple.rag.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.simple.rag.Logger;
import org.simple.rag.ingestion.EmbeddedChunk;

/**
 * FolderManager handles the application's folder structure.
 * Creates and manages directories for documents, embeddings, logs, and configs.
 */
public class FolderManager {
    
    private static final Logger logger = Logger.getInstance();
    
    private static final String DATA_DIR = "./data";
    private static final String DOCUMENTS_DIR = DATA_DIR + "/documents";
    private static final String EMBEDDINGS_DIR = DATA_DIR + "/embeddings";
    private static final String LOGS_DIR = "./logs";
    private static final String CONFIG_DIR = "./config";
    
    public static void initializeDirectories() {
        createDirectoryIfNotExists(DOCUMENTS_DIR, "Documents folder (place your PDFs and text files here)");
        createDirectoryIfNotExists(EMBEDDINGS_DIR, "Embeddings cache folder");
        createDirectoryIfNotExists(LOGS_DIR, "Application logs folder");
        createDirectoryIfNotExists(CONFIG_DIR, "Configuration folder");
    }
    
    private static void createDirectoryIfNotExists(String path, String description) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("✓ Created " + description + " at: " + path);
        }
    }
    
    public static String getDocumentsDir() {
        return DOCUMENTS_DIR;
    }
    
    public static String getEmbeddingsDir() {
        return EMBEDDINGS_DIR;
    }
    
    public static String getLogsDir() {
        return LOGS_DIR;
    }
    
    public static String getConfigDir() {
        return CONFIG_DIR;
    }
    
    /**
     * Save embedded chunks to a JSON file for persistence
     * @param chunks List of embedded chunks to save
     * @param filename Name of the file to save to (without extension)
     */
    public static void saveEmbeddedChunks(List<EmbeddedChunk> chunks, String filename) {
        if (chunks == null || chunks.isEmpty()) {
            logger.warn("No chunks to save");
            return;
        }
        
        File dir = new File(EMBEDDINGS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File file = new File(dir, filename + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            JSONArray jsonArray = new JSONArray();
            for (EmbeddedChunk chunk : chunks) {
                JSONObject jsonChunk = new JSONObject();
                jsonChunk.put("text", chunk.getText());
                jsonChunk.put("chunkIndex", chunk.getChunkIndex());
                jsonChunk.put("page", chunk.getPage());
                
                // Convert float array to JSONArray
                JSONArray vectorArray = new JSONArray();
                for (float value : chunk.getVector()) {
                    vectorArray.put(value);
                }
                jsonChunk.put("vector", vectorArray);
                
                // Add metadata
                JSONObject metadataJson = new JSONObject();
                for (Map.Entry<String, Object> entry : chunk.getMetadata().entrySet()) {
                    metadataJson.put(entry.getKey(), entry.getValue());
                }
                jsonChunk.put("metadata", metadataJson);
                
                jsonArray.put(jsonChunk);
            }
            
            writer.write(jsonArray.toString(2)); // Pretty print with 2 space indentation
            logger.info("✓ Saved " + chunks.size() + " embedded chunks to: " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save embedded chunks: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load embedded chunks from a JSON file
     * @param filename Name of the file to load from (without extension)
     * @return List of embedded chunks
     */
    public static List<EmbeddedChunk> loadEmbeddedChunks(String filename) {
        File dir = new File(EMBEDDINGS_DIR);
        if (!dir.exists()) {
            logger.warn("Embeddings directory does not exist: " + EMBEDDINGS_DIR);
            return new ArrayList<>();
        }
        
        File file = new File(dir, filename + ".json");
        if (!file.exists()) {
            logger.warn("Embedded chunks file not found: " + file.getAbsolutePath());
            return new ArrayList<>();
        }
        
        List<EmbeddedChunk> chunks = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            JSONArray jsonArray = new JSONArray(content);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonChunk = jsonArray.getJSONObject(i);
                
                String text = jsonChunk.getString("text");
                int chunkIndex = jsonChunk.getInt("chunkIndex");
                int page = jsonChunk.getInt("page");
                
                // Convert JSONArray to float array
                JSONArray vectorArray = jsonChunk.getJSONArray("vector");
                float[] vector = new float[vectorArray.length()];
                for (int j = 0; j < vectorArray.length(); j++) {
                    vector[j] = (float) vectorArray.getDouble(j);
                }
                
                EmbeddedChunk chunk = new EmbeddedChunk(text, vector, chunkIndex, page);
                
                // Add metadata
                if (jsonChunk.has("metadata")) {
                    JSONObject metadataJson = jsonChunk.getJSONObject("metadata");
                    for (String key : JSONObject.getNames(metadataJson)) {
                        chunk.addMetadata(key, metadataJson.get(key));
                    }
                }
                
                chunks.add(chunk);
            }
            
            logger.info("✓ Loaded " + chunks.size() + " embedded chunks from: " + file.getAbsolutePath());
            return chunks;
        } catch (Exception e) {
            logger.error("Failed to load embedded chunks: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    public static void displayFolderStructure() {
        logger.info("\n========== FOLDER STRUCTURE ==========\n");
        logger.info("Project Root/");
        logger.info("├── ./data/");
        logger.info("│   ├── documents/          <- Place your PDF and text files here");
        logger.info("│   └── embeddings/         <- Vector embeddings cache (auto-generated)");
        logger.info("├── ./config/");
        logger.info("│   ├── embedding.properties");
        logger.info("│   └── llm.properties");
        logger.info("├── ./logs/                 <- Application logs");
        logger.info("└── ./bin/                  <- Application JAR file");
        logger.info("\n======================================\n");
    }
    
    public static int countDocumentsInDir() {
        File dir = new File(DOCUMENTS_DIR);
        if (!dir.exists()) {
            return 0;
        }
        File[] files = dir.listFiles((d, name) -> 
            name.endsWith(".pdf") || name.endsWith(".txt")
        );
        return files != null ? files.length : 0;
    }
    
    public static void printDocumentsInDir() {
        File dir = new File(DOCUMENTS_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("Documents directory not found: " + DOCUMENTS_DIR);
            return;
        }
        
        File[] files = dir.listFiles((d, name) -> 
            name.endsWith(".pdf") || name.endsWith(".txt")
        );
        
        if (files == null || files.length == 0) {
            logger.info("No documents found in: " + DOCUMENTS_DIR);
        } else {
            logger.info("Found " + files.length + " document(s) in: " + DOCUMENTS_DIR);
            for (File file : files) {
                logger.info("  - " + file.getName() + " (" + formatFileSize(file.length()) + ")");
            }
        }
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
