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
  },

  /** Основной сценарий лабораторной — создание и чтение уведомлений. */
  notifications: {
    base: '/api/v1/notifications',
    byId: (id: number) => `/api/v1/notifications/${pathSegment(id)}`,
  },

  /** Справочник категорий, поверх которого backend реализует cache-aside. */
  categories: {
    base: '/api/v1/categories',
    byId: (id: number) => `/api/v1/categories/${pathSegment(id)}`,
  },

  /** Ключи с TTL для временного бронирования ресурса. */
  temporaryBlocks: {
    base: '/api/v1/blocks',
    byResource: (resourceKey: string) => `/api/v1/blocks/${pathSegment(resourceKey)}`,
  },

  /** Атомарный захват и безопасное освобождение распределенной блокировки. */
  locks: {
    execute: '/api/v1/locks/execute',
  },

  /** Управляемый эксперимент чтения из Redis primary или replica. */
  consistency: {
    experiments: '/api/v1/consistency/experiments',
  },
} as const
