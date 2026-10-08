/** Категория уведомления, сохраненная в постоянном источнике данных. */
export interface Category {
  id: number
  name: string
  description: string
}

/** Поля категории без серверного идентификатора. */
export interface SaveCategoryRequest {
  name: string
  description: string
}

/** Сведения, позволяющие интерфейсу явно показать cache hit или cache miss. */
export interface CategoryCacheInfo {
  source: 'CACHE' | 'DATABASE'
  cached: boolean
  remainingTtlSeconds: number | null
}

/** Категория вместе с результатом обращения к Redis-кэшу. */
export interface CategoryResponse {
  category: Category
  cache: CategoryCacheInfo
}
