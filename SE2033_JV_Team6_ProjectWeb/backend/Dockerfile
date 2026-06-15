# Multi-stage build for Java 21 backend
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom file
COPY pom.xml .

# Download dependencies
RUN mvn dependency:resolve

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy jar from builder
COPY --from=builder /app/target/backend-*.jar backend.jar

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8082/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "backend.jar"]

