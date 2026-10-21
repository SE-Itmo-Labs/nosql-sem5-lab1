import { API_ENDPOINTS } from '~/constants/apiEndpoints'
import type { ApiClient, ScreeningSeats } from '~/types/api'
import { createApiClientError } from '../ApiClientError'
import type { MockDatabaseStore } from './database'
import { waitForMockResponse } from './helpers'

export const createMockCatalogApi = (store: MockDatabaseStore): ApiClient['catalog'] => ({
  async getMovies() {
    await waitForMockResponse()
    return store.value.movies.filter(movie => movie.status !== 'ARCHIVED')
  },

  async getMovie(movieId) {
    await waitForMockResponse()
    const movie = store.value.movies.find(candidate => (
      candidate.id === movieId && candidate.status !== 'ARCHIVED'
    ))
    if (!movie) {
      throw createApiClientError(404, 'Not Found', 'Фильм не найден', API_ENDPOINTS.catalog.movie(movieId))
    }
    return movie
  },

  async getMovieScreenings(movieId) {
    await waitForMockResponse()
    return store.value.screenings
      .filter(screening => screening.movieId === movieId && screening.status === 'SCHEDULED')
      .sort((left, right) => left.startsAt.localeCompare(right.startsAt))
  },

  async getScreeningSeats(screeningId): Promise<ScreeningSeats> {
    await waitForMockResponse()
    Object.entries(store.value.seatHolds).forEach(([id, hold]) => {
      if (Date.parse(hold.expiresAt) <= Date.now()) Reflect.deleteProperty(store.value.seatHolds, id)
    })
    const screening = store.value.screenings.find(candidate => candidate.id === screeningId)
    if (!screening) {
      throw createApiClientError(
        404,
        'Not Found',
        'Сеанс не найден',
        API_ENDPOINTS.catalog.screeningSeats(screeningId),
      )
    }
    const auditorium = store.value.auditoriums.find(candidate => candidate.id === screening.auditoriumId)
    if (!auditorium) throw new Error('Зал сеанса не найден')

    return {
      screeningId: screening.id,
      movieId: screening.movieId,
      movieTitle: screening.movieTitle,
      startsAt: screening.startsAt,
      basePrice: screening.basePrice,
      auditorium,
      seats: (store.value.seatsByAuditorium[auditorium.id] ?? []).map((seat) => {
        const booked = store.value.orders.some(order => order.tickets.some(ticket => (
          ticket.screeningId === screeningId
          && ticket.seatId === seat.id
          && ticket.status === 'CONFIRMED'
        )))
        const held = Object.values(store.value.seatHolds).some(hold => (
          hold.screeningId === screeningId && hold.seatIds.includes(seat.id)
        ))
        return { ...seat, status: booked ? 'BOOKED' : held ? 'HELD' : 'AVAILABLE' }
      }),
    }
  },
})
