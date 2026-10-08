## Запуск бэкенда (этап 1)

Для сборки нужен JDK 25. Gradle использует `JAVA_HOME`; в IDE также выберите JDK 25 для Gradle JVM.
Проверка установленного компилятора: `"$JAVA_HOME/bin/javac" --version`.

1. Скопируйте `credentials.env.example` в `credentials.env` и заполните настройки PostgreSQL.
2. Соберите приложение: `./gradlew clean build`. Исполняемый файл: `build/libs/app.jar`.
3. Запустите: `docker compose --env-file credentials.env up -d --build`.

Swagger: http://localhost:16767/swagger-ui/index.html
OpenAPI: http://localhost:16767/v3/api-docs

Для локального запуска приложения вне Docker поднимите только БД:

```bash
docker compose --env-file credentials.env up -d redis-db postgresql
set -a
source credentials.env
set +a
SERVER_PORT=16767 ./gradlew bootRun
```

Команда `source` рассчитана на Bash и доверенный файл с shell-совместимыми значениями.
Сам Spring Boot файл `credentials.env` автоматически не читает.
Локально используются `localhost` и порты `POSTGRES_PORT` / `REDIS_PORT` (по умолчанию 5432 / 6379).
В Docker подключения переопределены именами сервисов и внутренними портами.
Compose ждёт готовности обеих БД. Существующие каталоги Redis и volume PostgreSQL сохранены.

Проверка демо-входа:

```bash
curl -i http://localhost:16767/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123"}'
curl -i http://localhost:16767/api/v1/hello \
  -H 'X-Username: admin' -H 'X-Password: 123'
```

Повторный `/api/v1/hello` читает значение из Redis с TTL 60 секунд.
Старый `/hello` временно сохранён для существующего фронтенда.
Spring Security и JWT не используются; текущая проверка `admin/123` остаётся демо-заготовкой.
Swagger и preflight OPTIONS доступны без заголовков входа.
Разрешённые origin можно изменить переменной `CORS_ALLOWED_ORIGINS` (список через запятую).

Учебные классы и пустые заготовки сохранены. `TestRepository` помечен `@NoRepositoryBean`,
поскольку `Integer` не является JPA-сущностью и такой пример не должен создаваться при запуске.
Категории, уведомления и пользователи ещё не реализованы полностью; их базовый префикс — `/api/v1`.
Разделы ниже и `PLAN.md` содержат исторические наброски. Итоговый отчёт в `docs/` оформляется в конце.
