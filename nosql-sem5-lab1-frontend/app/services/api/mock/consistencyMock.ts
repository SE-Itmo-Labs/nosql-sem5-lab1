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

    let acknowledgedReplicas = 0
    let replicationWaitDurationMs = 0

    if (request.mode === 'WAIT_FOR_REPLICA') {
      store.value.replicaValues[request.key] = request.value
      acknowledgedReplicas = 1
      replicationWaitDurationMs = Math.min(18, request.waitTimeoutMs)
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
      mode: request.mode,
      acknowledgedReplicas,
      consistent: readValue === request.value,
      writeDurationMs: 2,
      replicationWaitDurationMs,
      readDurationMs: request.readTarget === 'PRIMARY' ? 1 : 3,
    }
  },
})
