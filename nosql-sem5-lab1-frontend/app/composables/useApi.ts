import { createApiClient, parseApiMode } from '~/services/api/createApiClient'
import { createMockDatabase, type MockDatabase } from '~/services/api/mock/database'
import type { ApiClient } from '~/types/api'

export const useApi = () => {
  const { apiBase } = useRuntimeConfig().public
  const { authHeaders } = useAuth()

  return {
    hello: () => $fetch(`${apiBase}/api/v1/hello`, {
      headers: authHeaders(),
    }),
  }
}
