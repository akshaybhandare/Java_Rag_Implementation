#!/bin/bash

# Setup and run in one command
# Useful for first-time users

echo "Simple RAG - Quick Setup"
echo "======================"
echo ""

# Check if already built
if [ -f "./target/org.simple.rag-1.0.0-fat.jar" ]; then
    echo "✓ Application already built"
else
    echo "Building application (first run only)..."
    ./setup.sh || exit 1
fi

echo ""
echo "Initializing application..."
./run-rag.sh init

echo ""
echo "Setup complete!"
echo ""
echo "Next steps:"
echo "  1. Add your PDF files to: ./data/documents/"
echo "  2. Run: ./run-rag.sh ingest"
echo "  3. Run: ./run-rag.sh ask \"What is...?\""
echo ""
