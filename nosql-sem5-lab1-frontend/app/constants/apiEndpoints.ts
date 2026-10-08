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
  /** Техническая проверка доступности приложения; не является бизнес-методом. */
  system: {
    hello: '/hello',
  },

  /** Авторизация использует уже выбранный backend-префикс /api/v1/auth. */
  auth: {
    login: '/api/v1/auth/login',
    register: '/api/v1/auth/register',
  },

  /** Основной сценарий лабораторной — создание и чтение уведомлений. */
  notifications: {
    base: '/api/notifications',
    byId: (id: number) => `/api/notifications/${pathSegment(id)}`,
    byUser: (userId: string) => `/api/notifications/user/${pathSegment(userId)}`,
    stats: '/api/notifications/stats',
  },

  /** Справочник категорий, поверх которого backend реализует cache-aside. */
  categories: {
    base: '/api/categories',
    byId: (id: number) => `/api/categories/${pathSegment(id)}`,
  },

  /** Ключи с TTL для временного бронирования ресурса. */
  temporaryBlocks: {
    base: '/api/blocks',
    byResource: (resourceKey: string) => `/api/blocks/${pathSegment(resourceKey)}`,
  },

  /** Атомарный захват и безопасное освобождение распределенной блокировки. */
  locks: {
    acquire: '/api/locks/acquire',
    release: '/api/locks/release',
  },

  /** Управляемый эксперимент чтения из Redis primary или replica. */
  consistency: {
    experiments: '/api/consistency/experiments',
  },
} as const
