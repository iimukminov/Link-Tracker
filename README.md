## Запуск проекта

Проект состоит из двух микросервисов: **Bot** (интерфейс пользователя) и **Scrapper** (логика отслеживания). Для корректной работы должны быть запущены оба.

### 1. Подготовка

* Получите токен для бота у [@BotFather](https://t.me/BotFather) через команду `/newbot`.
* (Опционально) Получите GitHub Personal Access Token для работы с API без жестких лимитов.

### 2. Настройка переменных окружения

Для запуска в IDE (IntelliJ IDEA) добавьте следующие переменные в **Run/Debug Configurations**:

#### Для модуля `Bot`:

* `TELEGRAM_BOT_TOKEN` — ваш токен от BotFather.

#### Для модуля `Scrapper`:

* `GITHUB_TOKEN` — ваш GitHub PAT.
* `STACKOVERFLOW_KEY` — ключ API StackOverflow.
* `STACKOVERFLOW_ACCESS_KEY` — токен доступа API StackOverflow (добавьте при необходимости).

### 3. Запуск базы данных (PostgreSQL)

Для локальной разработки используется PostgreSQL. Перед запуском приложения необходимо поднять базу данных и накатить миграции через Docker Compose:

```bash
docker compose up -d
```

Убедитесь, что контейнеры базы данных и миграций (`postgresql` и `liquibase-migrations`) успешно запустились.

### 4. Сборка проекта

Перед первым запуском (или после изменений) необходимо собрать проект:

```bash
mvn clean install -DskipTests
```

*Примечание: Сборка `.jar` файлов обязательна, так как они используются в конфигурации Testcontainers при прогоне тестов.*

### 5. Запуск сервисов

Запустите приложения из вашей IDE в следующем порядке:

1. **Scrapper**: `backend.academy.linktracker.scrapper.ScrapperApplication`
2. **Bot**: `backend.academy.linktracker.bot.BotApplication`

### 6. Особенности реализации (access-type)

В сервисе Scrapper реализовано два подхода работы с БД: чистый SQL (JDBC) и ORM (JPA). Переключать их можно через конфигурацию в `application.yml` или переменную окружения:
* `app.database.access-type=JDBC`
* `app.database.access-type=JPA`
