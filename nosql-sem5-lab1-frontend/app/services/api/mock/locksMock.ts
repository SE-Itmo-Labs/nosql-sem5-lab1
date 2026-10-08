import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import type { MockDatabaseStore } from './database'
import { waitForMockResponse } from './helpers'

/**
 * Повторяет backend-сценарий execute: lock существует только во время
 * критической секции и освобождается самим сервисом после выполнения.
 */
export const createMockLocksApi = (store: MockDatabaseStore): ApiClient['locks'] => ({
  async execute(request) {
    await waitForMockResponse()

    if (request.holdMillis < 0 || request.holdMillis > 10_000) {
      throw createApiClientError(
        400,
        'Bad Request',
        'holdMillis должен быть от 0 до 10000',
        API_ENDPOINTS.locks.execute,
      )
    }

    const current = store.value.locks[request.resourceKey]
    if (current) {
      return {
        resourceKey: request.resourceKey,
        owner: current.owner,
        acquired: false,
        message: 'Ресурс уже обрабатывается другим запросом',
      }
    }

    const owner = store.value.currentUsername ?? 'mock-client'
    store.value.locks[request.resourceKey] = { resourceKey: request.resourceKey, owner }

    try {
      if (request.holdMillis > 0) {
        await new Promise(resolve => setTimeout(resolve, request.holdMillis))
      }

      return {
        resourceKey: request.resourceKey,
        owner,
        acquired: true,
        message: 'Критическая секция выполнена',
      }
    }
    finally {
      Reflect.deleteProperty(store.value.locks, request.resourceKey)
    }
  },
})
