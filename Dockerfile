FROM openjdk:17-jdk-slim

LABEL maintainer="RAG Application Team"
LABEL description="Simple RAG Application - Retrieval-Augmented Generation Pipeline"
LABEL version="1.0.0"

WORKDIR /app

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Copy application JAR
COPY target/org.simple.rag-1.0.0.jar app.jar

# Copy configuration files
COPY embedding.properties.sample embedding.properties.sample
COPY llm.properties.sample llm.properties.sample

# Create directories for data and logs
RUN mkdir -p data logs config && \
    chmod 755 data logs config

# Expose port for future REST API
EXPOSE 9090

# Set memory defaults
ENV JAVA_OPTS="-Xmx2g -Xms512m"

# Health check - tries to connect to potential REST endpoint
# In future versions, implement actual health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:9090/health 2>/dev/null || exit 1

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --ingest"]
