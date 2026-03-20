#!/bin/bash

# Simple RAG Application Runner Script
# Provides convenience commands to run the RAG application

JAR_FILE="./target/org.simple.rag-1.0.0-fat.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Application JAR not found: $JAR_FILE"
    echo ""
    echo "Please build the application first:"
    echo "  ./setup.sh"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed"
    exit 1
fi

# Run the application with provided arguments
java -jar "$JAR_FILE" "$@"
