package org.simple.rag;

public final class GlobalConstants {

    private GlobalConstants() {
    }

    // Directory Configuration
    public static final String VECTOR_STORE_DIR = "vector_store";
    public static final String KNOWLEDGE_BASE_DIR = "/Users/akshaybhandare/Learning/AI/RAG_JAVA/KnowledgeFolder";

    // API Configuration
    public static final String OPENAI_API_KEY_ENV_VAR = "OPENAI_API_KEY";

    // Supported File Types
    public static final String KNOWN_FILE_TYPES = "txt,pdf,docx,html,md";

    // Embedding Configuration (deprecated - use EmbeddingConfig instead)
    public static final String EMBEDDING_MODEL = "text-embedding-mxbai-embed-large-v1";
    public static final String EMBEDDING_ENDPOINT = "http://192.168.10.11:1234/v1/embeddings";
}
