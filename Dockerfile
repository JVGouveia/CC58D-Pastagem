# Stage 1: Build da aplicação
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom.xml e baixar dependências para cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar todo o código-fonte e compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime da aplicação
FROM eclipse-temurin:17-jre

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Criar usuário seguro
RUN addgroup --system spring && adduser --system --ingroup spring --home /home/spring spring

WORKDIR /app

# Copiar JAR do stage de build
COPY --from=build /app/target/Api1-0.0.1-SNAPSHOT.jar app.jar

# Configurar propriedades da JVM e timezone
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:+UseContainerSupport"
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Expor porta padrão da aplicação Spring
EXPOSE 8080

# Healthcheck para o Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Alternativa mais segura de entrypoint com JAVA_OPTS funcionando
USER spring:spring
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
