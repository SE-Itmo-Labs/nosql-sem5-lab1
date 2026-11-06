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
    if (selection.value.screening?.screeningId !== screening.screeningId) {
      selection.value = { screening, seatIds: [] }
    }
  }

  const toggleSeat = (seatId: number) => {
    const current = selection.value.seatIds
    selection.value = {
      ...selection.value,
      seatIds: current.includes(seatId)
        ? current.filter(id => id !== seatId)
        : [...current, seatId],
    }
  }

  return { selection, selectedSeatIds, selectScreening, toggleSeat }
}
