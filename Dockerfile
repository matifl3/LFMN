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
RUN mkdir -p /app/files /app/sesiones /app/archivos

# Copiar JAR
COPY --from=build /app/target/lfmNacional-0.0.1-SNAPSHOT.jar app.jar

# Copiar frontend
COPY files/ /app/files/

# Puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -q --spider http://localhost:8080/ || exit 1

# Ejecutar
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
