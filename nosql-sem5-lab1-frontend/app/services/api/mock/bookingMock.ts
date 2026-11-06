import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient, CinemaOrder } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import type { MockDatabaseStore, MockOrder, MockSeatHold } from './database'
import { remainingSeconds, waitForMockResponse } from './helpers'

const requireUser = (store: MockDatabaseStore) => {
  const user = store.value.users.find(candidate => candidate.username === store.value.currentUsername)
  if (!user) throw createApiClientError(401, 'Unauthorized', 'Требуется вход', '/api/v1')
  return user
}

const cleanupExpiredHolds = (store: MockDatabaseStore) => {
  Object.entries(store.value.seatHolds).forEach(([id, hold]) => {
    if (Date.parse(hold.expiresAt) <= Date.now()) Reflect.deleteProperty(store.value.seatHolds, id)
  })
}

const addSystemNotification = (store: MockDatabaseStore, userId: string, title: string, text: string) => {
  store.value.notifications.unshift({
    id: store.value.nextNotificationId++, userId, title, text, categoryId: 2,
    status: 'SENT', createdAt: new Date().toISOString(), sourceEventId: crypto.randomUUID(),
  })
}

const publicHold = (hold: MockSeatHold) => ({
  holdId: hold.holdId,
  screeningId: hold.screeningId,
  seatIds: hold.seatIds,
  createdAt: hold.createdAt,
  expiresAt: hold.expiresAt,
  remainingSeconds: remainingSeconds(hold.expiresAt),
  status: 'ACTIVE' as const,
})

export const createMockSeatHoldsApi = (store: MockDatabaseStore): ApiClient['seatHolds'] => ({
  async create(request) {
    await waitForMockResponse()
    cleanupExpiredHolds(store)
    const user = requireUser(store)
    const seatIds = [...new Set(request.seatIds)].sort((a, b) => a - b)
    const screening = store.value.screenings.find(candidate => candidate.id === request.screeningId)
    if (!screening || !seatIds.length) {
      throw createApiClientError(400, 'Bad Request', 'Некорректный сеанс или список мест', API_ENDPOINTS.seatHolds.base)
    }
    const seats = store.value.seatsByAuditorium[screening.auditoriumId] ?? []
    if (seatIds.some(id => !seats.some(seat => seat.id === id))) {
      throw createApiClientError(400, 'Bad Request', 'Место не принадлежит залу сеанса', API_ENDPOINTS.seatHolds.base)
    }
    const held = Object.values(store.value.seatHolds).some(hold => (
      hold.screeningId === request.screeningId && hold.seatIds.some(id => seatIds.includes(id))
    ))
    const booked = store.value.orders.some(order => order.tickets.some(ticket => (
      ticket.screeningId === request.screeningId
      && ticket.status === 'CONFIRMED'
      && seatIds.includes(ticket.seatId)
    )))
    if (held || booked) {
      throw createApiClientError(409, 'Conflict', 'Одно или несколько мест уже заняты', API_ENDPOINTS.seatHolds.base)
    }

    const createdAt = new Date()
    const hold: MockSeatHold = {
      holdId: crypto.randomUUID(),
      userId: user.id,
      screeningId: request.screeningId,
      seatIds,
      createdAt: createdAt.toISOString(),
      expiresAt: new Date(createdAt.getTime() + 5 * 60_000).toISOString(),
      remainingSeconds: 300,
      status: 'ACTIVE',
    }
    store.value.seatHolds[hold.holdId] = hold
    addSystemNotification(store, user.username, 'Места временно удерживаются',
      `Сеанс №${hold.screeningId}, места: ${hold.seatIds.join(', ')}.`)
    return publicHold(hold)
  },

  async getById(holdId) {
    await waitForMockResponse()
    cleanupExpiredHolds(store)
    const user = requireUser(store)
    const hold = store.value.seatHolds[holdId]
    if (!hold || hold.userId !== user.id) {
      throw createApiClientError(404, 'Not Found', 'Удержание не найдено или истекло', API_ENDPOINTS.seatHolds.byId(holdId))
    }
    return publicHold(hold)
  },

  async release(holdId) {
    await waitForMockResponse()
    const user = requireUser(store)
    const hold = store.value.seatHolds[holdId]
    if (!hold || hold.userId !== user.id) {
      throw createApiClientError(404, 'Not Found', 'Удержание не найдено', API_ENDPOINTS.seatHolds.byId(holdId))
    }
    Reflect.deleteProperty(store.value.seatHolds, holdId)
  },
})

