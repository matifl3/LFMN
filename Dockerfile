# ============================================
# LFM Nacional - Dockerfile
# ============================================

# Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copiar Maven wrapper y pom.xml primero (mejora cache)
COPY lfmNacional/mvnw lfmNacional/pom.xml ./
COPY lfmNacional/.mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY lfmNacional/src src

# Build
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Crear directorios
RUN mkdir -p /app/sesiones /app/archivos

# Copiar JAR
COPY --from=build /app/target/lfmNacional-0.0.1-SNAPSHOT.jar app.jar

# Puerto
EXPOSE 8080

# Health check (usa $PORT: Render lo inyecta; en local cae a 8080)
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -q --spider "http://localhost:${PORT:-8080}/actuator/health" || exit 1

# Ejecutar
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
