# Simple RAG Application

A lightweight Retrieval-Augmented Generation (RAG) pipeline for document-based question answering.

## Overview

This application combines document ingestion, semantic search, and language model generation to provide context-aware answers to user queries. It processes PDF documents, generates embeddings for semantic similarity search, and uses an LLM to synthesize comprehensive answers.

## Architecture

```
┌─────────────────────────────────────────────┐
│         RAG Pipeline                        │
├─────────────────────────────────────────────┤
│ 1. Document Ingestion (PDFs)               │
│ 2. Text Chunking with Overlap              │
│ 3. Embedding Generation                    │
│ 4. Vector Similarity Search                │
│ 5. LLM-based Response Generation           │
└─────────────────────────────────────────────┘
```

## Key Components

### Core Modules

- **RAGPipeline**: Main orchestrator combining search and generation
- **EmbeddingService**: Generates semantic embeddings via remote API
- **LLMService**: Interacts with language models (Ollama)
- **VectorSimilaritySearch**: Performs semantic similarity matching
- **PDFFileIngester**: Processes and chunks PDF documents

### Configuration

- **EmbeddingConfig**: Manages embedding service settings
- **LLMConfig**: Manages LLM service settings
- Settings loaded from: environment variables → property files → defaults

## System Requirements

- **Java**: 17 or higher
- **Maven**: 3.6 or higher
- **Memory**: Minimum 512MB, recommended 2GB+
- **Embedding Service**: Running embedding API (default: http://192.168.10.11:1234)
- **LLM Service**: Running Ollama or compatible API (default: http://100.77.159.56:11434)

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd org.simple.rag
   ```

2. Build the project:
   ```bash
   mvn clean package
   ```

3. Generate configuration files:
   ```bash
   cp embedding.properties.sample embedding.properties
   cp llm.properties.sample llm.properties
   ```

4. Edit configuration files with your service endpoints

## Configuration

### Environment Variables

Configure services using environment variables (highest priority):

```bash
# Embedding Service
export EMBEDDING_ENDPOINT="http://your-server:port/v1/embeddings"
export EMBEDDING_MODEL="text-embedding-mxbai-embed-large-v1"
export EMBEDDING_TIMEOUT="30"
export EMBEDDING_CONNECT_TIMEOUT="10"
export EMBEDDING_ENABLED="true"

# LLM Service
export LLM_ENDPOINT="http://your-server:port"
export LLM_MODEL="gemma3:4b"
export LLM_TIMEOUT="120"
export LLM_ENABLED="true"
```

### Property Files

Or use property files (`embedding.properties`, `llm.properties`):

```properties
embedding.endpoint=http://192.168.10.11:1234/v1/embeddings
embedding.model=text-embedding-mxbai-embed-large-v1
embedding.timeout=30
embedding.connect.timeout=10
embedding.enabled=true
```

### Configuration Priority

1. Environment Variables (highest)
2. Property Files
3. Default Values (lowest)

## Usage

### Running the Application

```bash
# Start interactive mode (prompts for document ingestion)
java -jar org.simple.rag-1.0.0.jar

# Start with automatic document ingestion
java -jar org.simple.rag-1.0.0.jar --ingest

# Adjust memory as needed
java -Xmx2g -Xms512m -jar org.simple.rag-1.0.0.jar
```

### Command-Line Options

- `--ingest` or `-i`: Automatically ingest documents on startup
- `--help` or `-h`: Show help information
- `--version` or `-v`: Show version information

### Interactive Mode

Once running, enter queries at the prompt:

```
Query: What is machine learning?
```

The pipeline will:
1. Generate an embedding for your query
2. Find similar chunks from the knowledge base
3. Generate a context-aware response using the LLM

Type `exit` or `quit` to exit the application.

## Document Structure

The application expects the knowledge base directory structure:

```
KnowledgeFolder/
├── document1.pdf
├── document2.pdf
├── subdirectory/
│   └── document3.pdf
└── ...
```

Configure the knowledge base path in `GlobalConstants.java` or via environment variables.

## Dependencies

- **LangChain4J**: 0.33.0 - LLM framework
- **PDFBox**: 2.0.30 - PDF processing
- **JSON**: 20240303 - JSON handling
- **Apache Commons Lang**: 3.20.0 - Utilities
- **SLF4J**: 2.0.11 - Logging
- **JUnit**: 5.10.2 - Testing

## Building

### Standard Build

```bash
mvn clean package
```

Output: `target/org.simple.rag-1.0.0.jar`

### Fat JAR (Bundled Dependencies)

```bash
mvn clean package -DskipTests
```

Output: `target/org.simple.rag-1.0.0-fat.jar` (includes all dependencies)

### Build with Tests

```bash
mvn clean verify
```

### Generate Documentation

```bash
mvn javadoc:jar
```

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions including:
- Docker containerization
- Production configuration
- Performance tuning
- Monitoring and logging
- Troubleshooting

## Performance Tuning

### Memory Configuration

```bash
java -Xmx4g -Xms2g -jar org.simple.rag-1.0.0.jar
```

### Embedding Service

- Larger batch sizes for faster ingestion
- Connection pooling for concurrent requests
- Caching embeddings for repeated queries

### LLM Configuration

- Adjust model for speed vs. quality tradeoff
- Tune context size (top K results)
- Configure timeouts based on model latency

## Troubleshooting

### Services Not Responding

1. Check service endpoints:
   ```bash
   curl http://your-embedding-service:port/health
   curl http://your-llm-service:port/api/tags
   ```

2. Verify network connectivity
3. Check configuration for correct endpoints
4. Review service logs for errors

### Out of Memory

- Increase JVM heap: `java -Xmx4g`
- Reduce embedding batch size
- Process documents in smaller batches

### Slow Performance

- Check network latency to services
- Monitor service resource utilization
- Consider caching frequently used embeddings
- Profile with tools like JProfiler or YourKit

## API Reference

### RAGPipeline

```java
// Create pipeline
RAGPipeline pipeline = new RAGPipeline(embeddingService, knowledgeBase);

// Query with default top K (3)
String response = pipeline.query("your question");

// Query with custom top K
String response = pipeline.query("your question", 5);

// Add documents to knowledge base
pipeline.addChunks(embeddedChunks);

// Get diagnostics
String diagnostics = pipeline.getDiagnostics();
```

### EmbeddingService

```java
// Generate single embedding
float[] embedding = service.generateEmbedding("text");

// Generate batch (sequential)
List<float[]> embeddings = service.generateEmbeddings(texts);

// Generate batch (async - faster)
List<float[]> embeddings = service.generateEmbeddingsAsync(texts);

// Check service health
boolean healthy = service.isHealthy();
```

### VectorSimilaritySearch

```java
// Search for similar chunks
List<SearchResult> results = search.search(queryVector, topK);

// Each result contains:
// - chunk: EmbeddedChunk object
// - similarity: float score (0-1)
```

## Testing

Run the test suite:

```bash
mvn test
```

Run specific test:

```bash
mvn test -Dtest=SimpleEmbeddingTest
```

## Contributing

1. Follow Java code style guidelines
2. Add comprehensive Javadoc to public APIs
3. Write unit tests for new functionality
4. Update documentation for user-facing changes
5. Ensure all tests pass before submitting PRs

## License

MIT License - See LICENSE file for details

## Support

For issues, questions, or suggestions:
1. Check the [DEPLOYMENT.md](DEPLOYMENT.md) guide
2. Review service health diagnostics
3. Check application logs for error messages
4. Consult the code documentation (Javadoc)

## Roadmap

- [ ] Persistent vector database (Chroma, Weaviate)
- [ ] Multi-document retrieval strategies
- [ ] Caching layer for embeddings
- [ ] Web API (REST/GraphQL)
- [ ] Advanced query processing (filters, date ranges)
- [ ] Custom prompt templates
- [ ] Model fallback strategies
- [ ] Metrics and observability
- [ ] Docker support
- [ ] Kubernetes manifests

## Version History

### v1.0.0
- Initial release
- PDF ingestion
- Semantic search
- LLM integration
- Interactive CLI

---

**Last Updated**: March 19, 2026
