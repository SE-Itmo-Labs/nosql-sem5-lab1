import { createApiClient, parseApiMode } from '~/services/api/createApiClient'
import { createMockDatabase, type MockDatabase } from '~/services/api/mock/database'

export const useApi = () => {
  const config = useRuntimeConfig().public
  const mockDatabase = useState<MockDatabase>('mock-api-database', createMockDatabase)

  return createApiClient({
    mode: parseApiMode(config.apiMode),
    baseUrl: config.apiBase,
    mockDatabase,
  })
}
