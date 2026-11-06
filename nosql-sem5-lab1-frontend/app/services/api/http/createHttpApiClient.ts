import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type {
  ApiClient,
  AuthResponse,
  Category,
  CategoryResponse,
  ConsistencyExperimentResponse,
  Movie,
  Notification,
  Screening,
  ScreeningSeats,
  SeatHold,
  Product,
  CinemaOrder,
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

    seatHolds: {
      create: request => http.post<SeatHold>(API_ENDPOINTS.seatHolds.base, request),
      getById: holdId => http.get<SeatHold>(API_ENDPOINTS.seatHolds.byId(holdId)),
      release: holdId => http.delete(API_ENDPOINTS.seatHolds.byId(holdId)),
    },

    products: {
      getAvailable: () => http.get<Product[]>(API_ENDPOINTS.products.base),
    },

    orders: {
      create: request => http.post<CinemaOrder>(API_ENDPOINTS.orders.base, request),
      getAll: () => http.get<CinemaOrder[]>(API_ENDPOINTS.orders.base),
      getById: orderId => http.get<CinemaOrder>(API_ENDPOINTS.orders.byId(orderId)),
      cancel: orderId => http.post<CinemaOrder>(API_ENDPOINTS.orders.cancel(orderId)),
    },

    notifications: {
      getAll: async query => paginateNotifications(await getNotifications(), query),
      getByUser: async (userId, query) => paginateNotifications(
        await getNotifications(),
        { ...query, userId },
      ),
      getById: id => http.get<Notification>(API_ENDPOINTS.notifications.byId(id)),
      create: request => http.post<Notification>(API_ENDPOINTS.adminNotifications.base, request),
      update: (id, request) => request.status === 'READ'
        ? http.post<Notification>(API_ENDPOINTS.notifications.read(id))
        : Promise.reject(createApiClientError(400, 'Bad Request',
          'Backend поддерживает только отметку о прочтении', API_ENDPOINTS.notifications.byId(id))),
      delete: id => http.delete(API_ENDPOINTS.notifications.byId(id)),
      getStats: async query => aggregateNotificationStats(await getNotifications(), query),
    },

    categories: {
      getAll: () => http.get<Category[]>(API_ENDPOINTS.categories.base),
      getById: id => http.get<CategoryResponse>(API_ENDPOINTS.categories.adminById(id)),
      create: request => http.post<Category>(API_ENDPOINTS.categories.adminBase, request),
      update: (id, request) => http.put<Category>(API_ENDPOINTS.categories.adminById(id), request),
      delete: id => http.delete(API_ENDPOINTS.categories.adminById(id)),
    },

    consistency: {
      runExperiment: request => http.post<ConsistencyExperimentResponse>(
        API_ENDPOINTS.consistency.experiments,
        request,
      ),
    },
  }
}
