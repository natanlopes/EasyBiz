# --- Etapa 1: Build (Construção) ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia arquivos de configuração de dependência
COPY pom.xml .
COPY .mvn .mvn

# Baixa as dependências de forma segura e silenciosa (-q)
RUN mvn dependency:resolve -q

# Copia o código fonte e compila o projeto
COPY src ./src
RUN mvn clean package -DskipTests -q

# --- Etapa 2: Run (Execução Final) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia apenas o JAR gerado na etapa anterior (Otimização de tamanho)
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão
EXPOSE 8080

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]