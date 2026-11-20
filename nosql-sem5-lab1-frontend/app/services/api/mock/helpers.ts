import type { PageQuery, PageResponse } from '~/types/api'

/** Небольшая задержка делает состояния загрузки видимыми и реалистичными. */
export const waitForMockResponse = (milliseconds = 180): Promise<void> => new Promise(
  resolve => setTimeout(resolve, milliseconds),
)

/** Повторяет серверную пагинацию для любого массива mock-данных. */
export const paginate = <T>(items: T[], query: PageQuery = {}): PageResponse<T> => {
  const page = Math.max(0, query.page ?? 0)
  const size = Math.max(1, query.size ?? 20)
  const start = page * size
  const content = items.slice(start, start + size)
  const totalPages = items.length === 0 ? 0 : Math.ceil(items.length / size)

  return {
    content,
    page,
    size,
    totalElements: items.length,
    totalPages,
    last: totalPages === 0 || page >= totalPages - 1,
    empty: content.length === 0,
  }
}

/** Возвращает количество полных секунд до момента истечения TTL. */
export const remainingSeconds = (expiresAt: string | number): number => {
  const timestamp = typeof expiresAt === 'number' ? expiresAt : Date.parse(expiresAt)
  return Math.max(0, Math.ceil((timestamp - Date.now()) / 1000))
}
