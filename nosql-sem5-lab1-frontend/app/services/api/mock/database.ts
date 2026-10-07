import type { Category, Notification, TemporaryBlock } from '~/types/api'

/** Учетная запись хранит пароль только внутри локального mock-хранилища. */
export interface MockUser {
  username: string
  email: string
  password: string
}

/** Внутренняя запись кэша содержит момент истечения, недоступный бизнес-модели. */
export interface MockCategoryCacheRecord {
  category: Category
  expiresAt: number
}

/** Внутреннее состояние распределенной блокировки. */
export interface MockLockRecord {
  resourceKey: string
  owner: string
  token: string
  expiresAt: number
}

/**
 * Единая локальная база mock-режима. По структуре она повторяет независимые
 * источники backend: постоянные записи, TTL-ключи, кэш и primary/replica.
 */
export interface MockDatabase {
  users: MockUser[]
  notifications: Notification[]
  categories: Category[]
  categoryCache: Record<number, MockCategoryCacheRecord>
  temporaryBlocks: Record<string, TemporaryBlock>
  locks: Record<string, MockLockRecord>
  primaryValues: Record<string, string>
  replicaValues: Record<string, string>
  nextNotificationId: number
  nextCategoryId: number
}

/** Состояние совместимо с Vue Ref, но mock-модули не зависят от Vue напрямую. */
export interface MockDatabaseStore {
  value: MockDatabase
}

/** Начальные данные позволяют сразу открыть интерфейс без работающего backend. */
export const createMockDatabase = (): MockDatabase => {
  const now = Date.now()

  return {
    users: [
      { username: 'client01', email: 'client01@example.com', password: 'client123' },
      { username: 'admin', email: 'admin@example.com', password: '123' },
    ],
    categories: [
      { id: 1, name: 'Системное', description: 'Служебные сообщения кинотеатра' },
      { id: 2, name: 'Бронирование', description: 'Изменения состояния билета' },
      { id: 3, name: 'Премьера', description: 'Новые фильмы и специальные показы' },
    ],
    notifications: [
      {
        id: 1,
        userId: 'client01',
        title: 'Билет забронирован',
        text: 'Сеанс в 19:30, зал 4, ряд 7, место 12.',
        categoryId: 2,
        status: 'SENT',
        createdAt: new Date(now - 15 * 60_000).toISOString(),
      },
      {
        id: 2,
        userId: 'client01',
        title: 'Премьера недели',
        text: 'В пятницу открывается продажа билетов на новый фильм.',
        categoryId: 3,
        status: 'READ',
        createdAt: new Date(now - 24 * 60 * 60_000).toISOString(),
      },
    ],
    categoryCache: {},
    temporaryBlocks: {},
    locks: {},
    primaryValues: {},
    replicaValues: {},
    nextNotificationId: 3,
    nextCategoryId: 4,
  }
}
