import type { Category, CategoryResponse, SaveCategoryRequest } from '~/types/api'

const getErrorMessage = (cause: unknown): string => (
  cause instanceof Error ? cause.message : 'Не удалось выполнить запрос'
)

/**
 * Управляет справочником и отдельным контрольным запросом cache-aside.
 * Обычный список не выдается за кэш-проверку: источник виден только после
 * GET конкретной категории, как зафиксировано в API-контракте.
 */
export const useCategories = () => {
  const api = useApi()
  const categories = ref<Category[]>([])
  const inspected = ref<CategoryResponse | null>(null)
  const inspectedAt = ref(0)
  const editorCategory = ref<Category | null>(null)
  const editorVersion = ref(0)
  const busyCategoryId = ref<number | null>(null)
  const isLoading = ref(true)
  const isInspecting = ref(false)
  const isSaving = ref(false)
  const loadError = ref('')
  const actionError = ref('')
  const feedback = ref('')
  const clock = ref(Date.now())
  let clockTimer: ReturnType<typeof setInterval> | undefined

  /** Уменьшает показанный TTL между запросами, не имитируя новый ответ backend. */
  const visibleCacheTtl = computed(() => {
    const initialTtl = inspected.value?.cache.remainingTtlSeconds
    if (initialTtl === null || initialTtl === undefined) return null

    const elapsedSeconds = Math.floor((clock.value - inspectedAt.value) / 1000)
    return Math.max(0, initialTtl - elapsedSeconds)
  })

  const loadCategories = async () => {
    isLoading.value = true
    loadError.value = ''

    try {
      categories.value = await api.categories.getAll()
    }
    catch (cause) {
      loadError.value = getErrorMessage(cause)
    }
    finally {
      isLoading.value = false
    }
  }

  /** Каждый клик делает настоящий API-вызов и поэтому показывает miss, затем hit. */
  const inspectCategory = async (category: Category) => {
    isInspecting.value = true
    busyCategoryId.value = category.id
    actionError.value = ''
    feedback.value = ''

    try {
      inspected.value = await api.categories.getById(category.id)
      inspectedAt.value = Date.now()
      clock.value = inspectedAt.value
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      isInspecting.value = false
      busyCategoryId.value = null
    }
  }

  const startCreate = () => {
    editorCategory.value = null
    editorVersion.value += 1
    actionError.value = ''
  }

  const startEdit = (category: Category) => {
    editorCategory.value = { ...category }
    editorVersion.value += 1
    actionError.value = ''
  }

  const saveCategory = async (request: SaveCategoryRequest) => {
    isSaving.value = true
    actionError.value = ''
    feedback.value = ''

    try {
      if (editorCategory.value) {
        const updated = await api.categories.update(editorCategory.value.id, request)
        feedback.value = `Категория «${updated.name}» обновлена, ее кэш инвалидирован`

        // Старый индикатор CACHE после PUT больше не отражает состояние backend.
        if (inspected.value?.category.id === updated.id) inspected.value = null
      }
      else {
        const created = await api.categories.create(request)
        feedback.value = `Категория «${created.name}» создана`
      }

      editorCategory.value = null
      editorVersion.value += 1
      await loadCategories()
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      isSaving.value = false
    }
  }

  const deleteCategory = async (category: Category) => {
    if (!globalThis.confirm(`Удалить категорию «${category.name}»?`)) return

    busyCategoryId.value = category.id
    actionError.value = ''
    feedback.value = ''

    try {
      await api.categories.delete(category.id)
      feedback.value = `Категория «${category.name}» удалена вместе с записью кэша`
      if (inspected.value?.category.id === category.id) inspected.value = null
      if (editorCategory.value?.id === category.id) startCreate()
      await loadCategories()
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      busyCategoryId.value = null
    }
  }

  onMounted(() => {
    void loadCategories()
    clockTimer = setInterval(() => { clock.value = Date.now() }, 1000)
  })

  onBeforeUnmount(() => {
    if (clockTimer) clearInterval(clockTimer)
  })

  return {
    actionError,
    busyCategoryId,
    categories,
    deleteCategory,
    editorCategory,
    editorVersion,
    feedback,
    inspectCategory,
    inspected,
    isInspecting,
    isLoading,
    isSaving,
    loadCategories,
    loadError,
    saveCategory,
    startCreate,
    startEdit,
    visibleCacheTtl,
  }
}
