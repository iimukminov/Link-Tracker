# Наблюдаемость (Observability)

Проект отдает Prometheus-метрики из сервисов `scrapper`, `bot` и `ai-agent`, собирает их через Prometheus и автоматически настраивает дашборд и алерты в Grafana.

По умолчанию в системе используется **Pull-модель**: Prometheus самостоятельно читает эндпоинты `/metrics` напрямую у приложений. Инфраструктура также полностью поддерживает **Push-модель** через Pushgateway. 

Во избежание дублирования данных (завышения counters и gauges при одновременной работе обеих моделей), отправка метрик в Pushgateway по умолчанию выключена переменной `PUSHGATEWAY_ENABLED=false`. Дашборды спроектированы универсально и не содержат жесткой привязки к `instance`, поэтому они будут корректно работать при любом выбранном подходе к сбору метрик.

## Запуск

Для старта всей инфраструктуры в стандартном режиме (Pull-модель) выполните команду:

```bash
docker compose up -d --build

```

### Доступные сервисы:

* API Scrapper: `http://localhost:8081`
* Метрики Scrapper: `http://localhost:8081/metrics`
* API Bot: `http://localhost:8011`
* Метрики Bot: `http://localhost:8011/metrics`
* API AI-Agent: `http://localhost:8083`
* Метрики AI-Agent: `http://localhost:8083/metrics`
* Prometheus: `http://localhost:9090`
* Pushgateway: `http://localhost:9091`
* Grafana: `http://localhost:3000` (логин/пароль по умолчанию: `admin` / `admin`)

Секреты и порты читаются из файла `.env` (на базе `.env_dist`).

### Включение Push-модели (Pushgateway):

Чтобы переключить систему на сбор метрик через Pushgateway (например, для проверки доп. критериев), выполните следующие шаги:

1. В файле `.env` установите флаг отправки метрик в значение `true`:
```env
PUSHGATEWAY_ENABLED=true

```
2. Откройте файл `prometheus/prometheus.yml` и **закомментируйте** прямые Pull-джобы для приложений (`scrapper`, `bot`, `ai-agent`), оставив активным только сбор из самого `pushgateway`. Это необходимо, чтобы Prometheus не собирал одни и те же метрики двумя путями одновременно и данные на дашбордах не завышались.
3. Перезапустите контейнеры:
```bash
docker compose up -d --build

```

---

## 2. Описание подключенных метрик

Помимо стандартных JVM и HTTP метрик, предоставляемых `spring-boot-starter-actuator` (настроены через Micrometer), в приложениях реализован сбор следующих кастомных бизнес-метрик:

### Модуль `scrapper`

* `links_on_track` (Gauge) — количество активных ссылок в БД, поставленных на мониторинг. Разделено по доменам через лейбл `tracked_source`. Обновляется в памяти при добавлении/удалении.
* `request_duration_ms` (DistributionSummary) — длительность операций в миллисекундах. Лейбл `scope` принимает значения:
* `database` — запросы к БД (сбор через AOP). Лейбл `scope_type` содержит имя таблицы (например, `chat`, `link`).
* `external_source` — запросы к внешним источникам. Лейбл `scope_type` равен домену (`github`, `stackoverflow`).


* `api_requests` (Counter) — количество входящих запросов к API Scrapper. Лейбл `source` содержит имя контроллера.

### Модуль `bot`

* `command_requests` (Counter) — количество обработанных команд бота. Лейбл `command` содержит имя команды.
* `command_duration_ms` (DistributionSummary) — длительность синхронных/асинхронных походов в API Scrapper (лейбл `scope="scrapper_sync_api"`).
* `command_handling_duration_ms` (DistributionSummary) — полная длительность выполнения логики команды внутри бота.
* `telegram_requests` (Counter) — количество входящих событий из Telegram по типам (лейбл `request_type`).
* `sent_notification` (Counter) — успешно отправленные нотификации пользователям. Увеличивается только после успешного ответа Telegram API.

### Модуль `ai-agent`

* `ai_agent_filtered_updates` (Counter) — количество отфильтрованных апдейтов с указанием причины в лейбле `reason`.
* `request_duration_ms` (DistributionSummary) — длительность отправки данных в Kafka (`scope="kafka_send"`) и вызовов к LLM API (`scope="llm_api"`).

Также отдаются стандартные метрики Micrometer, включая `http_server_requests_seconds_*` (для RED-метрик) и `jvm_memory_used_bytes` (для контроля RAM).

---

## 3. Дашборды и Алерты

Дашборд и алерты автоматически подхватываются из директории:

* `monitoring/dashboards/dashboard.json`
* `monitoring/alerts/ram_alert.yml`

Дашборд параметризован и содержит переменную `$application` для фильтрации панелей по конкретному микросервису.
Алерт срабатывает, если использование Heap-памяти JVM выбранного приложения (например, `scrapper`) превышает установленный порог (в МБ) на протяжении заданного времени.

---

## 4. PromQL

Ниже перечислены основные запросы, на которых построены панели дашборда (также представлены в `example_pql.txt`).

| Панель | Назначение | PromQL |
| --- | --- | --- |
| **HTTP Requests Rate** | RPS по приложениям | `sum(rate(http_server_requests_seconds_count{application=~"$application"}[1m]))` |
| **HTTP Errors (5xx)** | Частота ошибок сервера | `sum(rate(http_server_requests_seconds_count{status=~"5..", application=~"$application"}[1m]))` |
| **HTTP Latency p95** | 95-й перцентиль времени ответа | `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application=~"$application"}[1m])) by (le))` |
| **JVM Heap Memory** | Использование ОЗУ (в МБ) | `sum(jvm_memory_used_bytes{area="heap", application=~"$application"}) / 1024 / 1024` |
| **Tracked Links** | Активные ссылки по доменам | `sum(links_on_track) by (tracked_source)` |
| **Scrape Latency p95** | p95 операций парсинга | `histogram_quantile(0.95, sum(rate(request_duration_ms_milliseconds_bucket{scope="external_source"}[5m])) by (le, scope_type))` |
| **Incoming Messages** | Скорость входящих сообщений (текст) | `sum(rate(telegram_requests_total{request_type="text_message"}[1m]))` |
| **Bot Commands Rate** | Скорость запросов по командам | `sum(rate(command_requests_total[1m])) by (command)` |
| **Bot Command p95** | p95 полной обработки команды | `histogram_quantile(0.95, sum(rate(command_handling_duration_ms_milliseconds_bucket[5m])) by (le, command))` |
| **Notifications Sent** | График отправленных нотификаций | `sum(increase(sent_notification_total[1m]))` |

---

## 5. Сборка и публикация Docker-образов

Для ручной сборки и публикации образов в registry используются следующие команды:

```bash
# Задание переменных окружения
export REGISTRY="[registry.example.com/link-tracker](https://registry.example.com/link-tracker)"
export IMAGE_TAG="latest"

# Авторизация в Registry
docker login $REGISTRY

# Сборка образов
docker build -f scrapper/Dockerfile -t "$REGISTRY/scrapper:$IMAGE_TAG" .
docker build -f bot/Dockerfile -t "$REGISTRY/bot:$IMAGE_TAG" .
docker build -f ai-agent/Dockerfile -t "$REGISTRY/ai-agent:$IMAGE_TAG" .

# Публикация образов
docker push "$REGISTRY/scrapper:$IMAGE_TAG"
docker push "$REGISTRY/bot:$IMAGE_TAG"
docker push "$REGISTRY/ai-agent:$IMAGE_TAG"

```
