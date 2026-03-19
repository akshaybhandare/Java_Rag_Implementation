# BUILD ARTIFACTS & RELEASE PACKAGE

## What Users Get After `./setup.sh`

### 📦 Release Artifacts (in `target/` folder)

| File | Size | Purpose |
|------|------|---------|
| `org.simple.rag-1.0.0-fat.jar` | 7.8 MB | **MAIN RELEASE** - Use this! All dependencies included |
| `org.simple.rag-1.0.0.jar` | 53 KB | Code only (needs dependencies) |
| `org.simple.rag-1.0.0.tar.gz` | 7.3 MB | Distribution archive |
| `org.simple.rag-1.0.0.zip` | 7.3 MB | Distribution archive |

### ✅ What's Including in Build

**1. CLI Application** (compiled in JAR)
```
org/simple/rag/cli/
├── RagCLI.class              (main entry point)
├── ConfigManager.class       (config file management)
├── FolderManager.class       (folder structure)
└── commands/
    ├── InitCommand.class     (rag init)
    ├── IngestCommand.class   (rag ingest)
    ├── AskCommand.class      (rag ask)
    ├── ConfigCommand.class   (rag config)
    └── InfoCommand.class     (rag info)
```

**2. All Dependencies** (bundled in fat JAR)
- LangChain4J (RAG framework)
- Picocli (CLI framework)
- PDFBox (PDF processing)
- SLF4J (logging)
- GSON (JSON)
- And 20+ others

**3. Helper Scripts**
- `setup.sh` / `setup.bat` - Build script
- `run-rag.sh` / `run-rag.bat` - Run commands
- `quick-start.sh` - One-command setup

---

## How to Use Release

### Run Commands
```bash
java -jar target/org.simple.rag-1.0.0-fat.jar <command> [options]

# Examples:
java -jar target/org.simple.rag-1.0.0-fat.jar init
java -jar target/org.simple.rag-1.0.0-fat.jar ingest
java -jar target/org.simple.rag-1.0.0-fat.jar ask "What is this?"
java -jar target/org.simple.rag-1.0.0-fat.jar config --show
```

### Or Use Convenience Scripts
```bash
./run-rag.sh init
./run-rag.sh ingest
./run-rag.sh ask "Question"
./run-rag.sh config --show
```

---

## Distribution Package (For Users)

To distribute to end users, provide:

**Minimum:**
```
dist/
├── org.simple.rag-1.0.0-fat.jar
├── run-rag.sh          (or .bat for Windows)
├── README_CLI.md       (Quick start guide)
└── config/
    ├── embedding.properties.sample
    └── llm.properties.sample
```

**Full Package:**
```
dist/
├── org.simple.rag-1.0.0-fat.jar
├── run-rag.sh / run-rag.bat
├── setup.sh / setup.bat
├── quick-start.sh
├── README_CLI.md
├── config/
│   ├── embedding.properties.sample
│   └── llm.properties.sample
└── data/
    ├── documents/
    └── embeddings/
```

---

## Size Breakdown

- **Code + Dependencies**: 7.8 MB (fat JAR)
- **Source Code**: ~150 KB (all Java files)
- **Configuration**: < 1 KB
- **Total Release**: ~8 MB

---

## Version Info

- **Version**: 1.0.0
- **Java**: 17+
- **Build Tool**: Maven
- **Packaging**: JAR (all-in-one)
- **Platforms**: Linux, macOS, Windows

---

## Available Commands

After build, users can run:

```bash
./run-rag.sh init                      # Initialize
./run-rag.sh ingest                    # Ingest documents from ./data/documents/
./run-rag.sh ask "question"            # Ask single question
./run-rag.sh ask                       # Interactive mode
./run-rag.sh config --show             # Show config
./run-rag.sh config --llm-model <name> # Update LLM model
./run-rag.sh config --llm-endpoint     # Update LLM endpoint
./run-rag.sh config --embedding-model  # Update embedding model
./run-rag.sh info                      # Show app info
./run-rag.sh --help                    # Show all commands
```

---

## Ready to Ship!

Everything is ready:
✅ Compiled JAR with all dependencies
✅ Helper scripts for Windows, Mac, Linux
✅ Configuration management
✅ Folder structure auto-created
✅ Command reference & documentation

Users just need to:
1. Extract the release package
2. Run `./setup.sh` (or `setup.bat`)
3. Configure `config/*.properties`
4. Place documents in `./data/documents/`
5. Run `./run-rag.sh ingest`
6. Run `./run-rag.sh ask "question"`

Done! 🎯
