import type { ApiClient } from '~/types/api'
import { createMockAuthApi } from './authMock'
import { createMockCategoriesApi } from './categoriesMock'
import { createMockCatalogApi } from './catalogMock'
import { createMockConsistencyApi } from './consistencyMock'
import type { MockDatabaseStore } from './database'
import { createMockNotificationsApi } from './notificationsMock'
import { createMockOrdersApi, createMockProductsApi, createMockSeatHoldsApi } from './bookingMock'

/** Собирает небольшие доменные mock-модули в единый ApiClient. */
export const createMockApiClient = (store: MockDatabaseStore): ApiClient => ({
  auth: createMockAuthApi(store),
  catalog: createMockCatalogApi(store),
  seatHolds: createMockSeatHoldsApi(store),
  products: createMockProductsApi(store),
  orders: createMockOrdersApi(store),
  notifications: createMockNotificationsApi(store),
  categories: createMockCategoriesApi(store),
  consistency: createMockConsistencyApi(store),
})
