import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient, AuthResponse } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import type { MockDatabaseStore } from './database'
import { waitForMockResponse } from './helpers'

/**
 * SHA-256 здесь не заменяет backend-хеширование с солью: это только защита от
 * случайной публикации открытого mock-пароля внутри prerender-payload.
 */
const createPasswordHash = async (password: string): Promise<string> => {
  const encodedPassword = new TextEncoder().encode(password)
  const digest = await globalThis.crypto.subtle.digest('SHA-256', encodedPassword)

  return Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, '0')).join('')
}

/** Mock-авторизация проверяет учетные данные так же, как настоящий backend. */
export const createMockAuthApi = (store: MockDatabaseStore): ApiClient['auth'] => ({
  async login(request): Promise<AuthResponse> {
    await waitForMockResponse()
    const passwordHash = await createPasswordHash(request.password)
    const user = store.value.users.find(candidate => (
      candidate.username === request.username && candidate.passwordHash === passwordHash
    ))

    if (!user) {
      throw createApiClientError(
        401,
        'Unauthorized',
        'Неверный логин или пароль',
        API_ENDPOINTS.auth.login,
      )
    }

    store.value.currentUsername = user.username
    return {
      userId: user.id,
      username: user.username,
      displayName: user.displayName,
      role: user.role,
    }
  },
})
