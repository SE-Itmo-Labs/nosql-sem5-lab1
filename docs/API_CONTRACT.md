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

## Каталог кинотеатра

| Метод | URL | Результат |
| --- | --- | --- |
| `GET` | `/api/v1/movies` | актуальные и будущие фильмы, кроме архивных |
| `GET` | `/api/v1/movies/{movieId}` | карточка неархивного фильма |
| `GET` | `/api/v1/movies/{movieId}/screenings` | будущие запланированные сеансы фильма |
| `GET` | `/api/v1/screenings/{screeningId}/seats` | зал и упорядоченный список мест сеанса |

Карточка фильма содержит стабильный `slug`, название, описание, длительность в
минутах, возрастной рейтинг и статус `NOW_SHOWING` или `COMING_SOON`.

Пример сеанса:

```json
{
  "id": 1,
  "movieId": 1,
  "movieTitle": "Северный свет",
  "auditoriumId": 1,
  "auditoriumName": "Зал 1",
  "startsAt": "2026-10-21T09:00:00Z",
  "basePrice": 450.00,
  "status": "SCHEDULED"
}
```

Схема зала возвращает места в порядке ряда и номера. Статус `AVAILABLE`, `HELD`
или `BOOKED` вычисляется по активным Redis-удержаниям и билетам PostgreSQL.

Архивный фильм, прошедший или отменённый сеанс недоступны через клиентские
методы и возвращают `404`.

## Уведомления

| Метод | URL | Результат |
| --- | --- | --- |
| `GET` | `/api/v1/notifications` | уведомления текущего пользователя |
| `GET` | `/api/v1/notifications/{id}` | своё уведомление по ID |
| `POST` | `/api/v1/notifications/{id}/read` | отметить своё уведомление прочитанным |
| `DELETE` | `/api/v1/notifications/{id}` | удалить своё уведомление |
| `POST` | `/api/v1/admin/notifications` | отправить уведомление выбранному клиенту; только `ADMIN` |

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
списка возвращает JSON-массив от новых уведомлений к старым. Чтение уведомления
другого клиента возвращает ошибку доступа к ресурсу.

## Категории и кэш

| Метод | URL | Результат |
| --- | --- | --- |
| `GET` | `/api/v1/categories` | список категорий |
| `GET` | `/api/v1/admin/notification-categories/{id}` | категория и информация о кэше; только `ADMIN` |
| `POST` | `/api/v1/admin/notification-categories` | новая категория, ответ `201`; только `ADMIN` |
| `PUT` | `/api/v1/admin/notification-categories/{id}` | обновлённая категория; только `ADMIN` |
| `DELETE` | `/api/v1/admin/notification-categories/{id}` | удаление, ответ `204`; только `ADMIN` |

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

## Удержание мест

| Метод | URL | Результат |
| --- | --- | --- |
| `POST` | `/api/v1/seat-holds` | атомарно удержать места, ответ `201` |
| `GET` | `/api/v1/seat-holds/{holdId}` | получить своё активное удержание |
| `DELETE` | `/api/v1/seat-holds/{holdId}` | освободить свои места, ответ `204` |

Тело создания:

```json
{
  "screeningId": 15,
  "seatIds": [71, 72]
}
```

Ответ содержит `holdId`, места, `createdAt`, `expiresAt`, `remainingSeconds` и
статус `ACTIVE`. Владелец и TTL определяются сервером. Пересечение хотя бы по
одному удерживаемому или купленному месту возвращает `409` без частичного
создания ключей.

## Кинобар и заказы

| Метод | URL | Результат |
| --- | --- | --- |
| `GET` | `/api/v1/products` | доступные товары кинобара |
| `POST` | `/api/v1/orders` | подтвердить удержание и создать заказ |
| `GET` | `/api/v1/orders` | свои заказы от новых к старым |
| `GET` | `/api/v1/orders/{orderId}` | детали своего заказа |
| `POST` | `/api/v1/orders/{orderId}/cancel` | отменить свой заказ не позднее чем за 30 минут до сеанса |

```json
{
  "holdId": "0df3a95e-4fe8-4bb3-a18f-744729476f94",
  "products": [{ "productId": 3, "quantity": 2 }],
  "idempotencyKey": "client-generated-unique-value"
}
```

Подтверждение повторно проверяет удержание и занятость, берёт блокировки мест в
стабильном порядке и сохраняет заказ транзакционно. Повтор того же
`idempotencyKey` возвращает исходный заказ. Ответ содержит билеты, позиции
кинобара, снимки цен и итоговую сумму.

Подтверждение и отмена сохраняют outbox-событие в транзакции заказа. Фоновая
доставка создаёт системное уведомление в Redis; повторная доставка безопасна.

## Исследование согласованности Redis

| Метод | URL | Назначение |
| --- | --- | --- |
| `POST` | `/api/v1/admin/laboratory/consistency/experiments` | записать и прочитать проекцию существующего сеанса |
| `POST` | `/api/v1/admin/laboratory/consistency/nodes/{nodeId}/detach` | отключить выбранную реплику |
| `POST` | `/api/v1/admin/laboratory/consistency/nodes/{nodeId}/attach` | подключить выбранную реплику обратно |

Тело эксперимента:

```json
{
  "screeningId": 1,
  "readTarget": "REPLICA",
  "mode": "EVENTUAL",
  "waitTimeoutMs": 500
}
```

Backend проверяет сеанс в PostgreSQL и сам строит ключ
`laboratory:screening:{id}` и диагностическое значение. `readTarget` принимает
`PRIMARY` или `REPLICA`, а `mode` — `EVENTUAL` или `WAIT_FOR_REPLICA`. Ответ
содержит предметный объект, записанное и прочитанное значения, узел,
подтверждения и длительности. Проекция не участвует в бронировании.

## Swagger

- Swagger UI: <http://localhost:16767/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:16767/v3/api-docs>

В Swagger два глобальных поля авторизации соответствуют заголовкам
`X-Username` и `X-Password`.
