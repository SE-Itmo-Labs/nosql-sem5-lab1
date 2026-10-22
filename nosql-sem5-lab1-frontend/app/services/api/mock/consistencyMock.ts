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
    const screening = store.value.screenings.find(candidate => candidate.id === request.screeningId)
    if (!screening) throw new Error('Сеанс для эксперимента не найден')
    const key = `laboratory:screening:${screening.id}`
    const versionKey = `${key}:version`
    const version = Number(store.value.primaryValues[versionKey] ?? 0) + 1
    store.value.primaryValues[versionKey] = String(version)
    const value = `screening=${screening.id}|movie=${screening.movieId}|status=${screening.status}|startsAt=${screening.startsAt}|version=${version}`
    const previousReplicaValue = store.value.replicaValues[key] ?? null
    store.value.primaryValues[key] = value

    let replicasAcked: number | null = null
    let waitDurationMs = 0

    if (request.mode === 'WAIT_FOR_REPLICA') {
      store.value.replicaValues[key] = value
      replicasAcked = 1
      waitDurationMs = Math.min(18, request.waitTimeoutMs)
    }
    else {
      setTimeout(() => {
        store.value.replicaValues[key] = value
      }, REPLICATION_DELAY_MS)
    }

    const readValue = request.readTarget === 'PRIMARY'
      ? store.value.primaryValues[key] ?? null
      : request.mode === 'WAIT_FOR_REPLICA'
        ? store.value.replicaValues[key] ?? null
        : previousReplicaValue

    return {
      resourceType: 'SCREENING',
      resourceId: screening.id,
      resourceLabel: screening.movieTitle,
      key,
      writtenValue: value,
      readValue,
      readTarget: request.readTarget,
      sourceNode: request.readTarget === 'PRIMARY' ? 'redis-primary' : 'redis-replica-1',
      mode: request.mode,
      replicasAcked,
      consistent: readValue === value,
      writeDurationMs: 2,
      waitDurationMs,
      readDurationMs: request.readTarget === 'PRIMARY' ? 1 : 3,
    }
  },
})
