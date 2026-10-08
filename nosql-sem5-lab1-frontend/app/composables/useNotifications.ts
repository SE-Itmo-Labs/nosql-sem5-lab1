import type {
  Category,
  CreateNotificationRequest,
  Notification,
  NotificationQuery,
  NotificationStats,
  NotificationStatus,
  PageResponse,
} from '~/types/api'

/** Значения формы фильтра отделены от формата query-параметров REST API. */
export interface NotificationFilters {
  search: string
  categoryId: number | null
  status: NotificationStatus | null
}

type NotificationDraft = Omit<CreateNotificationRequest, 'userId'>

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
  const categories = ref<Category[]>([])
  const pageData = ref<PageResponse<Notification>>(createEmptyPage())
  const stats = ref<NotificationStats>(createEmptyStats())
  const filters = reactive<NotificationFilters>({
    search: '',
    categoryId: null,
    status: null,
  })
  const isLoading = ref(true)
  const isSubmitting = ref(false)
  const busyNotificationId = ref<number | null>(null)
  const loadError = ref('')
  const actionError = ref('')
  const feedback = ref('')
  const composerVersion = ref(0)
  let latestRequest = 0

  /** Собирает query единообразно для списка и агрегированной статистики. */
  const createQuery = (includePage: boolean): NotificationQuery => ({
    userId: user.value?.username,
    q: filters.search.trim() || undefined,
    categoryId: filters.categoryId ?? undefined,
    status: filters.status ?? undefined,
    ...(includePage ? { page: pageData.value.page, size: pageData.value.size } : {}),
  })

  /** Загружает список и метрики вместе, чтобы они всегда отражали один фильтр. */
  const loadNotifications = async () => {
    const requestId = ++latestRequest
    isLoading.value = true
    loadError.value = ''

    try {
      const [nextPage, nextStats] = await Promise.all([
        api.notifications.getAll(createQuery(true)),
        api.notifications.getStats(createQuery(false)),
      ])

      // Более старый медленный ответ не должен перезаписать свежий результат.
      if (requestId === latestRequest) {
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

  const resetFilters = async () => {
    filters.search = ''
    filters.categoryId = null
    filters.status = null
    await applyFilters()
  }

  const goToPage = async (page: number) => {
    if (page < 0 || page >= pageData.value.totalPages || page === pageData.value.page) return
    pageData.value.page = page
    await loadNotifications()
  }

  /** Отправитель всегда совпадает с текущим клиентом и не вводится вручную. */
  const createNotification = async (draft: NotificationDraft) => {
    if (!user.value) {
      actionError.value = 'Для отправки уведомления необходимо войти в систему'
      return
    }

    isSubmitting.value = true
    actionError.value = ''
    feedback.value = ''

    try {
      await api.notifications.create({ userId: user.value.username, ...draft })
      feedback.value = 'Уведомление отправлено и добавлено в историю'
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
    busyNotificationId.value = notification.id
    actionError.value = ''

    try {
      await api.notifications.update(notification.id, { status: 'READ' })
      feedback.value = `Уведомление «${notification.title}» отмечено прочитанным`
      await loadNotifications()
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      busyNotificationId.value = null
    }
  }

  const deleteNotification = async (notification: Notification) => {
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

  onMounted(initialize)

  return {
    actionError,
    applyFilters,
    busyNotificationId,
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
