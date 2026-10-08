import type { ConsistencyExperimentRequest, ConsistencyExperimentResponse } from '~/types/api'

const getErrorMessage = (cause: unknown): string => (
  cause instanceof Error ? cause.message : 'Не удалось выполнить эксперимент'
)

/** Хранит последний измеряемый эксперимент primary/replica и его состояние. */
export const useConsistencyExperiment = () => {
  const api = useApi()
  const result = ref<ConsistencyExperimentResponse | null>(null)
  const isRunning = ref(false)
  const error = ref('')

  const runExperiment = async (request: ConsistencyExperimentRequest) => {
    isRunning.value = true
    error.value = ''
    try {
      result.value = await api.consistency.runExperiment(request)
    }
    catch (cause) {
      error.value = getErrorMessage(cause)
    }
    finally {
      isRunning.value = false
    }
  }

  return { error, isRunning, result, runExperiment }
}
