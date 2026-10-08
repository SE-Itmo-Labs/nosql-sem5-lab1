import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient, Notification, NotificationQuery } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import type { MockDatabaseStore } from './database'
import { paginate, waitForMockResponse } from './helpers'

/** Применяет те же фильтры, которые определены контрактом списка уведомлений. */
const filterNotifications = (
  notifications: Notification[],
  query: NotificationQuery = {},
): Notification[] => {
  const search = query.q?.trim().toLocaleLowerCase('ru')

  return notifications
    .filter(item => query.userId === undefined || item.userId === query.userId)
    .filter(item => query.categoryId === undefined || item.categoryId === query.categoryId)
    .filter(item => query.status === undefined || item.status === query.status)
    .filter(item => !search || `${item.title} ${item.text}`.toLocaleLowerCase('ru').includes(search))
    .sort((left, right) => Date.parse(right.createdAt) - Date.parse(left.createdAt))
}

/** Mock основного сценария: список, отправка, изменение и статистика. */
export const createMockNotificationsApi = (
  store: MockDatabaseStore,
): ApiClient['notifications'] => ({
  async getAll(query = {}) {
    await waitForMockResponse()
    return paginate(filterNotifications(store.value.notifications, query), query)
  },

  async getByUser(userId, query = {}) {
    await waitForMockResponse()
    const items = filterNotifications(store.value.notifications, { ...query, userId })
    return paginate(items, query)
  },

  async getById(id) {
    await waitForMockResponse()
    const notification = store.value.notifications.find(item => item.id === id)

    if (!notification) {
      throw createApiClientError(404, 'Not Found', 'Уведомление не найдено', API_ENDPOINTS.notifications.byId(id))
    }

    return { ...notification }
  },

  async create(request) {
    await waitForMockResponse()
    const notification: Notification = {
      id: store.value.nextNotificationId++,
      ...request,
      status: 'SENT',
      createdAt: new Date().toISOString(),
    }
    store.value.notifications.push(notification)
    return { ...notification }
  },

  async update(id, request) {
    await waitForMockResponse()
    const index = store.value.notifications.findIndex(item => item.id === id)

    if (index < 0) {
      throw createApiClientError(404, 'Not Found', 'Уведомление не найдено', API_ENDPOINTS.notifications.byId(id))
    }

    const updated = { ...store.value.notifications[index]!, ...request }
    store.value.notifications[index] = updated
    return { ...updated }
  },

  async delete(id) {
    await waitForMockResponse()
    const index = store.value.notifications.findIndex(item => item.id === id)

    if (index < 0) {
      throw createApiClientError(404, 'Not Found', 'Уведомление не найдено', API_ENDPOINTS.notifications.byId(id))
    }

    store.value.notifications.splice(index, 1)
  },

  async getStats(query = {}) {
    await waitForMockResponse()
    const items = filterNotifications(store.value.notifications, query)

    return {
      total: items.length,
      sent: items.filter(item => item.status === 'SENT').length,
      read: items.filter(item => item.status === 'READ').length,
      byCategory: items.reduce<Record<string, number>>((result, item) => {
        result[item.categoryId] = (result[item.categoryId] ?? 0) + 1
        return result
      }, {}),
    }
  },
})
