# Etapa 1: Build de la aplicación con Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar solo pom.xml primero (para aprovechar caché de Docker)
COPY pom.xml .

# Descargar dependencias (esta capa se cachea)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Construir la aplicación (skip tests para build más rápido)
RUN mvn clean package -DskipTests -B

# Etapa 2: Imagen runtime con JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/ZGZEmergencyMapBack-*.jar app.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]