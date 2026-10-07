# API-контракт личного кабинета кинотеатра

Этот документ фиксирует целевой контракт между Nuxt-фронтендом и Spring Boot.
Он является договорённостью команды, а не описанием текущей степени готовности
backend. Состояние реализации на 8 октября 2026 года приведено в последнем разделе.

## Общие правила

- Базовый адрес задаётся переменной `NUXT_PUBLIC_API_BASE`. Локальное значение:
  `http://localhost:16767`.
- Все бизнес-методы находятся под `/api`; версия авторизации сохраняется в уже
  созданном backend-префиксе `/api/v1/auth`.
- Тела запросов и ответов передаются как `application/json`.
- Дата и время передаются в UTC в формате ISO 8601, например
  `2026-10-08T12:30:00Z`.
- Для неизвестного объекта backend отвечает `404`, для конфликта блокировки —
  `409`, для ошибки валидации — `400`, для отсутствующей авторизации — `401`.
- Ошибка имеет единый вид:

```json
{
  "timestamp": "2026-10-08T12:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/notifications"
}
```

## Авторизация

### `POST /api/v1/auth/login`

Вход существующего пользователя.

```json
{
  "username": "client01",
  "password": "secret123"
}
```

Успешный ответ `200`:

```json
{
  "token": "jwt-token",
  "type": "Bearer",
  "username": "client01",
  "email": "client01@example.com"
}
```

### `POST /api/v1/auth/register`

Создание клиента. Ответ `201` имеет ту же структуру, что и вход.

```json
{
  "username": "client01",
  "email": "client01@example.com",
  "password": "secret123"
}
```

После получения токена frontend передаёт его в заголовке
`Authorization: Bearer <token>`.

## Уведомления

Уведомление — основной объект варианта. Его создание одновременно означает
отправку клиенту и должно атомарно обновить индекс уведомлений пользователя.

| Метод | URL | Назначение |
| --- | --- | --- |
| `GET` | `/api/notifications` | Общий список с пагинацией и фильтрами |
| `GET` | `/api/notifications/user/{userId}` | Уведомления одного клиента |
| `GET` | `/api/notifications/{id}` | Одно уведомление |
| `POST` | `/api/notifications` | Создать и отправить уведомление |
| `PATCH` | `/api/notifications/{id}` | Изменить текст, категорию или статус |
| `DELETE` | `/api/notifications/{id}` | Удалить уведомление |
| `GET` | `/api/notifications/stats` | Агрегированная статистика |

Параметры списка: `page`, `size`, `sort`, `userId`, `categoryId`, `status`, `q`.
Поля `page` и `size` считаются от нуля и по умолчанию равны `0` и `20`.

Создание:

```json
{
  "userId": "client01",
  "title": "Сеанс скоро начнётся",
  "text": "Зал 4, ряд 7, место 12",
  "categoryId": 2
}
```

Ответ `201`:

```json
{
  "id": 41,
  "userId": "client01",
  "title": "Сеанс скоро начнётся",
  "text": "Зал 4, ряд 7, место 12",
  "categoryId": 2,
  "status": "SENT",
  "createdAt": "2026-10-08T12:30:00Z"
}
```

Ответ списка:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "last": true,
  "empty": true
}
```

## Категории и кэш

| Метод | URL | Назначение |
| --- | --- | --- |
| `GET` | `/api/categories` | Полный справочник категорий |
| `GET` | `/api/categories/{id}` | Категория с информацией о кэше |
| `POST` | `/api/categories` | Создать категорию и инвалидировать список |
| `PUT` | `/api/categories/{id}` | Обновить категорию и кэш |
| `DELETE` | `/api/categories/{id}` | Удалить категорию и запись кэша |

Контрольный `GET /api/categories/{id}` возвращает источник данных, чтобы на
защите можно было увидеть сначала cache miss, а затем cache hit:

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
    "remainingTtlSeconds": 54
  }
}
```

## Временная блокировка с TTL

| Метод | URL | Назначение |
| --- | --- | --- |
| `POST` | `/api/blocks` | Временно заблокировать ресурс |
| `GET` | `/api/blocks/{resourceKey}` | Проверить блокировку и оставшийся TTL |
| `DELETE` | `/api/blocks/{resourceKey}` | Досрочно освободить собственную блокировку |

Запрос на создание:

```json
{
  "resourceKey": "screening:15:seat:7-12",
  "owner": "client01",
  "ttlSeconds": 300
}
```

Успешный ответ `201` содержит `createdAt`, `expiresAt`, исходный
`ttlSeconds`, вычисленный `remainingTtlSeconds` и `active: true`. Если ключ уже
занят, backend возвращает `409`, не перезаписывая владельца и TTL.

После автоматического удаления Redis-ключа проверка отвечает `404`.

## Распределённая блокировка

### `POST /api/locks/acquire`

Атомарно выполняет `SET lock:{resourceKey} <token> NX EX <ttlSeconds>`.

```json
{
  "resourceKey": "notification:41:delivery",
  "owner": "worker-1",
  "ttlSeconds": 15
}
```

Ответ всегда имеет HTTP-статус `200`; результат попытки определяется полем
`acquired`. Секретный `token` возвращается только успешному владельцу.

### `POST /api/locks/release`

```json
{
  "resourceKey": "notification:41:delivery",
  "token": "random-owner-token"
}
```

Backend сравнивает токен и удаляет ключ одной Lua-операцией. Это не позволяет
клиенту снять блокировку, которая уже истекла и была захвачена другим владельцем.

## Исследование согласованности Redis

### `POST /api/consistency/experiments`

Метод записывает значение в primary и затем читает его из выбранного узла.

```json
{
  "key": "experiment:client01",
  "value": "revision-42",
  "readTarget": "REPLICA",
  "mode": "EVENTUAL",
  "waitTimeoutMs": 500
}
```

- `EVENTUAL` читает реплику сразу после записи.
- `WAIT_FOR_REPLICA` вызывает `WAIT 1 <waitTimeoutMs>` перед чтением.
- `PRIMARY` используется как контрольный режим чтения после собственной записи.

Ответ содержит записанное и прочитанное значения, число подтвердивших реплик,
признак `consistent` и длительности записи, ожидания и чтения в миллисекундах.
`WAIT` уменьшает окно рассогласования, но не превращает Redis в строго
согласованную систему и не является гарантией сохранения на диск.

## Текущее состояние backend

| Контракт | Состояние на 08.10.2026 |
| --- | --- |
| `POST /api/v1/auth/login` | Маршрут есть, но возвращает только `message` и `username` |
| `POST /api/v1/auth/register` | DTO существует, контроллера нет |
| `/api/notifications/**` | Методы присутствуют только в комментариях |
| `GET /api/categories/{id}` | Маршрут есть, но всегда выбрасывает `UnsupportedOperationException` |
| `/api/blocks/**` | Репозиторий и сервис есть, REST-контроллера нет |
| `/api/locks/**` | Есть сервис Redisson, REST-контроллера нет |
| `/api/consistency/**` | Ещё не реализовано |

Перед подключением настоящего backend необходимо также:

1. заменить Swagger-аннотацию `io.swagger.v3.oas.annotations.parameters.RequestBody`
   в `AuthController` на `org.springframework.web.bind.annotation.RequestBody`;
2. выбрать одну схему авторизации — целевой Bearer JWT из этого контракта либо
   временные `X-Username`/`X-Password`, но не обе одновременно;
3. добавить `PATCH` в список разрешённых CORS-методов;
4. привести Java `categoryId` к числу, как в `Category.id`;
5. формировать все ошибки через единый `ApiErrorResponse`.
