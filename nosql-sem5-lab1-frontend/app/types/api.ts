/**
 * Общие типы REST API.
 *
 * Файл является единой точкой согласования между фронтендом, HTTP-клиентом,
 * mock-реализацией и Spring Boot. Компоненты интерфейса не должны объявлять
 * собственные копии этих структур: иначе моки и настоящий backend быстро
 * начинают возвращать разные данные.
 */

/** Дата и время передаются между Java и браузером в формате ISO 8601. */
export type IsoDateTime = string

/** Стандартная ошибка, которую формирует GlobalExceptionHandler на backend. */
export interface ApiErrorResponse {
  timestamp: IsoDateTime
  status: number
  error: string
  message: string
  path: string
}

/** Унифицированная страница данных для списков с серверной пагинацией. */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
  empty: boolean
}

/** Параметры, одинаковые для всех постраничных списков. */
export interface PageQuery {
  page?: number
  size?: number
  sort?: string
}

// ---------------------------------------------------------------------------
// Авторизация
// ---------------------------------------------------------------------------

/** Учетные данные для входа через POST /api/v1/auth/login. */
export interface LoginRequest {
  username: string
  password: string
}

/** Данные для создания учетной записи клиента кинотеатра. */
export interface RegisterRequest extends LoginRequest {
  email: string
}

/**
 * Результат входа или регистрации.
 * Поля повторяют AuthResponse на backend, чтобы не требовался дополнительный
 * слой преобразования при переходе от моков к настоящему серверу.
 */
export interface AuthResponse {
  token: string
  type: 'Bearer'
  username: string
  email: string | null
}

// ---------------------------------------------------------------------------
// Уведомления — основной объект и обязательный сценарий варианта
// ---------------------------------------------------------------------------

/** Состояние уведомления, которое нужно показывать в личном кабинете. */
export type NotificationStatus = 'SENT' | 'READ'

/** Уведомление в том виде, в котором его получает frontend. */
export interface Notification {
  id: number
  userId: string
  title: string
  text: string
  categoryId: number
  status: NotificationStatus
  createdAt: IsoDateTime
}

/** Тело запроса на создание и отправку уведомления. */
export interface CreateNotificationRequest {
  userId: string
  title: string
  text: string
  categoryId: number
}

/**
 * Частичное изменение уведомления.
 * ID, получатель и дата создания не меняются после отправки.
 */
export interface UpdateNotificationRequest {
  title?: string
  text?: string
  categoryId?: number
  status?: NotificationStatus
}

/** Фильтры списка уведомлений; все поля необязательны. */
export interface NotificationQuery extends PageQuery {
  userId?: string
  categoryId?: number
  status?: NotificationStatus
  q?: string
}

/** Небольшая агрегация для информационных карточек на главной странице. */
export interface NotificationStats {
  total: number
  sent: number
  read: number
  byCategory: Record<string, number>
}

// ---------------------------------------------------------------------------
// Категории — справочник, для которого демонстрируется cache-aside
// ---------------------------------------------------------------------------

/** Категория уведомления, сохраненная в постоянном источнике данных. */
export interface Category {
  id: number
  name: string
  description: string
}

/** Поля категории без серверного идентификатора. */
export interface SaveCategoryRequest {
  name: string
  description: string
}

/**
 * Служебные сведения о кэше нужны для демонстрации лабораторного сценария.
 * Они позволяют интерфейсу явно показать cache hit/miss и оставшийся TTL.
 */
export interface CategoryCacheInfo {
  source: 'CACHE' | 'DATABASE'
  cached: boolean
  remainingTtlSeconds: number | null
}

/** Категория вместе с результатом обращения к Redis-кэшу. */
export interface CategoryResponse {
  category: Category
  cache: CategoryCacheInfo
}

// ---------------------------------------------------------------------------
// Временная блокировка ресурса — отдельный сценарий Redis TTL
// ---------------------------------------------------------------------------

/** Запрос на временную блокировку места или другого ресурса кинотеатра. */
export interface CreateTemporaryBlockRequest {
  resourceKey: string
  owner: string
  ttlSeconds: number
}

/** Текущее состояние временной блокировки. */
export interface TemporaryBlock {
  resourceKey: string
  owner: string
  ttlSeconds: number
  remainingTtlSeconds: number
  createdAt: IsoDateTime
  expiresAt: IsoDateTime
  active: boolean
}

