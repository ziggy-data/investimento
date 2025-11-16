# --- ESTÁGIO 1: Build (O Construtor) ---
# Usa uma imagem oficial do Maven com Java 21 para COMPILAR o código
FROM maven:3.9-eclipse-temurin-21 AS builder

# Define o diretório de trabalho dentro do container
WORKDIR /app

# 1. Copia o pom.xml e baixa as dependências (Camada de Cache)
# Isso é mais rápido do que copiar tudo de uma vez.
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. Copia o resto do código-fonte
COPY src ./src

# 3. Compila o projeto e gera o .jar, pulando os testes
# (Testes devem ser rodados no pipeline de CI, não no build do Docker)
RUN mvn clean package -DskipTests


# --- ESTÁGIO 2: Run (A Imagem Final) ---
# Usa uma imagem JRE (Java Runtime Environment) mínima e segura
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Define a porta que a aplicação vai expor
EXPOSE 8080

# Copia APENAS o .jar compilado do estágio "builder"
# O nome do .jar vem do seu pom.xml (<artifactId> e <version>)
COPY --from=builder /app/target/investimento-0.0.1-SNAPSHOT.jar app.jar

# Comando para executar a aplicação quando o container iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]