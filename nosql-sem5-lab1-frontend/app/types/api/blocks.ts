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

/** Попытка захватить распределенную блокировку от имени клиента. */
export interface AcquireLockRequest {
  resourceKey: string
  owner: string
  ttlSeconds: number
}

/**
 * Токен выдается только владельцу успешной блокировки и требуется для
 * безопасного освобождения через Lua-скрипт.
 */
export interface LockAttemptResponse {
  acquired: boolean
  resourceKey: string
  owner: string | null
  token: string | null
  expiresAt: IsoDateTime | null
  message: string
}

/** Данные для безопасного освобождения ранее захваченной блокировки. */
export interface ReleaseLockRequest {
  resourceKey: string
  token: string
}

/** Результат сравнения токена и удаления ключа блокировки. */
export interface ReleaseLockResponse {
  released: boolean
  resourceKey: string
  message: string
}
