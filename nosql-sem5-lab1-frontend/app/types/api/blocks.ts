import type { IsoDateTime } from './common'

/** Запрос на временную блокировку места или другого ресурса кинотеатра. */
export interface CreateTemporaryBlockRequest {
  resourceKey: string
  owner: string
  ttlSeconds: number
}

/** Текущее состояние временной блокировки Redis с TTL. */
export interface TemporaryBlock {
  resourceKey: string
  owner: string
  ttlSeconds: number
  remainingTtlSeconds: number
  createdAt: IsoDateTime
  expiresAt: IsoDateTime
  active: boolean
}

/** Запрос фактического POST /api/v1/locks/execute. */
export interface ExecuteLockRequest {
  resourceKey: string
  holdMillis: number
}

/** Backend сам берет и освобождает lock вокруг критической секции. */
export interface ExecuteLockResponse {
  resourceKey: string
  owner: string
  acquired: boolean
  message: string
}
