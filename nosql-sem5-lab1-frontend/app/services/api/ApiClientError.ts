import type { ApiErrorResponse } from '~/types/api'

/**
 * Одинаковая ошибка для настоящего backend и mock-режима.
 * Компоненты могут показывать `message` и при необходимости различать случаи
 * по HTTP-статусу, не анализируя внутренний формат `$fetch`.
 */
export class ApiClientError extends Error {
  readonly status: number
  readonly error: string
  readonly path: string

  constructor(response: ApiErrorResponse) {
    super(response.message)
    this.name = 'ApiClientError'
    this.status = response.status
    this.error = response.error
    this.path = response.path
  }
}

/** Создает стандартную ошибку для локальных mock-сценариев. */
export const createApiClientError = (
  status: number,
  error: string,
  message: string,
  path: string,
): ApiClientError => new ApiClientError({
  timestamp: new Date().toISOString(),
  status,
  error,
  message,
  path,
})

/**
 * Приводит ошибку `$fetch` к публичному формату клиента. Если backend не вернул
 * JSON, сохраняется безопасное сообщение без раскрытия технических деталей.
 */
export const normalizeHttpError = (cause: unknown, path: string): ApiClientError => {
  const candidate = cause as {
    data?: Partial<ApiErrorResponse>
    status?: number
    statusCode?: number
    message?: string
  }
  const status = candidate.statusCode ?? candidate.status ?? candidate.data?.status ?? 500

  return new ApiClientError({
    timestamp: candidate.data?.timestamp ?? new Date().toISOString(),
    status,
    error: candidate.data?.error ?? 'Request Failed',
    message: candidate.data?.message ?? candidate.message ?? 'Не удалось выполнить запрос к серверу',
    path: candidate.data?.path ?? path,
  })
}
