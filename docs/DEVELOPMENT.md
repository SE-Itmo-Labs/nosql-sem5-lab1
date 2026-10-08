# Запуск и разработка

Проект состоит из Spring Boot backend, Nuxt frontend, PostgreSQL и трёх
экземпляров Redis: primary и двух реплик.

## Требования

- JDK 25;
- Docker с Docker Compose;
- Node.js 22.19 или актуальная версия Node.js 24;
- npm.

Проверить активную версию Java можно командами:

```bash
javac --version
./gradlew --version
```

## Полный запуск через Docker

Создайте файл с параметрами PostgreSQL и запустите сборку:

```bash
cp credentials.env.example credentials.env
./gradlew clean build
docker compose --env-file credentials.env up -d --build
docker compose --env-file credentials.env ps
```

| Сервис | Адрес или порт |
| --- | --- |
| Spring Boot API | <http://localhost:16767> |
| PostgreSQL | `localhost:5432` |
| Redis primary | `localhost:6379` |
| Redis replica 1 | `localhost:6380` |
| Redis replica 2 | `localhost:6381` |

Логи приложения:

```bash
docker compose --env-file credentials.env logs -f spring-app
```

## Тестовые пользователи

При первом запуске backend создаёт две учётные записи:

| Роль | Логин | Пароль |
| --- | --- | --- |
| Клиент | `client01` | `client123` |
| Администратор | `admin` | `123` |

Регистрация, JWT и Spring Security в лабораторной не используются. Защищённые
запросы принимают два заголовка:

```text
X-Username: client01
X-Password: client123
```

Проверка входа:

```bash
curl -X POST http://localhost:16767/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"client01","password":"client123"}'
```

## Swagger

- Swagger UI: <http://localhost:16767/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:16767/v3/api-docs>

В Swagger нажмите `Authorize` и заполните `X-Username` и `X-Password`. Метод
входа можно вызвать без этих данных.

## Тесты

```bash
./gradlew test
```

Интеграционные тесты используют Testcontainers, поэтому для них должен работать
Docker. Тестовый Redis создаётся отдельно и не изменяет локальные данные проекта.

## Локальный запуск backend

PostgreSQL и Redis удобнее оставить в Docker, а Spring Boot запустить из IDE или
Gradle. Адреса по умолчанию соответствуют портам из таблицы выше.

```bash
docker compose --env-file credentials.env up -d postgresql redis-db redis-replica-1 redis-replica-2
set -a
source credentials.env
set +a
SERVER_PORT=16767 ./gradlew bootRun
```

## Frontend

```bash
cd nosql-sem5-lab1-frontend
cp .env.example .env
npm ci
npm run dev
```

Для работы без backend:

```dotenv
NUXT_PUBLIC_API_MODE=mock
NUXT_PUBLIC_API_BASE=http://localhost:16767
```

Для запросов к Spring Boot:

```dotenv
NUXT_PUBLIC_API_MODE=real
NUXT_PUBLIC_API_BASE=http://localhost:16767
```

## Проверка Redis

```bash
docker exec -it redis_container redis-cli
docker exec -it redis_replica1_container redis-cli
docker exec -it redis_replica2_container redis-cli
```

Команда `INFO replication` показывает роль узла и состояние подключения реплик.

Для ручной проверки персистентности можно записать контрольный ключ, подождать
не меньше секунды, перезапустить primary и прочитать ключ:

```bash
docker exec redis_container redis-cli SET persistence:test saved
docker compose restart redis-db
docker exec redis_container redis-cli GET persistence:test
```

Команда `docker compose down -v` удаляет Docker volumes. Данные primary также
хранятся в подключённой папке `redis_data/`, поэтому её не следует удалять перед
проверкой восстановления.

Подробности приведены в [контракте REST API](API_CONTRACT.md),
[архитектуре backend](BACKEND_ARCHITECTURE.md) и
[отчёте](LAB_REPORT.md).
