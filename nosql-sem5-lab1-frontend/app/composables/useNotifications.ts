import type {
  Category,
  CreateNotificationRequest,
  Notification,
  NotificationSearchQuery,
  NotificationStats,
  PageResponse,
} from '~/types/api'

export const DEFAULT_NOTIFICATION_PAGE_SIZE = 6

type NotificationDraft = CreateNotificationRequest

const createEmptyPage = (): PageResponse<Notification> => ({
  content: [],
  page: 0,
  size: DEFAULT_NOTIFICATION_PAGE_SIZE,
  totalElements: 0,
  totalPages: 0,
  last: true,
  empty: true,
})

const getErrorMessage = (cause: unknown): string => (
  cause instanceof Error ? cause.message : 'Не удалось выполнить запрос'
)

/**
 * Содержит состояние и операции раздела уведомлений. Компоненты отвечают
 * только за отображение и не обращаются к mock или HTTP-клиенту напрямую.
 */
export const useNotifications = () => {
  const api = useApi()
  const { user, restore } = useAuth()
  const canManageHistory = true
  const canSendNotifications = computed(() => user.value?.role === 'ROLE_ADMIN')
  const categories = ref<Category[]>([])
  const pageData = ref<PageResponse<Notification>>(createEmptyPage())
  const stats = ref<NotificationStats | null>(null)
  const statsError = ref('')
  let lastStatsRequest = 0
  let activeQuery: NotificationSearchQuery = { size: DEFAULT_NOTIFICATION_PAGE_SIZE }
  let requestedPage = 0
  const isLoading = ref(true)
  const isSubmitting = ref(false)
  const busyNotificationId = ref<number | null>(null)
  const readingIds = new Set<number>()
  const loadError = ref('')
  const actionError = ref('')
  const feedback = ref('')
  const composerVersion = ref(0)
  let latestRequest = 0
  const notificationRefreshVersion = useState('notification-refresh-version', () => 0)

  const loadNotifications = async (silent = false) => {
    const requestId = ++latestRequest
    if (!silent) isLoading.value = true
    loadError.value = ''

    try {
      const nextPage = await api.notifications.search({ ...activeQuery, page: requestedPage })

      // Более старый медленный ответ не должен перезаписать свежий результат.
      if (requestId === latestRequest) {
        const lastPage = Math.max(0, nextPage.totalPages - 1)
        if (nextPage.page > lastPage) {
          requestedPage = lastPage
          await loadNotifications(silent)
          return
        }
        pageData.value = nextPage
      }
    }
    catch (cause) {
      if (requestId === latestRequest) loadError.value = getErrorMessage(cause)
    }
    finally {
      if (requestId === latestRequest) isLoading.value = false
    }
  }

  // Сводка администратора обновляется отдельно от серверного списка и его фильтров.
  const loadStats = async () => {
    if (!canSendNotifications.value) return
    const requestId = ++lastStatsRequest
    try {
      const result = await api.notifications.getStats({ userId: user.value?.login })
      if (requestId === lastStatsRequest) {
        stats.value = result
        statsError.value = ''
      }
    }
    catch (cause) {
      if (requestId === lastStatsRequest) statsError.value = getErrorMessage(cause)
    }
  }

  /** Справочник загружается отдельно: список уведомлений останется доступным при его ошибке. */
  const loadCategories = async () => {
    try {
      categories.value = await api.categories.getAll()
    }
    catch (cause) {
      actionError.value = `Категории недоступны: ${getErrorMessage(cause)}`
    }
  }

  const initialize = async () => {
    restore()
    await Promise.all([loadCategories(), loadNotifications(), loadStats()])
  }

  const applyFilters = (query: NotificationSearchQuery) => {
    activeQuery = query
    requestedPage = 0
    return loadNotifications()
  }

  const resetFilters = () => applyFilters({ size: DEFAULT_NOTIFICATION_PAGE_SIZE })

  const goToPage = (page: number) => {
    if (page < 0 || page >= pageData.value.totalPages || page === pageData.value.page) return
    requestedPage = page
    return loadNotifications()
  }

  /** Ручная отправка доступна администратору и содержит явного получателя. */
  const createNotification = async (draft: NotificationDraft) => {
    if (!canSendNotifications.value) {
      actionError.value = 'Отправка уведомлений доступна администратору'
      return
    }

    isSubmitting.value = true
    actionError.value = ''
    feedback.value = ''

    try {
      await api.notifications.create(draft)
      feedback.value = 'Уведомление отправлено выбранному клиенту'
      composerVersion.value += 1
      requestedPage = 0
      await Promise.all([loadNotifications(), loadStats()])
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      isSubmitting.value = false
    }
  }

  const markAsRead = async (notification: Notification) => {
    if (notification.status === 'READ' || readingIds.has(notification.id)
      || busyNotificationId.value === notification.id) return
    readingIds.add(notification.id)

    try {
      await api.notifications.update(notification.id, { status: 'READ' })
      notification.status = 'READ'
      const displayed = pageData.value.content.find(item => item.id === notification.id)
      if (displayed) displayed.status = 'READ'
      await Promise.all([loadNotifications(true), loadStats()])
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      readingIds.delete(notification.id)
    }
  }

  const deleteNotification = async (notification: Notification) => {
    if (busyNotificationId.value !== null || readingIds.has(notification.id)) return
    if (!globalThis.confirm(`Удалить уведомление «${notification.title}»?`)) return

    busyNotificationId.value = notification.id
    actionError.value = ''

    try {
      await api.notifications.delete(notification.id)
      feedback.value = 'Уведомление удалено'

      await Promise.all([loadNotifications(), loadStats()])
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      busyNotificationId.value = null
    }
  }

  onBeforeUnmount(() => {
    latestRequest += 1
    lastStatsRequest += 1
  })

  onMounted(initialize)
  watch(notificationRefreshVersion, () => {
    void loadNotifications()
    void loadStats()
  })

  return {
    actionError,
    applyFilters,
    busyNotificationId,
    canManageHistory,
    canSendNotifications,
    categories,
    composerVersion,
    createNotification,
    deleteNotification,
    feedback,
    goToPage,
    isLoading,
    isSubmitting,
    loadError,
    loadNotifications,
    markAsRead,
    pageData,
    resetFilters,
    stats,
    statsError,
  }
}
