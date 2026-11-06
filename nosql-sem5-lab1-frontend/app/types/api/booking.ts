export interface SeatHold {
  holdId: string
  screeningId: number
  seatIds: number[]
  createdAt: string
  expiresAt: string
  remainingSeconds: number
  status: 'ACTIVE'
}

export interface CreateSeatHoldRequest {
  screeningId: number
  seatIds: number[]
}

export interface Product {
  id: number
  name: string
  price: number
  available: boolean
}

export interface OrderProductRequest {
  productId: number
  quantity: number
}

export interface CreateOrderRequest {
  holdId: string
  products: OrderProductRequest[]
  idempotencyKey: string
}

export interface Ticket {
  id: number
  screeningId: number
  seatId: number
  rowNumber: number
  seatNumber: number
  price: number
  status: 'CONFIRMED' | 'CANCELLED'
  movieTitle: string
  startsAt: string
  auditoriumName: string
}

export interface OrderProduct {
  productId: number
  name: string
  quantity: number
  unitPrice: number
  lineTotal: number
}

export interface CinemaOrder {
  id: number
  status: 'CONFIRMED' | 'CANCELLED'
  totalAmount: number
  createdAt: string
  confirmedAt: string
  cancelledAt?: string | null
  tickets: Ticket[]
  products: OrderProduct[]
}
