import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient, Category } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import type { MockDatabaseStore } from './database'
import { remainingSeconds, waitForMockResponse } from './helpers'

const CACHE_TTL_SECONDS = 60

/**
 * Mock справочника повторяет cache-aside: первый запрос читает постоянный
 * источник, следующие запросы до истечения TTL возвращают кэш.
 */
export const createMockCategoriesApi = (store: MockDatabaseStore): ApiClient['categories'] => ({
  async getAll() {
    await waitForMockResponse()
    return store.value.categories.map(category => ({ ...category }))
  },

  async getById(id) {
    await waitForMockResponse()
    const cached = store.value.categoryCache[id]

    if (cached && cached.expiresAt > Date.now()) {
      return {
        category: { ...cached.category },
        cache: {
          source: 'CACHE',
          cached: true,
          remainingTtlSeconds: remainingSeconds(cached.expiresAt),
        },
      }
    }

    Reflect.deleteProperty(store.value.categoryCache, id)
    const category = store.value.categories.find(item => item.id === id)

    if (!category) {
      throw createApiClientError(404, 'Not Found', 'Категория не найдена', API_ENDPOINTS.categories.byId(id))
    }

    store.value.categoryCache[id] = {
      category: { ...category },
      expiresAt: Date.now() + CACHE_TTL_SECONDS * 1000,
    }

    return {
      category: { ...category },
      cache: {
        source: 'DATABASE',
        cached: false,
        remainingTtlSeconds: null,
      },
    }
  },

  async create(request) {
    await waitForMockResponse()
    const category: Category = {
      id: store.value.nextCategoryId++,
      ...request,
    }
    store.value.categories.push(category)
    return { ...category }
  },

  async update(id, request) {
    await waitForMockResponse()
    const index = store.value.categories.findIndex(item => item.id === id)

    if (index < 0) {
      throw createApiClientError(404, 'Not Found', 'Категория не найдена', API_ENDPOINTS.categories.byId(id))
    }

    const category: Category = { id, ...request }
    store.value.categories[index] = category
    Reflect.deleteProperty(store.value.categoryCache, id)
    return { ...category }
  },

  async delete(id) {
    await waitForMockResponse()
    const index = store.value.categories.findIndex(item => item.id === id)

    if (index < 0) {
      throw createApiClientError(404, 'Not Found', 'Категория не найдена', API_ENDPOINTS.categories.byId(id))
    }

    store.value.categories.splice(index, 1)
    Reflect.deleteProperty(store.value.categoryCache, id)
  },
})
