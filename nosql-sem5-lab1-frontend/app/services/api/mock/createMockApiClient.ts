import type { ApiClient } from '~/types/api'
import { createMockAuthApi } from './authMock'
import { createMockCategoriesApi } from './categoriesMock'
import { createMockConsistencyApi } from './consistencyMock'
import type { MockDatabaseStore } from './database'
import { createMockLocksApi } from './locksMock'
import { createMockNotificationsApi } from './notificationsMock'
import { createMockTemporaryBlocksApi } from './temporaryBlocksMock'

/** Собирает небольшие доменные mock-модули в единый ApiClient. */
export const createMockApiClient = (store: MockDatabaseStore): ApiClient => ({
  auth: createMockAuthApi(store),
  notifications: createMockNotificationsApi(store),
  categories: createMockCategoriesApi(store),
  temporaryBlocks: createMockTemporaryBlocksApi(store),
  locks: createMockLocksApi(store),
  consistency: createMockConsistencyApi(store),
})
