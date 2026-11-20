import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import { readAnalytics } from './notificationAnalyticsMock'
import { filterNotifications } from '../notificationCollection'
import type { MockDatabaseStore, MockNotification } from './database'
import { paginate, waitForMockResponse } from './helpers'

/** Mock основного сценария: список, отправка, изменение и статистика. */
export const createMockNotificationsApi = (
  store: MockDatabaseStore,
  getCurrentUsername: () => string | null,
): ApiClient['notifications'] => ({
  async getReadAnalytics(days) {
    await waitForMockResponse()
    const user = store.value.users.find(user => user.username === getCurrentUsername())
    if (!user) throw createApiClientError(401, 'Unauthorized', 'Требуется вход', API_ENDPOINTS.adminNotifications.readAnalytics)
    if (user.role !== 'ROLE_ADMIN') {
      throw createApiClientError(403, 'Forbidden', 'Доступно администратору', API_ENDPOINTS.adminNotifications.readAnalytics)
    }
    if (!Number.isInteger(days) || days < 1 || days > 90) {
      throw createApiClientError(400, 'Bad Request', 'Период от 1 до 90 дней', API_ENDPOINTS.adminNotifications.readAnalytics)
    }
    return readAnalytics(store.value.notifications, days)
  },

  async search(query = {}) {
    await waitForMockResponse()
    const userId = getCurrentUsername()
    if (!userId) {
      throw createApiClientError(401, 'Unauthorized', 'Требуется вход', API_ENDPOINTS.notifications.search)
    }

    const from = query.updatedFrom === undefined ? -Infinity : Date.parse(query.updatedFrom)
    const to = query.updatedTo === undefined ? Infinity : Date.parse(query.updatedTo)
    const page = query.page ?? 0
    const size = query.size ?? 20
    if (!(from < to) || !Number.isInteger(page) || page < 0 || !Number.isInteger(size) || size < 1 || size > 100) {
      throw createApiClientError(400, 'Bad Request', 'Некорректные параметры поиска', API_ENDPOINTS.notifications.search)
    }

    const order = query.sort ?? 'UPDATED_AT_DESC'
    const useCreatedAt = order.startsWith('CREATED_AT')
    const direction = order.endsWith('_ASC') ? 1 : -1
    const items = filterNotifications(store.value.notifications, { ...query, userId })
      .filter((item) => {
        const updatedAt = Date.parse(item.updatedAt ?? item.createdAt)
        return updatedAt >= from && updatedAt < to
          && (query.processingRuleId === undefined || item.processingRuleId === query.processingRuleId)
          && (query.tag === undefined || item.tags?.includes(query.tag))
          && ((query.actionType === undefined && query.actorId === undefined)
            || item.actionLog?.some(action => (
              (query.actionType === undefined || action.type === query.actionType)
              && (query.actorId === undefined || action.actorId === query.actorId)
            )))
      })
      .sort((left, right) => {
        const timestamp = (item: MockNotification) => Date.parse(useCreatedAt ? item.createdAt : item.updatedAt ?? item.createdAt)
        return direction * (timestamp(left) - timestamp(right) || left.id - right.id)
      })
      .map(({ actionLog: _actionLog, ...item }) => ({
        ...item,
        updatedAt: item.updatedAt ?? item.createdAt,
        tags: item.tags ?? [],
        processingRuleId: item.processingRuleId ?? null,
      }))

    return paginate(items, { page, size })
  },

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
    const createdAt = new Date().toISOString()
    const notification: MockNotification = {
      id: store.value.nextNotificationId++,
      ...request,
      status: 'SENT',
      createdAt,
      updatedAt: createdAt,
      actionLog: [{ type: 'CREATED', actorId: getCurrentUsername() ?? 'system' }],
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

    const current = store.value.notifications[index]!
    const updated = { ...current, ...request }
    if (request.status === 'READ' && current.status !== 'READ') {
      updated.updatedAt = new Date().toISOString()
      updated.actionLog = [...current.actionLog ?? [], { type: 'READ', actorId: current.userId, at: updated.updatedAt }]
    }
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
