## Запуск проекта

Проект состоит из двух микросервисов: **Bot** (интерфейс пользователя) и **Scrapper** (логика отслеживания). Для корректной работы должны быть запущены оба.

### 1. Подготовка

* Получите токен для бота у [@BotFather](https://t.me/BotFather) через команду `/newbot`.

* (Опционально) Получите GitHub Personal Access Token для работы с API без жестких лимитов.

### 2. Настройка переменных окружения

Для запуска в IDE (IntelliJ IDEA) добавьте следующие переменные в **Run/Debug Configurations**:

#### Для модуля `Bot`:

* `TELEGRAM_BOT_TOKEN` — ваш токен от BotFather.

* `APP_SCRAPPER_BASE_URL` — адрес сервиса Scrapper (по умолчанию `http://localhost:8081`).

#### Для модуля `scrapper`:

* `GITHUB_TOKEN` — ваш GitHub PAT.

* `STACKOVERFLOW_KEY` — ключ API StackOverflow.

### 3. Запуск сервисов

Сначала необходимо собрать проект:

```bash
mvn clean install -DskipTests
```

Затем запустите приложения в следующем порядке:

1. **Scrapper**: `backend.academy.linktracker.scrapper.ScrapperApplication`

2. **Bot**: `backend.academy.linktracker.bot.BotApplication`

