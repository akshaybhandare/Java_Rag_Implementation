#!/bin/bash

# Simple RAG Application Setup Script
# This script builds the application and prepares it for use

echo "=========================================="
echo "Simple RAG - Setup Script"
echo "=========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed"
    echo "Please install Maven 3.6+ from https://maven.apache.org/"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed"
    echo "Please install Java 17+ from https://openjdk.java.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F'"' '{print $2}' | sed 's/[._].*//')
if [ "$JAVA_VERSION" -lt "17" ]; then
    echo "❌ Java 17+ is required (found version $JAVA_VERSION)"
    exit 1
fi

echo "✓ Java version: $(java -version 2>&1 | head -n 1)"
echo "✓ Maven is installed"
echo ""

# Clean and build
echo "Building application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo ""
echo "=========================================="
echo "✓ Build complete!"
echo "=========================================="
echo ""
echo "Application JAR: target/org.simple.rag-1.0.0-fat.jar"
echo ""
echo "Quick start:"
echo "  1. ./run-rag.sh init"
echo "  2. Place PDFs in: ./data/documents/"
echo "  3. ./run-rag.sh ingest"
echo "  4. ./run-rag.sh ask \"Your question\""
echo ""
echo "View all commands: ./run-rag.sh --help"
echo ""
