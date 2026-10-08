/** Узел Redis, из которого будет выполнено контрольное чтение. */
export type RedisReadTarget = 'PRIMARY' | 'REPLICA'

/**
 * EVENTUAL сразу читает асинхронную реплику, WAIT_FOR_REPLICA сначала вызывает
 * Redis WAIT и только затем выполняет чтение.
 */
export type ReplicationMode = 'EVENTUAL' | 'WAIT_FOR_REPLICA'

/** Параметры воспроизводимого эксперимента записи и последующего чтения. */
export interface ConsistencyExperimentRequest {
  key: string
  value: string
  readTarget: RedisReadTarget
  mode: ReplicationMode
  waitTimeoutMs: number
}

/** Метрики и фактические значения, которые frontend покажет пользователю. */
export interface ConsistencyExperimentResponse {
  key: string
  writtenValue: string
  readValue: string | null
  readTarget: RedisReadTarget
  sourceNode: string
  mode: ReplicationMode
  replicasAcked: number | null
  consistent: boolean
  writeDurationMs: number
  waitDurationMs: number
  readDurationMs: number
}
