# Translation Service

Небольшой REST-сервис для перевода текста через неофициальный Google Translate API. Каждое слово обрабатывается в собственном потоке, все переводы сохраняются в PostgreSQL вместе с IP клиента.

## Стек

- **Java 21**, **Spring Boot 3.3**
- **Spring Web**, **Spring Data JPA**, **Bean Validation**
- **PostgreSQL 17**, **Flyway** для миграций
- **Gradle**, **Docker Compose**
- **Lombok**, **Jackson**

## Архитектура

```
controller  →  service  →  translationProcess  →  Google Translate
                  ↓
             repository  →  PostgreSQL
```

- `TranslationController` - HTTP-слой: валидация параметров, резолв клиентского IP (с учётом `X-Forwarded-For`).
- `TranslationService` - бизнес-логика: проверка поддерживаемых языков, сохранение результата.
- `TranslationProcess` - параллельный перевод слов через управляемый `ExecutorService` (пул 10 потоков) + `CompletableFuture.allOf`.
- `GlobalExceptionHandler` - единая обработка ошибок (валидация, 405, 502 от внешнего API, fallback 500).

## Запуск

### Через Docker Compose

```bash
export POSTGRES_PASSWORD=your_password
export PGADMIN_PASSWORD=your_password
docker-compose up --build
```

Приложение поднимется на `http://localhost:8080`, PostgreSQL - на `5432`, pgAdmin - на `5050`.

### Локально

Требования: JDK 21, работающий PostgreSQL.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/translations
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
./gradlew bootRun
```

## API

### `GET /translate`

| Параметр         | Тип      | Обязательный | Описание                                       |
|------------------|----------|:------------:|------------------------------------------------|
| `words`          | `String` |      ✅      | Текст для перевода (не пустой, до 5000 симв.)  |
| `sourceLanguage` | `String` |      ✅      | ISO-код исходного языка (например, `en`)       |
| `targetLanguage` | `String` |      ✅      | ISO-код целевого языка (например, `ru`)        |

**Пример запроса:**
```
GET /translate?words=Hello%20world&sourceLanguage=en&targetLanguage=ru
```

**Ответ `200 OK`:**
```json
{ "text": "Привет мир" }
```

### Коды ошибок

| Код | Причина                                                       |
|-----|---------------------------------------------------------------|
| 400 | Пустой `words`, неподдерживаемый язык, некорректные параметры |
| 405 | Любой метод кроме `GET`                                       |
| 502 | Google Translate недоступен или вернул некорректный ответ    |
| 500 | Непредвиденная ошибка                                         |

## Схема БД

Таблица `translations`:

| Поле              | Тип           | Описание                    |
|-------------------|---------------|-----------------------------|
| `id`              | `BIGSERIAL`   | Первичный ключ              |
| `client_ip`       | `VARCHAR(45)` | IP клиента (поддержка IPv6) |
| `original_text`   | `TEXT`        | Исходный текст              |
| `translated_text` | `TEXT`        | Перевод                     |

Миграции Flyway лежат в `src/main/resources/db/migration/`.

## Переменные окружения

| Переменная          | Значение по умолчанию | Описание                          |
|---------------------|-----------------------|-----------------------------------|
| `DB_URL`            | локальный PG          | JDBC-URL                          |
| `DB_USERNAME`       | `postgres`            | Пользователь БД                   |
| `DB_PASSWORD`       | -                     | Пароль БД (обязателен в проде)    |
| `POSTGRES_PASSWORD` | -                     | Пароль для контейнера Postgres    |
| `PGADMIN_PASSWORD`  | -                     | Пароль для pgAdmin                |

## Тесты

```bash
./gradlew test
```
