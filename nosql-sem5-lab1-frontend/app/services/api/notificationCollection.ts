import type {
  Notification,
  NotificationQuery,
  NotificationStats,
  PageResponse,
} from '~/types/api'

/** Применяет фильтры UI к массиву, который возвращает текущий backend. */
export const filterNotifications = (
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

/** Адаптирует массив Spring Boot к PageResponse, который использует экран. */
export const paginateNotifications = (
  notifications: Notification[],
  query: NotificationQuery = {},
): PageResponse<Notification> => {
  const filtered = filterNotifications(notifications, query)
  const page = Math.max(0, query.page ?? 0)
  const size = Math.max(1, query.size ?? 20)
  const totalPages = filtered.length === 0 ? 0 : Math.ceil(filtered.length / size)
  const content = filtered.slice(page * size, (page + 1) * size)

  return {
    content,
    page,
    size,
    totalElements: filtered.length,
    totalPages,
    last: totalPages === 0 || page >= totalPages - 1,
    empty: content.length === 0,
  }
}

/** Статистика вычисляется на клиенте, пока отдельного endpoint на backend нет. */
export const aggregateNotificationStats = (
  notifications: Notification[],
  query: NotificationQuery = {},
): NotificationStats => {
  const filtered = filterNotifications(notifications, query)

  return {
    total: filtered.length,
    sent: filtered.filter(item => item.status === 'SENT').length,
    read: filtered.filter(item => item.status === 'READ').length,
    byCategory: filtered.reduce<Record<string, number>>((result, item) => {
      result[item.categoryId] = (result[item.categoryId] ?? 0) + 1
      return result
    }, {}),
  }
}
