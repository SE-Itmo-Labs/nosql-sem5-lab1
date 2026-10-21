import type {
  Auditorium,
  Category,
  Movie,
  Notification,
  Screening,
  ScreeningSeat,
  SeatHold,
  Product,
  CinemaOrder,
} from '~/types/api'

/**
 * Учетная запись хранит только отпечаток пароля. Так открытый пароль не
 * попадает в сериализованное состояние статической Nuxt-страницы.
 */
export interface MockUser {
  id: number
  username: string
  displayName: string
  role: 'ROLE_USER' | 'ROLE_ADMIN'
  passwordHash: string
}

/** Внутренняя запись кэша содержит момент истечения, недоступный бизнес-модели. */
export interface MockCategoryCacheRecord {
  category: Category
  expiresAt: number
}

/** Внутреннее состояние распределенной блокировки. */
export interface MockSeatHold extends SeatHold {
  userId: number
}

export interface MockOrder extends CinemaOrder {
  userId: number
  holdId: string
  idempotencyKey: string
}

/**
 * Единая локальная база mock-режима. По структуре она повторяет независимые
 * источники backend: постоянные записи, TTL-ключи, кэш и primary/replica.
 */
export interface MockDatabase {
  users: MockUser[]
  movies: Movie[]
  screenings: Screening[]
  auditoriums: Auditorium[]
  seatsByAuditorium: Record<number, ScreeningSeat[]>
  products: Product[]
  seatHolds: Record<string, MockSeatHold>
  orders: MockOrder[]
  notifications: Notification[]
  categories: Category[]
  categoryCache: Record<number, MockCategoryCacheRecord>
  primaryValues: Record<string, string>
  replicaValues: Record<string, string>
  nextNotificationId: number
  nextCategoryId: number
  nextOrderId: number
  nextTicketId: number
  currentUsername: string | null
}

/** Состояние совместимо с Vue Ref, но mock-модули не зависят от Vue напрямую. */
export interface MockDatabaseStore {
  value: MockDatabase
}

/** Начальные данные позволяют сразу открыть интерфейс без работающего backend. */
export const createMockDatabase = (): MockDatabase => {
  const now = Date.now()
  const futureDate = (days: number, hour: number): string => {
    const date = new Date(now + days * 24 * 60 * 60_000)
    date.setHours(hour, 0, 0, 0)
    return date.toISOString()
  }
  const createSeats = (rows: number, perRow: number): ScreeningSeat[] => Array.from(
    { length: rows * perRow },
    (_, index) => {
      const rowNumber = Math.floor(index / perRow) + 1
      const seatNumber = index % perRow + 1
      return {
        id: index + 1,
        rowNumber,
        seatNumber,
        type: rowNumber === 1 && seatNumber <= 2
          ? 'ACCESSIBLE'
          : rowNumber === rows ? 'PREMIUM' : 'STANDARD',
        status: 'AVAILABLE',
      }
    },
  )

  return {
    users: [
      {
        id: 1,
        username: 'client01',
        displayName: 'Тестовый клиент',
        role: 'ROLE_USER',
        passwordHash: '186474c1f2c2f735a54c2cf82ee8e87f2a5cd30940e280029363fecedfc5328c',
      },
      {
        id: 2,
        username: 'admin',
        displayName: 'Администратор',
        role: 'ROLE_ADMIN',
        passwordHash: 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3',
      },
    ],
    movies: [
      {
        id: 1,
        slug: 'northern-lights',
        title: 'Северный свет',
        description: 'Камерная драма о путешествии за полярный круг.',
        durationMinutes: 112,
        ageRating: 12,
        status: 'NOW_SHOWING',
      },
      {
        id: 2,
        slug: 'last-signal',
        title: 'Последний сигнал',
        description: 'Научно-фантастический триллер о сообщении из глубин космоса.',
        durationMinutes: 128,
        ageRating: 16,
        status: 'NOW_SHOWING',
      },
      {
        id: 3,
        slug: 'paper-dragon',
        title: 'Бумажный дракон',
        description: 'Семейная анимационная история о дружбе и смелости.',
        durationMinutes: 94,
        ageRating: 6,
        status: 'NOW_SHOWING',
      },
    ],
    auditoriums: [
      { id: 1, name: 'Зал 1' },
      { id: 2, name: 'Зал 2' },
    ],
    screenings: [
      { id: 1, movieId: 1, movieTitle: 'Северный свет', auditoriumId: 1, auditoriumName: 'Зал 1', startsAt: futureDate(1, 12), basePrice: 450, status: 'SCHEDULED' },
      { id: 2, movieId: 2, movieTitle: 'Последний сигнал', auditoriumId: 1, auditoriumName: 'Зал 1', startsAt: futureDate(1, 16), basePrice: 550, status: 'SCHEDULED' },
      { id: 3, movieId: 3, movieTitle: 'Бумажный дракон', auditoriumId: 2, auditoriumName: 'Зал 2', startsAt: futureDate(1, 13), basePrice: 350, status: 'SCHEDULED' },
      { id: 4, movieId: 1, movieTitle: 'Северный свет', auditoriumId: 2, auditoriumName: 'Зал 2', startsAt: futureDate(2, 18), basePrice: 500, status: 'SCHEDULED' },
      { id: 5, movieId: 2, movieTitle: 'Последний сигнал', auditoriumId: 1, auditoriumName: 'Зал 1', startsAt: futureDate(2, 20), basePrice: 600, status: 'SCHEDULED' },
    ],
    seatsByAuditorium: {
      1: createSeats(5, 8),
      2: createSeats(4, 6).map(seat => ({ ...seat, id: seat.id + 100 })),
    },
    products: [
      { id: 1, name: 'Попкорн солёный', price: 290, available: true },
      { id: 2, name: 'Кола 0,5 л', price: 220, available: true },
      { id: 3, name: 'Начос с соусом', price: 340, available: true },
    ],
    seatHolds: {},
    orders: [],
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
    primaryValues: {},
    replicaValues: {},
    nextNotificationId: 3,
    nextCategoryId: 4,
    nextOrderId: 1,
    nextTicketId: 1,
    currentUsername: null,
  }
}
