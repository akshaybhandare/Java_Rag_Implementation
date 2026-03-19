# Simple RAG - CLI Application

Retrieval-Augmented Generation application for document question-answering.

---

## Build

```bash
./setup.sh          # Linux/macOS
setup.bat          # Windows
```

**Release Artifacts** (in `target/`):
- `org.simple.rag-1.0.0-fat.jar` (7.8 MB) - **Use this** - Contains all dependencies
- `org.simple.rag-1.0.0.jar` (53 KB) - Compiled code only

---

## Configuration

Edit these files:

### `config/embedding.properties`
```properties
embedding.endpoint=http://localhost:1234/v1/embeddings
embedding.model=text-embedding-mxbai-embed-large-v1
embedding.timeout=30
embedding.enabled=true
```

### `config/llm.properties`
```properties
llm.endpoint=http://localhost:11434
llm.model=mistral:7b
llm.timeout=120
llm.enabled=true
```

Or via CLI:
```bash
./run-rag.sh config --llm-endpoint http://yourserver:11434
./run-rag.sh config --embedding-endpoint http://yourserver:1234/v1/embeddings
./run-rag.sh config --show
```

---

## Two Main Commands

### 1. Ingest Documents

Place PDFs/TXT files in `./data/documents/` then:
```bash
./run-rag.sh ingest
```

### 2. Ask Questions

Single question:
```bash
./run-rag.sh ask "What is the main topic?"
```

Interactive mode:
```bash
./run-rag.sh ask
# Type questions, press Enter, type 'quit' to exit
```

---

## Directory Structure

```
./config/              - Configuration files
./data/
  ├── documents/       - Place your PDFs and TXT files here
  └── embeddings/      - Auto-generated embeddings
./logs/                - Application logs
```

---

## What You Get After Build

✅ **Executable JAR** - `target/org.simple.rag-1.0.0-fat.jar`
- All dependencies included
- Ready to run: `java -jar org.simple.rag-1.0.0-fat.jar`
- Cross-platform (Linux, macOS, Windows)

✅ **Scripts**
- `setup.sh` / `setup.bat` - Build
- `run-rag.sh` / `run-rag.bat` - Run commands

✅ **Folder Structure**
- Auto-created `config/`, `data/`, `logs/` directories
- Configuration files with defaults
- Ready for documents

---

## System Requirements

- Java 17+
- Maven 3.6+ (for building)
- Embedding service running (e.g., Ollama)
- LLM service running (e.g., Ollama)

---

## Example Workflow

```bash
# 1. Build
./setup.sh

# 2. Configure (edit files or use CLI)
./run-rag.sh config --llm-model mistral:7b

# 3. Add documents
cp your-documents.pdf ./data/documents/

# 4. Ingest
./run-rag.sh ingest

# 5. Ask
./run-rag.sh ask "What is this about?"
```

---
