# syntax=docker/dockerfile:1

# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven descriptor and resolve deps first (better layer caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw && ./mvnw -q -B -e -DskipTests dependency:go-offline

# Copy sources and build
COPY src src
RUN ./mvnw -q -B -DskipTests package

# ===== Run stage =====
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Non-root user
RUN useradd -r -u 1001 appuser
USER appuser

# Copy fat jar from build stage
# Adjust the JAR name if your finalName differs
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

# JVM and Spring defaults for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError" \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=UTC

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]