// ---------------------------------------------------------------------------
// Распределенная блокировка — атомарный SET NX EX и безопасное освобождение
// ---------------------------------------------------------------------------

/** Попытка захватить распределенную блокировку от имени конкретного клиента. */
export interface AcquireLockRequest {
  resourceKey: string
  owner: string
  ttlSeconds: number
}

/**
 * Токен выдается только владельцу успешной блокировки.
 * Backend сравнивает его в Lua-скрипте перед удалением ключа и тем самым не
 * позволяет одному клиенту освободить чужую блокировку.
 */
export interface LockAttemptResponse {
  acquired: boolean
  resourceKey: string
  owner: string | null
  token: string | null
  expiresAt: IsoDateTime | null
  message: string
}

/** Данные для безопасного освобождения ранее захваченной блокировки. */
export interface ReleaseLockRequest {
  resourceKey: string
  token: string
}

/** Результат сравнения токена и удаления ключа блокировки. */
export interface ReleaseLockResponse {
  released: boolean
  resourceKey: string
  message: string
}

// ---------------------------------------------------------------------------
// Исследование согласованности primary/replica
// ---------------------------------------------------------------------------

/** Узел Redis, из которого будет выполнено контрольное чтение. */
export type RedisReadTarget = 'PRIMARY' | 'REPLICA'

/**
 * EVENTUAL сразу читает асинхронную реплику, WAIT_FOR_REPLICA сначала вызывает
 * Redis WAIT и только затем выполняет чтение. Сравнение режимов является сутью
 * назначенного вариантом исследования согласованности.
 */
export type ReplicationMode = 'EVENTUAL' | 'WAIT_FOR_REPLICA'

/** Параметры одного воспроизводимого эксперимента записи и последующего чтения. */
export interface ConsistencyExperimentRequest {
  key: string
  value: string
  readTarget: RedisReadTarget
  mode: ReplicationMode
  waitTimeoutMs: number
}

/** Метрики и фактические значения, которые frontend покажет пользователю. */
export interface ConsistencyExperimentResponse {
  key: string
  writtenValue: string
  readValue: string | null
  readTarget: RedisReadTarget
  mode: ReplicationMode
  acknowledgedReplicas: number
  consistent: boolean
  writeDurationMs: number
  replicationWaitDurationMs: number
  readDurationMs: number
}

// ---------------------------------------------------------------------------
// Интерфейс клиента — общий контракт HTTP- и mock-реализаций
// ---------------------------------------------------------------------------

/**
 * Любой источник данных фронтенда обязан реализовать этот интерфейс.
 * На следующем этапе его получат две реализации: настоящий HTTP-клиент и
 * локальное mock-хранилище. Страницы поэтому не будут знать, откуда пришли данные.
 */
export interface ApiClient {
  auth: {
    login(request: LoginRequest): Promise<AuthResponse>
    register(request: RegisterRequest): Promise<AuthResponse>
  }
  notifications: {
    getAll(query?: NotificationQuery): Promise<PageResponse<Notification>>
    getByUser(userId: string, query?: PageQuery): Promise<PageResponse<Notification>>
    getById(id: number): Promise<Notification>
    create(request: CreateNotificationRequest): Promise<Notification>
    update(id: number, request: UpdateNotificationRequest): Promise<Notification>
    delete(id: number): Promise<void>
    getStats(query?: NotificationQuery): Promise<NotificationStats>
  }
  categories: {
    getAll(): Promise<Category[]>
    getById(id: number): Promise<CategoryResponse>
    create(request: SaveCategoryRequest): Promise<Category>
    update(id: number, request: SaveCategoryRequest): Promise<Category>
    delete(id: number): Promise<void>
  }
  temporaryBlocks: {
    create(request: CreateTemporaryBlockRequest): Promise<TemporaryBlock>
    getByResource(resourceKey: string): Promise<TemporaryBlock>
    release(resourceKey: string): Promise<void>
  }
  locks: {
    acquire(request: AcquireLockRequest): Promise<LockAttemptResponse>
    release(request: ReleaseLockRequest): Promise<ReleaseLockResponse>
  }
  consistency: {
    runExperiment(request: ConsistencyExperimentRequest): Promise<ConsistencyExperimentResponse>
  }
}
