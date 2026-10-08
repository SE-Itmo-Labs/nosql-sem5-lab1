import type { ApiClient } from '~/types/api'
import type { MockDatabaseStore } from './database'
import { waitForMockResponse } from './helpers'

const REPLICATION_DELAY_MS = 300

/**
 * Mock делает окно eventual consistency воспроизводимым: обычное чтение видит
 * предыдущее значение реплики, а WAIT синхронизирует ее перед чтением.
 */
export const createMockConsistencyApi = (
  store: MockDatabaseStore,
): ApiClient['consistency'] => ({
  async runExperiment(request) {
    await waitForMockResponse()
    const previousReplicaValue = store.value.replicaValues[request.key] ?? null
    store.value.primaryValues[request.key] = request.value

    let replicasAcked: number | null = null
    let waitDurationMs = 0

    if (request.mode === 'WAIT_FOR_REPLICA') {
      store.value.replicaValues[request.key] = request.value
      replicasAcked = 1
      waitDurationMs = Math.min(18, request.waitTimeoutMs)
    }
    else {
      setTimeout(() => {
        store.value.replicaValues[request.key] = request.value
      }, REPLICATION_DELAY_MS)
    }

    const readValue = request.readTarget === 'PRIMARY'
      ? store.value.primaryValues[request.key] ?? null
      : request.mode === 'WAIT_FOR_REPLICA'
        ? store.value.replicaValues[request.key] ?? null
        : previousReplicaValue

    return {
      key: request.key,
      writtenValue: request.value,
      readValue,
      readTarget: request.readTarget,
      sourceNode: request.readTarget === 'PRIMARY' ? 'redis-primary' : 'redis-replica-1',
      mode: request.mode,
      replicasAcked,
      consistent: readValue === request.value,
      writeDurationMs: 2,
      waitDurationMs,
      readDurationMs: request.readTarget === 'PRIMARY' ? 1 : 3,
    }
  },
})