export const createMockProductsApi = (store: MockDatabaseStore): ApiClient['products'] => ({
  async getAvailable() {
    await waitForMockResponse()
    return store.value.products.filter(product => product.available)
  },
})

export const createMockOrdersApi = (store: MockDatabaseStore): ApiClient['orders'] => ({
  async create(request) {
    await waitForMockResponse()
    cleanupExpiredHolds(store)
    const user = requireUser(store)
    const existing = store.value.orders.find(order => (
      order.userId === user.id && order.idempotencyKey === request.idempotencyKey
    ))
    if (existing) return existing

    const hold = store.value.seatHolds[request.holdId]
    if (!hold || hold.userId !== user.id) {
      throw createApiClientError(404, 'Not Found', 'Удержание не найдено или истекло', API_ENDPOINTS.orders.base)
    }
    const screening = store.value.screenings.find(candidate => candidate.id === hold.screeningId)
    if (!screening) throw new Error('Сеанс удержания не найден')
    const seats = store.value.seatsByAuditorium[screening.auditoriumId] ?? []
    const selectedProducts = request.products.map(item => {
      const product = store.value.products.find(candidate => candidate.id === item.productId && candidate.available)
      if (!product) {
        throw createApiClientError(409, 'Conflict', 'Товар кинобара недоступен', API_ENDPOINTS.orders.base)
      }
      return { product, quantity: item.quantity }
    })
    const createdAt = new Date().toISOString()
    const order: MockOrder = {
      id: store.value.nextOrderId++,
      userId: user.id,
      holdId: hold.holdId,
      idempotencyKey: request.idempotencyKey,
      status: 'CONFIRMED',
      totalAmount: hold.seatIds.length * screening.basePrice + selectedProducts.reduce(
        (sum, item) => sum + item.product.price * item.quantity,
        0,
      ),
      createdAt,
      confirmedAt: createdAt,
      tickets: hold.seatIds.map((seatId) => {
        const seat = seats.find(candidate => candidate.id === seatId)
        if (!seat) throw new Error('Место удержания не найдено')
        return {
          id: store.value.nextTicketId++,
          screeningId: screening.id,
          movieTitle: screening.movieTitle,
          startsAt: screening.startsAt,
          auditoriumName: screening.auditoriumName,
          seatId,
          rowNumber: seat.rowNumber,
          seatNumber: seat.seatNumber,
          price: screening.basePrice,
          status: 'CONFIRMED' as const,
        }
      }),
      products: selectedProducts.map(({ product, quantity }) => ({
        productId: product.id,
        name: product.name,
        quantity,
        unitPrice: product.price,
        lineTotal: product.price * quantity,
      })),
    }
    store.value.orders.unshift(order)
    Reflect.deleteProperty(store.value.seatHolds, hold.holdId)
    addSystemNotification(store, user.username, 'Заказ подтверждён',
      `Заказ №${order.id} подтверждён. Сумма: ${order.totalAmount}.`)
    return order
  },

  async getAll() {
    await waitForMockResponse()
    const user = requireUser(store)
    return store.value.orders.filter(order => order.userId === user.id)
  },

  async getById(orderId): Promise<CinemaOrder> {
    await waitForMockResponse()
    const user = requireUser(store)
    const order = store.value.orders.find(candidate => candidate.id === orderId && candidate.userId === user.id)
    if (!order) throw createApiClientError(404, 'Not Found', 'Заказ не найден', API_ENDPOINTS.orders.byId(orderId))
    return order
  },

  async cancel(orderId) {
    await waitForMockResponse()
    const user = requireUser(store)
    const order = store.value.orders.find(candidate => candidate.id === orderId && candidate.userId === user.id)
    if (!order) throw createApiClientError(404, 'Not Found', 'Заказ не найден', API_ENDPOINTS.orders.byId(orderId))
    if (order.status === 'CANCELLED') return order
    const tooLate = order.tickets.some((ticket) => {
      const screening = store.value.screenings.find(candidate => candidate.id === ticket.screeningId)
      return !screening || Date.parse(screening.startsAt) <= Date.now() + 30 * 60_000
    })
    if (tooLate) {
      throw createApiClientError(409, 'Conflict',
        'Заказ можно отменить не позднее чем за 30 минут до сеанса', API_ENDPOINTS.orders.cancel(orderId))
    }
    order.status = 'CANCELLED'
    order.cancelledAt = new Date().toISOString()
    order.tickets.forEach((ticket) => { ticket.status = 'CANCELLED' })
    addSystemNotification(store, user.username, 'Заказ отменён', `Заказ №${order.id} отменён.`)
    return order
  },
})
