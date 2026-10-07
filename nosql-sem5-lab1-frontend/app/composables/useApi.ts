import { createApiClient, parseApiMode } from '~/services/api/createApiClient'
import { createMockDatabase, type MockDatabase } from '~/services/api/mock/database'
import type { ApiClient } from '~/types/api'

/**
 * Возвращает единый клиент данных для всех страниц приложения.
 *
 * Режим задается через окружение, а mock-база хранится в Nuxt state. Поэтому
 * разные страницы видят одни и те же изменения без глобального singleton,
 * который мог бы смешивать данные разных SSR-запросов.
 */
export const useApi = (): ApiClient => {
  const config = useRuntimeConfig().public
  const mockDatabase = useState<MockDatabase>('mock-api-database', createMockDatabase)

  return createApiClient({
    mode: parseApiMode(config.apiMode),
    baseUrl: config.apiBase,
    mockDatabase,
  })
}
