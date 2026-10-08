import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type {
  ApiClient,
  AuthResponse,
  Category,
  CategoryResponse,
  ConsistencyExperimentResponse,
  LockAttemptResponse,
  Notification,
  NotificationStats,
  PageResponse,
  ReleaseLockResponse,
  TemporaryBlock,
} from '~/types/api'
import { createHttpRequester } from './createHttpRequester'

interface HttpApiClientOptions {
  baseUrl: string
  getAccessToken: () => string | null
}

/**
 * Настоящая реализация ApiClient. Здесь видно соответствие каждого метода
 * фронтенда конкретному REST-маршруту без деталей отображения страниц.
 */
export const createHttpApiClient = (options: HttpApiClientOptions): ApiClient => {
  const http = createHttpRequester(options)

  return {
    auth: {
      login: request => http.post<AuthResponse>(API_ENDPOINTS.auth.login, request),
      register: request => http.post<AuthResponse>(API_ENDPOINTS.auth.register, request),
    },

    notifications: {
      getAll: query => http.get<PageResponse<Notification>>(API_ENDPOINTS.notifications.base, query),
      getByUser: (userId, query) => http.get<PageResponse<Notification>>(
        API_ENDPOINTS.notifications.byUser(userId),
        query,
      ),
      getById: id => http.get<Notification>(API_ENDPOINTS.notifications.byId(id)),
      create: request => http.post<Notification>(API_ENDPOINTS.notifications.base, request),
      update: (id, request) => http.patch<Notification>(API_ENDPOINTS.notifications.byId(id), request),
      delete: id => http.delete(API_ENDPOINTS.notifications.byId(id)),
      getStats: query => http.get<NotificationStats>(API_ENDPOINTS.notifications.stats, query),
    },

    categories: {
      getAll: () => http.get<Category[]>(API_ENDPOINTS.categories.base),
      getById: id => http.get<CategoryResponse>(API_ENDPOINTS.categories.byId(id)),
      create: request => http.post<Category>(API_ENDPOINTS.categories.base, request),
      update: (id, request) => http.put<Category>(API_ENDPOINTS.categories.byId(id), request),
      delete: id => http.delete(API_ENDPOINTS.categories.byId(id)),
    },

    temporaryBlocks: {
      create: request => http.post<TemporaryBlock>(API_ENDPOINTS.temporaryBlocks.base, request),
      getByResource: resourceKey => http.get<TemporaryBlock>(
        API_ENDPOINTS.temporaryBlocks.byResource(resourceKey),
      ),
      release: resourceKey => http.delete(API_ENDPOINTS.temporaryBlocks.byResource(resourceKey)),
    },

    locks: {
      acquire: request => http.post<LockAttemptResponse>(API_ENDPOINTS.locks.acquire, request),
      release: request => http.post<ReleaseLockResponse>(API_ENDPOINTS.locks.release, request),
    },

    consistency: {
      runExperiment: request => http.post<ConsistencyExperimentResponse>(
        API_ENDPOINTS.consistency.experiments,
        request,
      ),
    },
  }
}
