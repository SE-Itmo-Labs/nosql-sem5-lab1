import type {
  Category,
  CreateNotificationRequest,
  Notification,
  NotificationQuery,
  NotificationStats,
  PageResponse,
} from '~/types/api'

/** Значения формы фильтра отделены от формата query-параметров REST API. */
export interface NotificationFilters {
  search: string
  categoryId: number | null
}

type NotificationDraft = CreateNotificationRequest

const createEmptyPage = (): PageResponse<Notification> => ({
  content: [],
  page: 0,
  size: 6,
  totalElements: 0,
  totalPages: 0,
  last: true,
  empty: true,
})

const createEmptyStats = (): NotificationStats => ({
  total: 0,
  sent: 0,
  read: 0,
  byCategory: {},
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
  const stats = ref<NotificationStats>(createEmptyStats())
  const filters = reactive<NotificationFilters>({
    search: '',
    categoryId: null,
  })
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

  /** Собирает query единообразно для списка и агрегированной статистики. */
  const createQuery = (includePage: boolean): NotificationQuery => ({
    userId: user.value?.login,
    q: filters.search.trim() || undefined,
    categoryId: filters.categoryId ?? undefined,
    ...(includePage ? { page: pageData.value.page, size: pageData.value.size } : {}),
  })

  const loadNotifications = async (silent = false) => {
    const requestId = ++latestRequest
    if (!silent) isLoading.value = true
    loadError.value = ''

    try {
      const [nextPage, nextStats] = await Promise.all([
        api.notifications.getAll(createQuery(true)),
        canSendNotifications.value
          ? api.notifications.getStats(createQuery(false))
          : Promise.resolve(createEmptyStats()),
      ])

      // Более старый медленный ответ не должен перезаписать свежий результат.
      if (requestId === latestRequest) {
        const lastPage = Math.max(0, nextPage.totalPages - 1)
        if (nextPage.page > lastPage) {
          pageData.value.page = lastPage
          await loadNotifications(silent)
          return
        }
        pageData.value = nextPage
        stats.value = nextStats
      }
    }
    catch (cause) {
      if (requestId === latestRequest) loadError.value = getErrorMessage(cause)
    }
    finally {
      if (requestId === latestRequest) isLoading.value = false
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
    await Promise.all([loadCategories(), loadNotifications()])
  }

  const applyFilters = async () => {
    pageData.value.page = 0
    await loadNotifications()
  }

  const resetFilters = () => {
    filters.search = ''
    filters.categoryId = null
  }

  const goToPage = async (page: number) => {
    if (page < 0 || page >= pageData.value.totalPages || page === pageData.value.page) return
    pageData.value.page = page
    await loadNotifications()
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
      pageData.value.page = 0
      await loadNotifications()
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
    const requestId = latestRequest

    try {
      await api.notifications.update(notification.id, { status: 'READ' })
      notification.status = 'READ'
      const displayed = pageData.value.content.find(item => item.id === notification.id)
      if (displayed) displayed.status = 'READ'
      if (canSendNotifications.value && requestId === latestRequest) {
        stats.value.sent = Math.max(0, stats.value.sent - 1)
        stats.value.read += 1
      }
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

      // После удаления последней записи возвращаемся на существующую страницу.
      if (pageData.value.content.length === 1 && pageData.value.page > 0) {
        pageData.value.page -= 1
      }
      await loadNotifications()
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      busyNotificationId.value = null
    }
  }

  let searchTimer: ReturnType<typeof setTimeout> | undefined

  const cancelSearch = () => clearTimeout(searchTimer)

  watch(() => filters.search, () => {
    cancelSearch()
    latestRequest += 1
    searchTimer = setTimeout(() => { void applyFilters() }, 300)
  })
  watch(() => filters.categoryId, () => {
    cancelSearch()
    void applyFilters()
  })
  onBeforeUnmount(() => {
    cancelSearch()
    latestRequest += 1
  })

  onMounted(initialize)
  watch(notificationRefreshVersion, () => { void loadNotifications() })

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
    filters,
    goToPage,
    isLoading,
    isSubmitting,
    loadError,
    loadNotifications,
    markAsRead,
    pageData,
    resetFilters,
    stats,
    user,
  }
}
