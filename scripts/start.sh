#!/bin/bash

# Simple RAG Application Startup Script
# 
# This script starts the RAG application with proper configuration and error handling.
# 
# Usage: ./start.sh [OPTIONS]
# Options:
#   --ingest         Automatically ingest documents on startup
#   --help           Show this help message
#   --version        Show version information

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_HOME="$(dirname "$SCRIPT_DIR")"
LIB_DIR="$APP_HOME/lib"
CONFIG_DIR="$APP_HOME/config"
LOGS_DIR="$APP_HOME/logs"
DATA_DIR="$APP_HOME/data"

# Create directories if they don't exist
mkdir -p "$LOGS_DIR" "$DATA_DIR"

# Default settings
APP_JAR="$LIB_DIR/org.simple.rag-1.0.0.jar"
JAVA_OPTS="${JAVA_OPTS:--Xmx2g -Xms512m}"
INGEST_FLAG=""
LOG_FILE="$LOGS_DIR/app-$(date +%Y%m%d-%H%M%S).log"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
show_help() {
    cat << EOF
Simple RAG Application Launcher

Usage: $0 [OPTIONS]

Options:
    --ingest         Automatically ingest documents on startup
    --help           Show this help message
    --version        Show version information
    --memory N       Set heap memory (e.g., --memory 4g)
    --config PATH    Use custom config directory (default: ./config)

Examples:
    $0                          # Start with interactive mode
    $0 --ingest                 # Start and ingest documents
    $0 --memory 4g --ingest     # Start with 4GB memory and ingest

Configuration:
    Configuration files should be in the config directory:
    - embedding.properties
    - llm.properties

    Or set environment variables:
    - EMBEDDING_ENDPOINT
    - EMBEDDING_MODEL
    - LLM_ENDPOINT
    - LLM_MODEL

Logging:
    Logs are written to: $LOGS_DIR/

For more information, see README.md and DEPLOYMENT.md

EOF
}

show_version() {
    echo "Simple RAG Application v1.0.0"
}

log() {
    local level=$1
    shift
    local message="$@"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$timestamp] [$level] $message" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}ERROR: $@${NC}" >&2
    exit 1
}

warn() {
    echo -e "${YELLOW}WARN: $@${NC}"
}

success() {
    echo -e "${GREEN}✓ $@${NC}"
}

check_java() {
    if ! command -v java &> /dev/null; then
        error "Java is not installed or not in PATH"
    fi

    local java_version=$(java -version 2>&1 | grep -oP '(\d+)' | head -1)
    if [ "$java_version" -lt 17 ]; then
        error "Java 17+ is required. Found version: $java_version"
    fi

    success "Java version check passed (v$java_version)"
}

check_jar() {
    if [ ! -f "$APP_JAR" ]; then
        error "Application JAR not found: $APP_JAR"
    fi
    success "Application JAR found"
}

check_config() {
    if [ ! -d "$CONFIG_DIR" ]; then
        warn "Config directory not found: $CONFIG_DIR"
        warn "Using environment variables or defaults"
    else
        export CLASSPATH="$CONFIG_DIR:$CLASSPATH"
    fi
}

parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --ingest)
                INGEST_FLAG="--ingest"
                shift
                ;;
            --help)
                show_help
                exit 0
                ;;
            --version)
                show_version
                exit 0
                ;;
            --memory)
                if [[ $2 == -* ]] || [ -z "$2" ]; then
                    error "Memory size required for --memory flag"
                fi
                JAVA_OPTS="-Xmx$2 -Xms$(echo $2 | sed 's/g//' | awk '{print int($1/4)}')g"
                shift 2
                ;;
            --config)
                if [[ $2 == -* ]] || [ -z "$2" ]; then
                    error "Path required for --config flag"
                fi
                CONFIG_DIR="$2"
                shift 2
                ;;
            *)
                error "Unknown option: $1"
                ;;
        esac
    done
}

main() {
    echo ""
    echo "=========================================="
    echo "Simple RAG Application Launcher"
    echo "=========================================="
    echo ""

    log "INFO" "Starting in directory: $APP_HOME"

    # Run checks
    check_java
    check_jar
    check_config

    # Log configuration
    log "INFO" "Java Options: $JAVA_OPTS"
    log "INFO" "Config Directory: $CONFIG_DIR"
    log "INFO" "Logs Directory: $LOGS_DIR"
    log "INFO" "Log File: $LOG_FILE"
    
    if [ -n "$INGEST_FLAG" ]; then
        log "INFO" "Document ingestion enabled"
    fi

    echo ""
    success "All checks passed. Starting application..."
    echo ""

    # Start application
    java $JAVA_OPTS -cp "$CONFIG_DIR:$APP_JAR" org.simple.rag.RAGApplication $INGEST_FLAG 2>&1 | tee -a "$LOG_FILE"
}

# Trap signals for graceful shutdown
trap 'echo ""; log "INFO" "Shutdown signal received"; exit 0' SIGINT SIGTERM

# Parse arguments
parse_args "$@"

# Run main
main
