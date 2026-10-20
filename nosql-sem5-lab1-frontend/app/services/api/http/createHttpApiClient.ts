import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type {
  ApiClient,
  AuthResponse,
  Category,
  CategoryResponse,
  ConsistencyExperimentResponse,
  Movie,
  Notification,
  ExecuteLockResponse,
  Screening,
  ScreeningSeats,
  TemporaryBlock,
} from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import { aggregateNotificationStats, paginateNotifications } from '../notificationCollection'
import { createHttpRequester } from './createHttpRequester'

interface HttpApiClientOptions {
  baseUrl: string
  getAuthHeaders: () => Record<string, string>
}

/**
 * Настоящая реализация ApiClient. Здесь видно соответствие каждого метода
 * фронтенда конкретному REST-маршруту без деталей отображения страниц.
 */
export const createHttpApiClient = (options: HttpApiClientOptions): ApiClient => {
  const http = createHttpRequester(options)
  const getNotifications = () => http.get<Notification[]>(API_ENDPOINTS.notifications.base)

  return {
    auth: {
      login: request => http.post<AuthResponse>(API_ENDPOINTS.auth.login, request),
    },

    catalog: {
      getMovies: () => http.get<Movie[]>(API_ENDPOINTS.catalog.movies),
      getMovie: movieId => http.get<Movie>(API_ENDPOINTS.catalog.movie(movieId)),
      getMovieScreenings: movieId => http.get<Screening[]>(
        API_ENDPOINTS.catalog.movieScreenings(movieId),
      ),
      getScreeningSeats: screeningId => http.get<ScreeningSeats>(
        API_ENDPOINTS.catalog.screeningSeats(screeningId),
      ),
    },

    notifications: {
      getAll: async query => paginateNotifications(await getNotifications(), query),
      getByUser: async (userId, query) => paginateNotifications(
        await getNotifications(),
        { ...query, userId },
      ),
      getById: id => http.get<Notification>(API_ENDPOINTS.notifications.byId(id)),
      create: request => http.post<Notification>(API_ENDPOINTS.adminNotifications.base, request),
      update: (id, _request) => Promise.reject(createApiClientError(
        501,
        'Not Implemented',
        'Текущий backend пока не поддерживает изменение уведомлений',
        API_ENDPOINTS.notifications.byId(id),
      )),
      delete: id => Promise.reject(createApiClientError(
        501,
        'Not Implemented',
        'Текущий backend пока не поддерживает удаление уведомлений',
        API_ENDPOINTS.notifications.byId(id),
      )),
      getStats: async query => aggregateNotificationStats(await getNotifications(), query),
    },

    categories: {
      getAll: () => http.get<Category[]>(API_ENDPOINTS.categories.base),
      getById: id => http.get<CategoryResponse>(API_ENDPOINTS.categories.adminById(id)),
      create: request => http.post<Category>(API_ENDPOINTS.categories.adminBase, request),
      update: (id, request) => http.put<Category>(API_ENDPOINTS.categories.adminById(id), request),
      delete: id => http.delete(API_ENDPOINTS.categories.adminById(id)),
    },

    temporaryBlocks: {
      create: request => http.post<TemporaryBlock>(API_ENDPOINTS.temporaryBlocks.base, request),
      getByResource: resourceKey => http.get<TemporaryBlock>(
        API_ENDPOINTS.temporaryBlocks.byResource(resourceKey),
      ),
      release: resourceKey => http.delete(API_ENDPOINTS.temporaryBlocks.byResource(resourceKey)),
    },

    locks: {
      execute: request => http.post<ExecuteLockResponse>(API_ENDPOINTS.locks.execute, request),
    },

    consistency: {
      runExperiment: request => http.post<ConsistencyExperimentResponse>(
        API_ENDPOINTS.consistency.experiments,
        request,
      ),
    },
  }
}
