FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

# Копируем структуру проекта
COPY pom.xml .
COPY bot/pom.xml bot/
COPY scrapper/pom.xml scrapper/
COPY ai-agent/pom.xml ai-agent/
COPY build-report-aggregate/pom.xml build-report-aggregate/

# Копируем контракты (обязательно для генерации Avro)
COPY api-contracts/pom.xml api-contracts/
COPY api-contracts/src api-contracts/src

# Если у тебя есть модуль common, его тоже нужно скопировать для успешной сборки
COPY common/pom.xml common/
COPY common/src common/src

# Скачиваем зависимости только для ai-agent
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl ai-agent -am

# Копируем исходники ai-agent и собираем
COPY ai-agent/src ai-agent/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl ai-agent -am -DskipTests

# --- Слой распаковки ---
FROM eclipse-temurin:25-jre-alpine AS extractor
WORKDIR /app
# Берем jar-файл из папки ai-agent
COPY --from=builder /app/ai-agent/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# --- Финальный образ ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S botgroup && adduser -S botuser -G botgroup
USER botuser

COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
