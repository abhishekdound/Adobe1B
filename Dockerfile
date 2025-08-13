# Dockerfile
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Create uploads directory
RUN mkdir -p /app/uploads

# Copy Maven wrapper and pom.xml first (for better layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Environment variables for Adobe Challenge requirements
ENV LLM_PROVIDER=gemini
ENV LLM_MODEL=gemini-2.5-flash
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/creds/google-creds.json
ENV AZURE_TTS_KEY=${AZURE_TTS_KEY}
ENV AZURE_TTS_ENDPOINT=${AZURE_TTS_ENDPOINT}
ENV TTS_PROVIDER=azure

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/analysis/health || exit 1

# Run the application bound to 0.0.0.0 for external access
CMD ["java", "-Dserver.address=0.0.0.0", "-Dserver.port=8080", "-jar", "target/Adobe1B.jar"]