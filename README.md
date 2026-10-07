# nosql-sem5-lab1

Lab1, NoSQL. Снагин Станислав, Александрова Милана 

вар. `1869847`?

Внимание! У разных вариантов разные параметры задания!
Вариант №1869847

Разработать веб-приложение на языке Java с использованием Spring Boot, реализующее личный кабинет пользователя.

Предметная область: кинотеатр.
Основной объект системы: уведомление.
Пользовательская роль: клиент.
Обязательный сценарий: отправка уведомлений.

Система хранения типа «ключ — значение»: Redis.
Дополнительные параметры

- сценарий временного хранения: временная блокировка ресурса;
- сценарий кэширования: справочник категорий;
- атомарный механизм: распределённая блокировка ресурса;
- исследование поведения системы: изменение параметров согласованности чтения/записи.

Требования к программе

+ Развернуть систему хранения, указанную в варианте.
- Реализовать подключение к ней из Java/Spring Boot.
- Выбрать и обосновать способ представления данных в модели «ключ — значение».
- Реализовать хранение части данных приложения в выбранной системе.
- Реализовать механизм временных данных: TTL для Redis, application-level expiration для Riak KV или lease для Etcd.
- Реализовать кэширование часто запрашиваемых данных.
- Реализовать назначенный атомарный счётчик, ограничение запросов или блокировку.
- Настроить сохранение и восстановление данных, если выбранная система поддерживает этот механизм.
- Исследовать назначенный сценарием режим поведения системы.
- Сделать выводы о применимости выбранного хранилища для разработанного приложения.


## Запуск и разработка

Проект состоит из Spring Boot backend, Nuxt frontend, PostgreSQL и Redis.
Frontend может работать независимо от незавершённого backend в явном
`mock`-режиме.

### Быстрый запуск frontend с демонстрационными данными

Требуется Node.js `22.19+` или актуальная версия Node.js 24.

```bash
cd nosql-sem5-lab1-frontend
cp .env.example .env
npm ci
npm run dev
```

В `.env` должен быть выбран режим локальных данных:

```dotenv
NUXT_PUBLIC_API_MODE=mock
NUXT_PUBLIC_API_BASE=http://localhost:16767
```

Для демо-входа используются логин `client01` и пароль `client123`. Mock-режим
не отправляет сетевые запросы и реализует тот же `ApiClient`, что и настоящий
HTTP-клиент.

### Запуск инфраструктуры и backend

1. Создайте `credentials.env` по примеру `crendentials.env.example`.
2. Укажите `POSTGRES_USER`, `POSTGRES_PASSWORD` и `POSTGRES_DB`.
3. Соберите приложение и запустите контейнеры:

```bash
./gradlew build
docker compose --env-file credentials.env up --build --force-recreate -d
```

Чтобы frontend обращался к Spring Boot, переключите режим:

```dotenv
NUXT_PUBLIC_API_MODE=real
NUXT_PUBLIC_API_BASE=http://localhost:16767
```

Ошибки backend в режиме `real` намеренно не подменяются mock-ответами. Благодаря
этому проблемы интеграции остаются заметными во время разработки.

### Полезные адреса

| Назначение | Адрес |
| --- | --- |
| Nuxt в режиме разработки | <http://localhost:3000/nosql-sem5-lab1/> |
| Spring Boot API | <http://localhost:16767> |
| Swagger UI | <http://localhost:16767/swagger-ui/index.html> |
| Опубликованный frontend | <https://se-itmo-labs.github.io/nosql-sem5-lab1/> |
| API-контракт | [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md) |

### Работа с Redis CLI

Подключение к Redis внутри Docker-контейнера:

```bash
docker exec -it redis_container redis-cli
```

<details>
<summary><strong>Основные команды разработки</strong></summary>

Frontend:

```bash
cd nosql-sem5-lab1-frontend
npm run dev       # сервер разработки
npm run build     # production-сборка
npm run generate  # статическая версия для GitHub Pages
```

Backend:

```bash
./gradlew build
./gradlew bootRun
```

Docker:

```bash
docker compose --env-file credentials.env up -d
docker compose ps
docker compose logs -f spring-app
```

</details>

<details>
<summary><strong>Структура frontend API-слоя</strong></summary>

```text
nosql-sem5-lab1-frontend/app/
├── composables/
│   ├── useApi.ts                     # выбор mock или real через конфигурацию
│   └── useAuth.ts                    # пользовательская сессия поверх ApiClient
├── constants/
│   └── apiEndpoints.ts               # все URL backend в одном месте
├── services/
│   ├── api/
│   │   ├── createApiClient.ts        # фабрика выбранного режима
│   │   ├── http/                     # настоящий REST-клиент
│   │   └── mock/                     # локальные реализации по доменам
│   └── auth/
│       └── authSession.ts            # чтение токена без циклических импортов
└── types/
    └── api/                           # контракты, разделённые по доменам
```

</details>
