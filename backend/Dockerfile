# Etapa de construcción
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copiar archivos de Gradle
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# Descargar dependencias (cacheable)
RUN gradle dependencies --no-daemon

# Copiar código fuente
COPY src ./src

# Construir aplicación
RUN gradle bootJar --no-daemon -x test

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copiar JAR desde etapa de construcción
COPY --from=build /app/build/libs/prode-k2-mundial-1.0.0.jar app.jar

# Cambiar propietario
RUN chown -R appuser:appgroup /app

USER appuser

# Puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Comando de ejecución
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]