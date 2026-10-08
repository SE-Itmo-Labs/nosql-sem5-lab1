# Запуск и разработка

Проект состоит из Spring Boot backend, Nuxt frontend, PostgreSQL и Redis.
Пока backend находится в разработке, frontend может работать
независимо в явном `mock`-режиме.

## Требования

- Java 21;
- Node.js 22.19 или актуальная версия Node.js 24;
- Docker с Compose;
- npm.

## Frontend с демонстрационными данными

```bash
cd nosql-sem5-lab1-frontend
cp .env.example .env
npm ci
npm run dev
```

В `.env` выберите локальный источник данных:

```dotenv
NUXT_PUBLIC_API_MODE=mock
NUXT_PUBLIC_API_BASE=http://localhost:16767
```

Демонстрационная учетная запись: логин `client01`, пароль `client123`.
Mock-режим не отправляет сетевые запросы, но использует тот же `ApiClient`, что
и HTTP-реализация.

## Инфраструктура и backend

1. Создайте `credentials.env` по примеру `crendentials.env.example`.
2. Укажите `POSTGRES_USER`, `POSTGRES_PASSWORD` и `POSTGRES_DB`.
3. Соберите приложение и запустите контейнеры.

```bash
./gradlew build
docker compose --env-file credentials.env up --build --force-recreate -d
```

Для подключения frontend к Spring Boot измените `.env`:

```dotenv
NUXT_PUBLIC_API_MODE=real
NUXT_PUBLIC_API_BASE=http://localhost:16767
```

В режиме `real` ошибки backend не подменяются локальными ответами: проблемы
интеграции остаются видимыми во время разработки.

## Основные команды

### Frontend

```bash
cd nosql-sem5-lab1-frontend
npm run dev       # сервер разработки
npm run build     # production-сборка
npm run generate  # статическая версия для GitHub Pages
```

### Backend

```bash
./gradlew build
./gradlew bootRun
```

### Docker

```bash
docker compose --env-file credentials.env up -d
docker compose ps
docker compose logs -f spring-app
```

## Redis CLI

Команда подключения зависит от имени контейнера в `docker-compose.yml`. Для
основного экземпляра:

```bash
docker exec -it redis_container redis-cli
```

## Полезные адреса

| Назначение | Адрес |
| --- | --- |
| Nuxt в режиме разработки | <http://localhost:3000/nosql-sem5-lab1/> |
| Spring Boot API | <http://localhost:16767> |
| Swagger UI | <http://localhost:16767/swagger-ui/index.html> |
| Опубликованный frontend | <https://se-itmo-labs.github.io/nosql-sem5-lab1/> |

Описание запросов находится в [контракте REST API](API_CONTRACT.md).
