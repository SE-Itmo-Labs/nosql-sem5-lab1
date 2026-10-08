import { ApiClientError } from '~/services/api/ApiClientError'
import type { CreateTemporaryBlockRequest, TemporaryBlock } from '~/types/api'

export type TemporaryBlockViewStatus = 'idle' | 'active' | 'expired' | 'missing' | 'released'
type TemporaryBlockDraft = Pick<CreateTemporaryBlockRequest, 'resourceKey' | 'ttlSeconds'>

const getErrorMessage = (cause: unknown): string => (
  cause instanceof Error ? cause.message : 'Не удалось выполнить запрос'
)

/**
 * Управляет жизненным циклом временного Redis-ключа. Локальный таймер нужен
 * только для интерфейса; факт истечения подтверждается повторным GET к API.
 */
export const useTemporaryBlocks = () => {
  const api = useApi()
  const { user, restore } = useAuth()
  const block = ref<TemporaryBlock | null>(null)
  const status = ref<TemporaryBlockViewStatus>('idle')
  const remainingTtl = ref(0)
  const lookupKey = ref('screening:15:seat:7-12')
  const isCreating = ref(false)
  const isChecking = ref(false)
  const isReleasing = ref(false)
  const actionError = ref('')
  const feedback = ref('')
  let countdownTimer: ReturnType<typeof setInterval> | undefined
  let expirationVerificationStarted = false

  const setActiveBlock = (nextBlock: TemporaryBlock) => {
    block.value = { ...nextBlock, active: true }
    lookupKey.value = nextBlock.resourceKey
    remainingTtl.value = Math.max(0, nextBlock.remainingTtlSeconds)
    status.value = 'active'
    expirationVerificationStarted = false
  }

  /** 404 является ожидаемым результатом после TTL, а не ошибкой интерфейса. */
  const isNotFound = (cause: unknown): boolean => (
    cause instanceof ApiClientError && cause.status === 404
  )

  const verifyExpiration = async (resourceKey: string) => {
    if (expirationVerificationStarted) return
    expirationVerificationStarted = true
    isChecking.value = true

    try {
      // Если сервер всё ещё видит ключ, синхронизируем таймер с его ответом.
      setActiveBlock(await api.temporaryBlocks.getByResource(resourceKey))
    }
    catch (cause) {
      if (isNotFound(cause)) {
        status.value = 'expired'
        feedback.value = 'TTL истёк: Redis-ключ автоматически удалён, повторный GET вернул 404'
      }
      else {
        actionError.value = getErrorMessage(cause)
      }
    }
    finally {
      isChecking.value = false
    }
  }

  /** Вычисляет остаток по expiresAt, не уменьшая исходное значение ttlSeconds. */
  const updateCountdown = () => {
    if (!block.value || status.value !== 'active') return

    const seconds = Math.max(0, Math.ceil((Date.parse(block.value.expiresAt) - Date.now()) / 1000))
    remainingTtl.value = seconds
    block.value.remainingTtlSeconds = seconds

    if (seconds === 0) {
      block.value.active = false
      status.value = 'expired'
      void verifyExpiration(block.value.resourceKey)
    }
  }

  const createBlock = async (draft: TemporaryBlockDraft) => {
    if (!user.value) {
      actionError.value = 'Для временной блокировки необходимо войти в систему'
      return
    }

    isCreating.value = true
    actionError.value = ''
    feedback.value = ''

    try {
      const created = await api.temporaryBlocks.create({
        resourceKey: draft.resourceKey,
        ttlSeconds: draft.ttlSeconds,
        owner: user.value.login,
      })
      setActiveBlock(created)
      feedback.value = `Ресурс заблокирован на ${created.ttlSeconds} секунд`
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      isCreating.value = false
    }
  }

  const checkBlock = async () => {
    const resourceKey = lookupKey.value.trim()
    if (!resourceKey) {
      actionError.value = 'Введите ключ ресурса для проверки'
      return
    }

    isChecking.value = true
    actionError.value = ''
    feedback.value = ''

    try {
      setActiveBlock(await api.temporaryBlocks.getByResource(resourceKey))
      feedback.value = 'Активная временная блокировка найдена'
    }
    catch (cause) {
      if (isNotFound(cause)) {
        const wasExpiredResource = block.value?.resourceKey === resourceKey && status.value === 'expired'
        if (!wasExpiredResource) block.value = null
        remainingTtl.value = 0
        status.value = wasExpiredResource ? 'expired' : 'missing'
        feedback.value = wasExpiredResource
          ? 'Ключ отсутствует: автоматическое удаление по TTL подтверждено'
          : 'Активной блокировки для этого ресурса нет'
      }
      else {
        actionError.value = getErrorMessage(cause)
      }
    }
    finally {
      isChecking.value = false
    }
  }

  const releaseBlock = async () => {
    if (!block.value) return

    isReleasing.value = true
    actionError.value = ''
    feedback.value = ''

    try {
      await api.temporaryBlocks.release(block.value.resourceKey)
      block.value.active = false
      remainingTtl.value = 0
      status.value = 'released'
      feedback.value = 'Временная блокировка снята досрочно'
    }
    catch (cause) {
      actionError.value = getErrorMessage(cause)
    }
    finally {
      isReleasing.value = false
    }
  }

  onMounted(() => {
    restore()
    countdownTimer = setInterval(updateCountdown, 250)
  })

  onBeforeUnmount(() => {
    if (countdownTimer) clearInterval(countdownTimer)
  })

  return {
    actionError,
    block,
    checkBlock,
    createBlock,
    feedback,
    isChecking,
    isCreating,
    isReleasing,
    lookupKey,
    releaseBlock,
    remainingTtl,
    status,
    user,
  }
}
