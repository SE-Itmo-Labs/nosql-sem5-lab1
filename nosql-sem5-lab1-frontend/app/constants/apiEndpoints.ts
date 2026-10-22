/**
 * Каталог URL backend API.
 *
 * Страницы и composable-функции не собирают URL вручную. Это дает одно место
 * для изменения маршрутов и исключает расхождения между HTTP-клиентом, моками
 * и документацией контракта.
 */

/** Кодирует динамическую часть пути, чтобы ID ресурса не мог изменить URL. */
const pathSegment = (value: string | number) => encodeURIComponent(String(value))

export const API_ENDPOINTS = {
  /** Авторизация использует уже выбранный backend-префикс /api/v1/auth. */
  auth: {
    login: '/api/v1/auth/login',
  },

  catalog: {
    movies: '/api/v1/movies',
    movie: (movieId: number) => `/api/v1/movies/${pathSegment(movieId)}`,
    movieScreenings: (movieId: number) => `/api/v1/movies/${pathSegment(movieId)}/screenings`,
    screeningSeats: (screeningId: number) => `/api/v1/screenings/${pathSegment(screeningId)}/seats`,
  },

  seatHolds: {
    base: '/api/v1/seat-holds',
    byId: (holdId: string) => `/api/v1/seat-holds/${pathSegment(holdId)}`,
  },

  products: {
    base: '/api/v1/products',
  },

  orders: {
    base: '/api/v1/orders',
    byId: (orderId: number) => `/api/v1/orders/${pathSegment(orderId)}`,
    cancel: (orderId: number) => `/api/v1/orders/${pathSegment(orderId)}/cancel`,
  },

  /** Основной сценарий лабораторной — создание и чтение уведомлений. */
  notifications: {
    base: '/api/v1/notifications',
    byId: (id: number) => `/api/v1/notifications/${pathSegment(id)}`,
    read: (id: number) => `/api/v1/notifications/${pathSegment(id)}/read`,
  },

  /** Справочник категорий, поверх которого backend реализует cache-aside. */
  categories: {
    base: '/api/v1/categories',
    adminBase: '/api/v1/admin/notification-categories',
    adminById: (id: number) => `/api/v1/admin/notification-categories/${pathSegment(id)}`,
  },

  /** Управляемый эксперимент чтения из Redis primary или replica. */
  consistency: {
    experiments: '/api/v1/admin/laboratory/consistency/experiments',
  },

  adminNotifications: {
    base: '/api/v1/admin/notifications',
  },
} as const
