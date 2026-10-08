import type { ExecuteLockRequest, ExecuteLockResponse } from '~/types/api'

const getErrorMessage = (cause: unknown): string => (
  cause instanceof Error ? cause.message : 'Не удалось выполнить критическую секцию'
)

/** Управляет демонстрацией двух конкурирующих запросов к одному Redis-lock. */
export const useDistributedLock = () => {
  const api = useApi()
  const results = ref<ExecuteLockResponse[]>([])
  const isRunning = ref(false)
  const error = ref('')

  const runCompetition = async (request: ExecuteLockRequest) => {
    isRunning.value = true
    results.value = []
    error.value = ''

    try {
      // Запросы стартуют одновременно: первый удерживает lock, второй фиксирует конфликт.
      results.value = await Promise.all([
        api.locks.execute(request),
        api.locks.execute(request),
      ])
    }
    catch (cause) {
      error.value = getErrorMessage(cause)
    }
    finally {
      isRunning.value = false
    }
  }

  return { error, isRunning, results, runCompetition }
}
