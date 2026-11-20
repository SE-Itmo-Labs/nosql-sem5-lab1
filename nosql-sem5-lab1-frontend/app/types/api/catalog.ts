export type MovieStatus = 'NOW_SHOWING' | 'COMING_SOON' | 'ARCHIVED'
export type ScreeningStatus = 'SCHEDULED' | 'CANCELLED' | 'COMPLETED'
export type SeatType = 'STANDARD' | 'PREMIUM' | 'ACCESSIBLE'
export type SeatAvailabilityStatus = 'AVAILABLE' | 'HELD' | 'BOOKED'

export interface Movie {
  id: number
  slug: string
  title: string
  description: string
  durationMinutes: number
  ageRating: number
  status: MovieStatus
}

export interface Screening {
  id: number
  movieId: number
  movieTitle: string
  auditoriumId: number
  auditoriumName: string
  startsAt: string
  basePrice: number
  status: ScreeningStatus
}

export interface Auditorium {
  id: number
  name: string
}

export interface ScreeningSeat {
  id: number
  rowNumber: number
  seatNumber: number
  type: SeatType
  status: SeatAvailabilityStatus
}

export interface ScreeningSeats {
  screeningId: number
  movieId: number
  movieTitle: string
  startsAt: string
  basePrice: number
  auditorium: Auditorium
  seats: ScreeningSeat[]
}
