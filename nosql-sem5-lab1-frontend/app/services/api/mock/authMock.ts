import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient, AuthResponse } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import type { MockDatabaseStore } from './database'
import { waitForMockResponse } from './helpers'

/** Mock-авторизация проверяет учетные данные так же, как настоящий backend. */
export const createMockAuthApi = (store: MockDatabaseStore): ApiClient['auth'] => ({
  async login(request): Promise<AuthResponse> {
    await waitForMockResponse()
    const user = store.value.users.find(candidate => (
      candidate.username === request.username && candidate.password === request.password
    ))

    if (!user) {
      throw createApiClientError(
        401,
        'Unauthorized',
        'Неверный логин или пароль',
        API_ENDPOINTS.auth.login,
      )
    }

    return {
      token: `mock-token-${user.username}-${Date.now()}`,
      type: 'Bearer',
      username: user.username,
      email: user.email,
    }
  },

  async register(request): Promise<AuthResponse> {
    await waitForMockResponse()

    if (store.value.users.some(user => user.username === request.username)) {
      throw createApiClientError(
        409,
        'Conflict',
        'Пользователь с таким логином уже существует',
        API_ENDPOINTS.auth.register,
      )
    }

    store.value.users.push({ ...request })
    return {
      token: `mock-token-${request.username}-${Date.now()}`,
      type: 'Bearer',
      username: request.username,
      email: request.email,
    }
  },
})
