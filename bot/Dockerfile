# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

# 1. Копируем родительский POM
COPY pom.xml .

# 2. Копируем POM-файлы всех модулей проекта
COPY bot/pom.xml bot/
COPY scrapper/pom.xml scrapper/
COPY ai-agent/pom.xml ai-agent/
COPY build-report-aggregate/pom.xml build-report-aggregate/
COPY api-contracts/pom.xml api-contracts/

# 3. Кэшируем зависимости
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl bot -am

# 4. Копируем исходный код бота и собираем
COPY bot/src bot/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl bot -am -DskipTests


# --- Слой распаковки (layertools) ---
FROM eclipse-temurin:25-jre-alpine AS extractor
WORKDIR /app
COPY --from=builder /app/bot/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract


# --- Финальный легковесный образ ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Создаем непривилегированного пользователя для безопасности
RUN addgroup -S botgroup && adduser -S botuser -G botgroup
USER botuser

# Копируем слои приложения (от редко изменяемых к часто изменяемым)
COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
