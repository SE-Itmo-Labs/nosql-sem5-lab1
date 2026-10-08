import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient, TemporaryBlock } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import type { MockDatabaseStore } from './database'
import { remainingSeconds, waitForMockResponse } from './helpers'

/** Возвращает активную блокировку и удаляет запись с истекшим TTL. */
const findActiveBlock = (
  store: MockDatabaseStore,
  resourceKey: string,
): TemporaryBlock | null => {
  const block = store.value.temporaryBlocks[resourceKey]
  if (!block) return null

  const remainingTtlSeconds = remainingSeconds(block.expiresAt)
  if (remainingTtlSeconds === 0) {
    Reflect.deleteProperty(store.value.temporaryBlocks, resourceKey)
    return null
  }

  return { ...block, remainingTtlSeconds, active: true }
}

/** Mock временных Redis-ключей с проверкой конфликта и автоматическим TTL. */
export const createMockTemporaryBlocksApi = (
  store: MockDatabaseStore,
): ApiClient['temporaryBlocks'] => ({
  async create(request) {
    await waitForMockResponse()

    if (request.ttlSeconds <= 0) {
      throw createApiClientError(
        400,
        'Bad Request',
        'TTL должен быть больше нуля',
        API_ENDPOINTS.temporaryBlocks.base,
      )
    }

    if (findActiveBlock(store, request.resourceKey)) {
      throw createApiClientError(
        409,
        'Conflict',
        'Ресурс уже временно заблокирован',
        API_ENDPOINTS.temporaryBlocks.byResource(request.resourceKey),
      )
    }

    const createdAt = new Date()
    const block: TemporaryBlock = {
      ...request,
      remainingTtlSeconds: request.ttlSeconds,
      createdAt: createdAt.toISOString(),
      expiresAt: new Date(createdAt.getTime() + request.ttlSeconds * 1000).toISOString(),
      active: true,
    }
    store.value.temporaryBlocks[request.resourceKey] = block
    return { ...block }
  },

  async getByResource(resourceKey) {
    await waitForMockResponse()
    const block = findActiveBlock(store, resourceKey)

    if (!block) {
      throw createApiClientError(
        404,
        'Not Found',
        'Активная временная блокировка не найдена',
        API_ENDPOINTS.temporaryBlocks.byResource(resourceKey),
      )
    }

    return block
  },

  async release(resourceKey) {
    await waitForMockResponse()
    Reflect.deleteProperty(store.value.temporaryBlocks, resourceKey)
  },
})
