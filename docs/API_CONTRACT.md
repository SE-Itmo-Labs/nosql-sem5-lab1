# REST API личного кабинета кинотеатра

Документ описывает фактически реализованные методы Spring Boot backend.
Локальный адрес: `http://localhost:16767`, общий префикс: `/api/v1`.

## Общие правила

- запросы и ответы используют `application/json`;
- дата и время передаются в UTC в формате ISO 8601;
- все методы, кроме входа и Swagger, требуют заголовки `X-Username` и
  `X-Password`;
- тестовый клиент: `client01` / `client123`;
- тестовый администратор: `admin` / `123`.

JWT и Spring Security не используются. После проверки заголовков текущий
пользователь доступен контроллеру через атрибут запроса.

Типовой ответ с ошибкой:

```json
{
  "timestamp": "2026-10-08T12:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/notifications"
}
```

Основные статусы: `400` — неверный запрос, `401` — ошибка входа, `404` — объект
не найден, `409` — ресурс уже заблокирован, `500` — внутренняя ошибка.

## Авторизация и пользователь

### `POST /api/v1/auth/login`

Проверяет тестовую учётную запись. Заголовки авторизации не нужны.

```json
{
  "username": "client01",
  "password": "client123"
}
```

Ответ `200`:

```json
{
  "userId": 1,
  "username": "client01",
  "displayName": "Тестовый клиент",
  "role": "ROLE_USER"
}
```

### `GET /api/v1/users/me`

Возвращает текущего пользователя. Формат ответа совпадает с ответом входа.

## Уведомления

| Метод | URL | Результат |
| --- | --- | --- |
| `GET` | `/api/v1/notifications` | уведомления текущего пользователя |
| `GET` | `/api/v1/notifications/{id}` | уведомление по ID |
| `POST` | `/api/v1/notifications` | создать и отправить уведомление |

Создание уведомления:

```json
{
  "userId": "client01",
  "title": "Сеанс скоро начнётся",
  "text": "Зал 4, ряд 7, место 12",
  "categoryId": 2
}
```

Ответ `201` содержит `id`, поля запроса, статус `SENT` и `createdAt`. Метод
списка возвращает JSON-массив от новых уведомлений к старым.

## Категории и кэш

| Метод | URL | Результат |
| --- | --- | --- |
| `GET` | `/api/v1/categories` | список категорий |
| `GET` | `/api/v1/categories/{id}` | категория и информация о кэше |
| `POST` | `/api/v1/categories` | новая категория, ответ `201` |
| `PUT` | `/api/v1/categories/{id}` | обновлённая категория |
| `DELETE` | `/api/v1/categories/{id}` | удаление, ответ `204` |

Тело создания и обновления:

```json
{
  "name": "Бронирование",
  "description": "Изменения состояния билета"
}
```

Контрольный `GET` показывает источник данных:

```json
{
  "category": {
    "id": 2,
    "name": "Бронирование",
    "description": "Изменения состояния билета"
  },
  "cache": {
    "source": "CACHE",
    "cached": true,
    "remainingTtlSeconds": 278
  }
}
```

При первом чтении `source` равен `DATABASE`, при повторном — `CACHE`.

## Временная блокировка с TTL

| Метод | URL | Результат |
| --- | --- | --- |
| `POST` | `/api/v1/blocks` | создать блокировку, ответ `201` |
| `GET` | `/api/v1/blocks/{resourceKey}` | состояние и оставшийся TTL |
| `DELETE` | `/api/v1/blocks/{resourceKey}` | снять свою блокировку, ответ `204` |

Тело создания:

```json
{
  "resourceKey": "screening:15:seat:7-12",
  "owner": "client01",
  "ttlSeconds": 300
}
```

Ответ содержит `resourceKey`, `owner`, исходный и оставшийся TTL, `createdAt`,
`expiresAt` и `active`. Повторное создание того же ключа возвращает `409` и не
изменяет владельца или TTL. При досрочном удалении владельцем считается текущий
пользователь из заголовков.

## Распределённая блокировка

### `POST /api/v1/locks/execute`

Пытается выполнить операцию внутри критической секции Redisson.

```json
{
  "resourceKey": "notification:41:delivery",
  "holdMillis": 1000
}
```

`holdMillis` может быть от `0` до `10000`. Ответ:

```json
{
  "resourceKey": "notification:41:delivery",
  "owner": "client01",
  "acquired": true,
  "message": "Критическая секция выполнена"
}
```

При параллельном занятии ресурса запрос остаётся успешным по HTTP, но получает
`acquired: false`.

## Исследование согласованности Redis

| Метод | URL | Назначение |
| --- | --- | --- |
| `GET` | `/api/v1/consistency/state` | выбранные режимы чтения и записи |
| `PUT` | `/api/v1/consistency/mode` | изменить один или оба режима |
| `POST` | `/api/v1/consistency/write` | записать значение в primary |
| `GET` | `/api/v1/consistency/read/{key}` | прочитать в выбранном режиме |
| `GET` | `/api/v1/consistency/nodes/{key}` | сравнить все три узла |
| `POST` | `/api/v1/consistency/experiments` | выполнить измеряемый эксперимент |
| `POST` | `/api/v1/consistency/replicas/{number}/detach` | отключить реплику 1 или 2 |
| `POST` | `/api/v1/consistency/replicas/{number}/attach` | подключить реплику обратно |

Изменение режима:

```json
{
  "readMode": "REPLICA_PREFERRED",
  "writeMode": "WAIT_FOR_REPLICAS"
}
```

Для чтения доступны `MASTER` и `REPLICA_PREFERRED`, для записи — `ASYNC` и
`WAIT_FOR_REPLICAS`. Поле, которое изменять не нужно, можно не передавать.

Простая запись:

```json
{
  "key": "demo",
  "value": "version-1"
}
```

Один эксперимент со своими параметрами:

```json
{
  "key": "demo",
  "value": "version-2",
  "readTarget": "REPLICA",
  "mode": "EVENTUAL",
  "waitTimeoutMs": 500
}
```

`readTarget` принимает `PRIMARY` или `REPLICA`, а `mode` — `EVENTUAL` или
`WAIT_FOR_REPLICA`. Ответ содержит записанное и прочитанное значения, узел,
число подтверждений, `consistent` и длительности записи, ожидания и чтения.

## Swagger

- Swagger UI: <http://localhost:16767/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:16767/v3/api-docs>

В Swagger два глобальных поля авторизации соответствуют заголовкам
`X-Username` и `X-Password`.
