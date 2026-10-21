import type { AuthResponse, LoginRequest } from './auth'
import type { Category, CategoryResponse, SaveCategoryRequest } from './categories'
import type { Movie, Screening, ScreeningSeats } from './catalog'
import type {
  CinemaOrder,
  CreateOrderRequest,
  CreateSeatHoldRequest,
  Product,
  SeatHold,
} from './booking'
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
  catalog: {
    getMovies(): Promise<Movie[]>
    getMovie(movieId: number): Promise<Movie>
    getMovieScreenings(movieId: number): Promise<Screening[]>
    getScreeningSeats(screeningId: number): Promise<ScreeningSeats>
  }
  seatHolds: {
    create(request: CreateSeatHoldRequest): Promise<SeatHold>
    getById(holdId: string): Promise<SeatHold>
    release(holdId: string): Promise<void>
  }
  products: {
    getAvailable(): Promise<Product[]>
  }
  orders: {
    create(request: CreateOrderRequest): Promise<CinemaOrder>
    getAll(): Promise<CinemaOrder[]>
    getById(orderId: number): Promise<CinemaOrder>
    cancel(orderId: number): Promise<CinemaOrder>
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
  consistency: {
    runExperiment(request: ConsistencyExperimentRequest): Promise<ConsistencyExperimentResponse>
  }
}
