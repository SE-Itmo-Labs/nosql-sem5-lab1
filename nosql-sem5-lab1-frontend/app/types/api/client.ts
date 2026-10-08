import type { AuthResponse, LoginRequest } from './auth'
import type {
  CreateTemporaryBlockRequest,
  ExecuteLockRequest,
  ExecuteLockResponse,
  TemporaryBlock,
} from './blocks'
import type { Category, CategoryResponse, SaveCategoryRequest } from './categories'
import type { ConsistencyExperimentRequest, ConsistencyExperimentResponse } from './consistency'
import type { PageQuery, PageResponse } from './common'
import type {
  CreateNotificationRequest,
  Notification,
  NotificationQuery,
  NotificationStats,
  UpdateNotificationRequest,
} from './notifications'

/**
 * Общий контракт источника данных.
 * HTTP- и mock-реализации обязаны возвращать одинаковые структуры, поэтому
 * страницы не знают, подключен настоящий backend или локальная демонстрация.
 */
export interface ApiClient {
  auth: {
    login(request: LoginRequest): Promise<AuthResponse>
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
    execute(request: ExecuteLockRequest): Promise<ExecuteLockResponse>
  }
  consistency: {
    runExperiment(request: ConsistencyExperimentRequest): Promise<ConsistencyExperimentResponse>
  }
}
