# syntax=docker/dockerfile:1

############################
# Stage 1 — Build
############################
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Resolve dependencies first so they stay cached unless pom.xml changes.
# The cache mount keeps the local Maven repo across builds (BuildKit).
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

# Build the application.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests

# Split the fat jar into Spring Boot layers for efficient Docker caching.
RUN java -Djarmode=layertools -jar target/*.jar extract --destination extracted

############################
# Stage 2 — Runtime
############################
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# curl is used by the container HEALTHCHECK below.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as an unprivileged user.
RUN groupadd --system spring && useradd --system --gid spring spring

# Copy layers from least- to most-frequently changing for better cache hits.
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

USER spring

EXPOSE 8080

# Container-aware heap sizing; override JAVA_OPTS at runtime if needed.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Health probe hits the Actuator endpoint (note the /api/v1 context-path).
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fs http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
