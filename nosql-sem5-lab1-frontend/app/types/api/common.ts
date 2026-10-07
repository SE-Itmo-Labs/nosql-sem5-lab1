/** Дата и время передаются между Java и браузером в формате ISO 8601. */
export type IsoDateTime = string

/** Стандартная ошибка, которую формирует GlobalExceptionHandler на backend. */
export interface ApiErrorResponse {
  timestamp: IsoDateTime
  status: number
  error: string
  message: string
  path: string
}

/** Унифицированная страница данных для списков с серверной пагинацией. */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
  empty: boolean
}

/** Параметры, одинаковые для всех постраничных списков. */
export interface PageQuery {
  page?: number
  size?: number
  sort?: string
}
