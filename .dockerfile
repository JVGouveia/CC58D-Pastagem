# Stage 1: Build - usa imagem do Maven para compilar
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o pom.xml e o código-fonte para o container
COPY pom.xml .
COPY src ./src

# Executa o build do projeto, pulando os testes para agilizar
RUN mvn clean package -DskipTests

# Stage 2: Runtime - usa imagem leve com JRE para rodar o .jar
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia o arquivo .jar compilado do estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que a aplicação vai usar
EXPOSE 3001

# Comando para executar a aplicação Java
ENTRYPOINT ["java", "-jar", "app.jar"]
