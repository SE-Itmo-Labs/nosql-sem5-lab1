import type { Movie, Screening, ScreeningSeats } from '~/types/api'

const getErrorMessage = (cause: unknown): string => (
  cause instanceof Error ? cause.message : 'Не удалось загрузить каталог'
)

export const useCatalog = () => {
  const api = useApi()
  const movies = ref<Movie[]>([])
  const movie = ref<Movie | null>(null)
  const screenings = ref<Screening[]>([])
  const seatMap = ref<ScreeningSeats | null>(null)
  const isLoading = ref(false)
  const error = ref('')

  const run = async (operation: () => Promise<void>) => {
    isLoading.value = true
    error.value = ''
    try {
      await operation()
    }
    catch (cause) {
      error.value = getErrorMessage(cause)
    }
    finally {
      isLoading.value = false
    }
  }

  const loadMovies = () => run(async () => {
    movies.value = await api.catalog.getMovies()
  })

  const loadMovie = (movieId: number) => run(async () => {
    const [loadedMovie, loadedScreenings] = await Promise.all([
      api.catalog.getMovie(movieId),
      api.catalog.getMovieScreenings(movieId),
    ])
    movie.value = loadedMovie
    screenings.value = loadedScreenings
  })

  const loadSeatMap = (screeningId: number) => run(async () => {
    seatMap.value = await api.catalog.getScreeningSeats(screeningId)
  })

  return {
    error,
    isLoading,
    loadMovie,
    loadMovies,
    loadSeatMap,
    movie,
    movies,
    screenings,
    seatMap,
  }
}
