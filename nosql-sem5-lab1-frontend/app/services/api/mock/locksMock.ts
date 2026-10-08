import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import type { MockDatabaseStore, MockLockRecord } from './database'
import { waitForMockResponse } from './helpers'

/** Возвращает действующую блокировку и очищает запись после истечения EX. */
const findActiveLock = (
  store: MockDatabaseStore,
  resourceKey: string,
): MockLockRecord | null => {
  const lock = store.value.locks[resourceKey]
  if (!lock) return null

  if (lock.expiresAt <= Date.now()) {
    Reflect.deleteProperty(store.value.locks, resourceKey)
    return null
  }

  return lock
}

/** Mock атомарного SET NX EX и безопасного Lua-освобождения по токену. */
export const createMockLocksApi = (store: MockDatabaseStore): ApiClient['locks'] => ({
  async acquire(request) {
    await waitForMockResponse()

    if (request.ttlSeconds <= 0) {
      throw createApiClientError(
        400,
        'Bad Request',
        'TTL должен быть больше нуля',
        API_ENDPOINTS.locks.acquire,
      )
    }

    const current = findActiveLock(store, request.resourceKey)

    if (current) {
      return {
        acquired: false,
        resourceKey: request.resourceKey,
        owner: current.owner,
        token: null,
        expiresAt: new Date(current.expiresAt).toISOString(),
        message: 'Ресурс уже заблокирован другим владельцем',
      }
    }

    const token = `mock-lock-${Date.now()}-${Math.random().toString(36).slice(2)}`
    const lock: MockLockRecord = {
      resourceKey: request.resourceKey,
      owner: request.owner,
      token,
      expiresAt: Date.now() + request.ttlSeconds * 1000,
    }
    store.value.locks[request.resourceKey] = lock

    return {
      acquired: true,
      resourceKey: request.resourceKey,
      owner: request.owner,
      token,
      expiresAt: new Date(lock.expiresAt).toISOString(),
      message: 'Блокировка успешно захвачена',
    }
  },

  async release(request) {
    await waitForMockResponse()
    const current = findActiveLock(store, request.resourceKey)
    const released = Boolean(current && current.token === request.token)

    if (released) Reflect.deleteProperty(store.value.locks, request.resourceKey)

    return {
      released,
      resourceKey: request.resourceKey,
      message: released
        ? 'Блокировка освобождена'
        : 'Блокировка не найдена или принадлежит другому владельцу',
    }
  },
})
