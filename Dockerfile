# ─── Build Stage ──────────────────────────────────────────────────────────────
# Uses official Maven + Temurin JDK 17 on Alpine for a minimal build image.
# Dependencies are cached in a separate layer to speed up rebuilds.
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy only pom.xml first — allows Docker layer caching of dependencies
COPY pom.xml .
RUN mvn dependency:go-offline --batch-mode --no-transfer-progress -q

# Copy source and build (tests are skipped: they run in the CI pipeline)
COPY src ./src
RUN mvn package --batch-mode --no-transfer-progress -DskipTests -q

# ─── Runtime Stage ────────────────────────────────────────────────────────────
# eclipse-temurin:17-jre-alpine is a minimal JRE image (~90 MB compressed).
# No JDK, no Maven, no source code in the final image.
FROM eclipse-temurin:17-jre-alpine AS runtime

# Create a dedicated non-root system user and group for the application.
# Running as root inside a container is a security anti-pattern.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the final JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Ensure the appuser owns the JAR
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Document the port the application listens on (Spring Boot default: 8080)
EXPOSE 8080

# JVM tuning for containers:
#   -XX:+UseContainerSupport          — respect cgroup CPU/memory limits
#   -XX:MaxRAMPercentage=75.0         — use 75% of container RAM for heap
#   -Djava.security.egd=file:/dev/./urandom — faster startup on Linux containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
