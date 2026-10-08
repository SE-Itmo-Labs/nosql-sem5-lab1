import { readAccessToken } from '~/services/auth/authSession'
import type { ApiClient } from '~/types/api'
import { createHttpApiClient } from './http/createHttpApiClient'
import { createMockApiClient } from './mock/createMockApiClient'
import type { MockDatabaseStore } from './mock/database'

/** Допустимые источники данных выбираются только явной настройкой окружения. */
export type ApiMode = 'mock' | 'real'

interface CreateApiClientOptions {
  mode: ApiMode
  baseUrl: string
  mockDatabase: MockDatabaseStore
}

/**
 * Фабрика является единственным местом переключения mock/real.
 * Никакого автоматического fallback нет: ошибка backend в real-режиме должна
 * быть видна, а не маскироваться демонстрационными данными.
 */
export const createApiClient = ({
  mode,
  baseUrl,
  mockDatabase,
}: CreateApiClientOptions): ApiClient => {
  if (mode === 'real') {
    return createHttpApiClient({
      baseUrl,
      getAccessToken: readAccessToken,
    })
  }

  return createMockApiClient(mockDatabase)
}

/** Неизвестное значение считается ошибкой конфигурации, а не новым режимом. */
export const parseApiMode = (value: unknown): ApiMode => {
  if (value === 'mock' || value === 'real') return value
  throw new Error(`Неизвестный NUXT_PUBLIC_API_MODE: ${String(value)}`)
}
