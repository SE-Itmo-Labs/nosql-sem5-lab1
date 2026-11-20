import type { ScreeningSeats } from '~/types/api'

interface BookingSelection {
  screening: ScreeningSeats | null
  seatIds: number[]
}

export const useBookingSelection = () => {
  const selection = useState<BookingSelection>('booking-selection', () => ({
    screening: null,
    seatIds: [],
  }))

  const selectedSeatIds = computed(() => new Set(selection.value.seatIds))

  const selectScreening = (screening: ScreeningSeats) => {
    const sameScreening = selection.value.screening?.screeningId === screening.screeningId
    const availableSeatIds = new Set(screening.seats
      .filter(seat => seat.status === 'AVAILABLE')
      .map(seat => seat.id))

    // Обновляем схему и убираем места, занятые после предыдущего выбора.
    selection.value = {
      screening,
      seatIds: sameScreening
        ? selection.value.seatIds.filter(id => availableSeatIds.has(id))
        : [],
    }
  }

  const clearSelection = () => {
    selection.value = { screening: null, seatIds: [] }
  }

  const toggleSeat = (seatId: number) => {
    const current = selection.value.seatIds
    const seat = selection.value.screening?.seats.find(candidate => candidate.id === seatId)
    if (!current.includes(seatId) && seat?.status !== 'AVAILABLE') return

    selection.value = {
      ...selection.value,
      seatIds: current.includes(seatId)
        ? current.filter(id => id !== seatId)
        : [...current, seatId],
    }
  }

  return { selection, selectedSeatIds, selectScreening, toggleSeat, clearSelection }
}
