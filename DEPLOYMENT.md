# Deployment Guide

This guide covers deploying the Simple RAG Application in various environments.

## Table of Contents

1. [Local Development](#local-development)
2. [Standalone Server](#standalone-server)
3. [Docker Deployment](#docker-deployment)
4. [Production Checklist](#production-checklist)
5. [Monitoring & Logging](#monitoring--logging)
6. [Troubleshooting](#troubleshooting)

## Local Development

### Prerequisites

- Java 17+
- Maven 3.6+
- 512MB minimum RAM
- IDE (IntelliJ, Eclipse, VS Code)

### Setup Steps

1. **Clone and build:**
   ```bash
   git clone <repo-url>
   cd org.simple.rag
   mvn clean package
   ```

2. **Create configuration:**
   ```bash
   cp embedding.properties.sample embedding.properties
   cp llm.properties.sample llm.properties
   ```

3. **Edit configuration for local services:**
   ```properties
   # embedding.properties
   embedding.endpoint=http://localhost:1234/v1/embeddings
   embedding.enabled=true
   
   # llm.properties
   llm.endpoint=http://localhost:11434
   llm.enabled=true
   ```

4. **Run the application:**
   ```bash
   java -jar target/org.simple.rag-1.0.0.jar --ingest
   ```

## Standalone Server

### Prerequisites

- Java 17+ installed on server
- Access to embedding and LLM services
- Minimum 2GB RAM
- Network connectivity to service endpoints

### Installation Steps

1. **Transfer artifacts:**
   ```bash
   scp target/org.simple.rag-1.0.0.jar user@server:/opt/rag/
   scp embedding.properties user@server:/opt/rag/
   scp llm.properties user@server:/opt/rag/
   ```

2. **Create application directory structure:**
   ```bash
   ssh user@server
   mkdir -p /opt/rag/{logs,data,config}
   cd /opt/rag
   ```

3. **Set permissions:**
   ```bash
   chmod 755 org.simple.rag-1.0.0.jar
   chmod 644 *.properties
   ```

4. **Create systemd service (optional):**
   ```bash
   sudo tee /etc/systemd/system/rag-app.service > /dev/null <<EOF
   [Unit]
   Description=Simple RAG Application
   After=network.target
   
   [Service]
   Type=simple
   User=rag
   WorkingDirectory=/opt/rag
   ExecStart=/usr/bin/java -Xmx2g -Xms512m -jar org.simple.rag-1.0.0.jar
   Restart=on-failure
   RestartSec=10
   StandardOutput=journal
   StandardError=journal
   
   [Install]
   WantedBy=multi-user.target
   EOF
   
   sudo systemctl daemon-reload
   sudo systemctl enable rag-app
   sudo systemctl start rag-app
   ```

5. **Check status:**
   ```bash
   sudo systemctl status rag-app
   sudo journalctl -u rag-app -f
   ```

### Configuration via Environment

Set environment variables for deployment:

```bash
# Create environment file
sudo tee /opt/rag/.env > /dev/null <<EOF
EMBEDDING_ENDPOINT=http://embedding-server:1234/v1/embeddings
EMBEDDING_MODEL=text-embedding-mxbai-embed-large-v1
EMBEDDING_TIMEOUT=30
LLM_ENDPOINT=http://llm-server:11434
LLM_MODEL=gemma3:4b
LLM_TIMEOUT=120
EOF

# Source in service
EnvironmentFile=/opt/rag/.env
```

## Docker Deployment

### Dockerfile

Create `Dockerfile` in project root:

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy application
COPY target/org.simple.rag-1.0.0.jar app.jar
COPY embedding.properties .
COPY llm.properties .

# Create volumes for data
VOLUME /app/data
VOLUME /app/logs

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:9090/health || exit 1

# Default memory settings
ENV JAVA_OPTS="-Xmx2g -Xms512m"

EXPOSE 9090

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  embedding-service:
    image: mistral-embedding-server:latest
    environment:
      MODEL: text-embedding-mxbai-embed-large-v1
    ports:
      - "1234:1234"
    volumes:
      - embedding-cache:/root/.cache

  llm-service:
    image: ollama:latest
    environment:
      MODEL: gemma3:4b
    ports:
      - "11434:11434"
    volumes:
      - llm-models:/root/.ollama

  rag-app:
    build: .
    depends_on:
      - embedding-service
      - llm-service
    environment:
      EMBEDDING_ENDPOINT: http://embedding-service:1234/v1/embeddings
      LLM_ENDPOINT: http://llm-service:11434
      JAVA_OPTS: "-Xmx2g -Xms512m"
    volumes:
      - ./data:/app/data
      - ./logs:/app/logs
      - ./embedding.properties:/app/embedding.properties
      - ./llm.properties:/app/llm.properties
    ports:
      - "9090:9090"
    stdin_open: true
    tty: true

volumes:
  embedding-cache:
  llm-models:
```

### Deploy with Docker Compose

```bash
# Build and start
docker-compose up -d

# View logs
docker-compose logs -f rag-app

# Stop services
docker-compose down

# Clean up volumes
docker-compose down -v
```

### Docker Commands

```bash
# Build image
docker build -t simple-rag:1.0.0 .

# Run container
docker run -d \
  --name rag-app \
  -e EMBEDDING_ENDPOINT=http://embedding-service:1234 \
  -e LLM_ENDPOINT=http://llm-service:11434 \
  -v /data:/app/data \
  -v /logs:/app/logs \
  --memory=2g \
  --cpus=2 \
  simple-rag:1.0.0

# Inspect container
docker exec -it rag-app /bin/bash

# View logs
docker logs -f rag-app

# Stop container
docker stop rag-app

# Remove container
docker rm rag-app
```

## Production Checklist

- [ ] Java 17+ installed and configured
- [ ] All required services running and accessible
- [ ] Configuration files validated
- [ ] Firewall rules allow service access
- [ ] Sufficient disk space (check `/var`)
- [ ] Memory allocation appropriate (min 2GB)
- [ ] Backup strategy in place
- [ ] Monitoring and alerting configured
- [ ] Logging centralization setup
- [ ] Security credentials secured (not in code)
- [ ] SSL/TLS configured for external APIs if needed
- [ ] Performance baselines established
- [ ] Disaster recovery plan documented
- [ ] Load testing completed
- [ ] Documentation updated

## Monitoring & Logging

### Application Logs

Logs output to stdout/stderr:

```bash
# View real-time logs
tail -f application.log

# Search logs for errors
grep ERROR application.log

# Count log levels
grep -c INFO application.log
grep -c WARN application.log
grep -c ERROR application.log
```

### Systemd Logging

```bash
# View recent logs
sudo journalctl -u rag-app -n 50

# Follow logs in real-time
sudo journalctl -u rag-app -f

# View logs from specific time
sudo journalctl -u rag-app --since "2 minutes ago"

# Export logs
sudo journalctl -u rag-app > rag-app-logs.txt
```

### Health Checks

```bash
# Check embedding service
curl http://localhost:1234/v1/embeddings -X POST \
  -H "Content-Type: application/json" \
  -d '{"model":"text-embedding-mxbai-embed-large-v1","input":"health check"}'

# Check LLM service
curl http://localhost:11434/api/tags

# Check application diagnostics
java -cp target/org.simple.rag-1.0.0.jar org.simple.rag.SimpleEmbeddingTest
```

### Performance Monitoring

```bash
# Monitor Java process
jps -l
jstat -gc <pid> 1000

# CPU and memory usage
top -p <pid>

# Network connections
netstat -tap | grep java
```

## Troubleshooting

### Service Connection Issues

1. **Verify connectivity:**
   ```bash
   nc -zv embedding-server 1234
   nc -zv llm-server 11434
   ```

2. **Check configuration:**
   ```bash
   echo $EMBEDDING_ENDPOINT
   echo $LLM_ENDPOINT
   ```

3. **Review logs for details:**
   ```bash
   grep "Connection refused" application.log
   grep "timeout" application.log
   ```

### Performance Issues

1. **Check resource utilization:**
   ```bash
   free -h  # Memory
   df -h    # Disk
   top -b -n 1 | grep java  # CPU
   ```

2. **Increase memory if needed:**
   ```bash
   export JAVA_OPTS="-Xmx4g -Xms2g"
   ```

3. **Profile with JFR:**
   ```bash
   java -XX:StartFlightRecording=duration=60s,filename=recording.jfr \
     -jar org.simple.rag-1.0.0.jar
   ```

### Configuration Errors

1. **Verify property files exist:**
   ```bash
   ls -la *.properties
   ```

2. **Check property syntax:**
   ```bash
   grep "=" embedding.properties
   ```

3. **Validate with test:**
   ```bash
   java -cp target/org.simple.rag-1.0.0.jar \
     org.simple.rag.config.EmbeddingConfig
   ```

### Memory Leaks

1. **Monitor heap growth:**
   ```bash
   jmap -heap <pid>
   jmap -histo <pid> > heap-dump.txt
   ```

2. **Generate heap dump:**
   ```bash
   jmap -dump:live,format=b,file=heap.bin <pid>
   ```

3. **Analyze with jhat:**
   ```bash
   jhat -J-Xmx2g heap.bin
   # Access at http://localhost:7000
   ```

## Upgrade Procedure

1. **Backup current version:**
   ```bash
   cp org.simple.rag-1.0.0.jar org.simple.rag-1.0.0.jar.backup
   cp *.properties *.properties.backup
   ```

2. **Download new version:**
   ```bash
   scp user@build-server:target/org.simple.rag-1.1.0.jar .
   ```

3. **Update configuration if needed:**
   ```bash
   diff embedding.properties.sample embedding.properties
   ```

4. **Stop current service:**
   ```bash
   sudo systemctl stop rag-app
   ```

5. **Deploy new version:**
   ```bash
   mv org.simple.rag-1.1.0.jar org.simple.rag.jar
   ```

6. **Start new version:**
   ```bash
   sudo systemctl start rag-app
   ```

7. **Verify:**
   ```bash
   sudo systemctl status rag-app
   sleep 5
   sudo journalctl -u rag-app -n 20
   ```

## Rollback Procedure

If issues occur with new version:

```bash
# Stop current version
sudo systemctl stop rag-app

# Restore backup
cp org.simple.rag-1.0.0.jar.backup org.simple.rag.jar
cp embedding.properties.backup embedding.properties
cp llm.properties.backup llm.properties

# Start previous version
sudo systemctl start rag-app

# Verify
sudo systemctl status rag-app
```

---

**Last Updated**: March 19, 2026
