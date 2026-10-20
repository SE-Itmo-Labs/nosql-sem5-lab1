<script setup lang="ts">
import SeatMap from '~/components/catalog/SeatMap.vue'
import PageHeader from '~/components/ui/PageHeader.vue'
import StatePanel from '~/components/ui/StatePanel.vue'
import { formatPrice, formatScreeningDate, formatScreeningTime } from '~/utils/cinemaFormatters'

defineOptions({ name: 'ScreeningSeatsPage' })
definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Выбор мест' })

const route = useRoute()
const screeningId = Number(route.params.id)
const { error, isLoading, loadSeatMap, seatMap } = useCatalog()
const { selection, selectedSeatIds, selectScreening, toggleSeat } = useBookingSelection()

watch(seatMap, (value) => {
  if (value) selectScreening(value)
})
onMounted(() => loadSeatMap(screeningId))

const selectedSeats = computed(() => seatMap.value?.seats.filter(seat => selectedSeatIds.value.has(seat.id)) ?? [])
const total = computed(() => selectedSeats.value.length * (seatMap.value?.basePrice ?? 0))
</script>

<template>
  <div>
    <NuxtLink v-if="seatMap" class="back-link" :to="`/movies/${seatMap.movieId}`">← Вернуться к сеансам</NuxtLink>
    <StatePanel v-if="isLoading" kind="loading" icon="◌" title="Загружаем зал" description="Проверяем актуальное состояние мест." />
    <StatePanel v-else-if="error || !seatMap" kind="error" icon="!" title="Сеанс недоступен" :description="error || 'Сеанс не найден'">
      <template #actions><button class="btn btn-secondary" type="button" @click="loadSeatMap(screeningId)">Повторить</button></template>
    </StatePanel>
    <template v-else>
      <PageHeader eyebrow="Выбор мест" :title="seatMap.movieTitle" :description="`${formatScreeningDate(seatMap.startsAt)}, ${formatScreeningTime(seatMap.startsAt)} · ${seatMap.auditorium.name}`" />
      <SeatMap :seats="seatMap.seats" :selected-seat-ids="selectedSeatIds" @toggle="toggleSeat" />
      <section class="selection-summary card">
        <div>
          <span>Выбрано мест</span>
          <strong>{{ selectedSeats.length || '—' }}</strong>
        </div>
        <div>
          <span>Места</span>
          <strong>{{ selectedSeats.map(seat => `${seat.rowNumber} ряд, ${seat.seatNumber}`).join('; ') || 'Выберите места на схеме' }}</strong>
        </div>
        <div>
          <span>Итого</span>
          <strong>{{ formatPrice(total) }}</strong>
        </div>
        <button class="btn btn-primary" type="button" :disabled="!selection.seatIds.length">Продолжить</button>
      </section>
      <p class="iteration-note">На следующей итерации кнопка создаст временное удержание мест в Redis.</p>
    </template>
  </div>
</template>

<style scoped>
.back-link { display: inline-block; margin-bottom: 20px; color: var(--color-text-secondary); font-size: 13px; text-decoration: none; }
.selection-summary { display: grid; grid-template-columns: .6fr 1.8fr .8fr auto; align-items: center; gap: 18px; margin-top: 18px; }
.selection-summary div { display: flex; min-width: 0; flex-direction: column; gap: 5px; }
.selection-summary span { color: var(--color-text-muted); font-size: 10px; font-weight: 750; text-transform: uppercase; }
.selection-summary strong { overflow: hidden; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.iteration-note { margin: 12px 0 0; color: var(--color-text-muted); font-size: 12px; text-align: right; }
@media (max-width: 760px) { .selection-summary { grid-template-columns: 1fr 1fr; } .selection-summary div:nth-child(2) { grid-column: 1 / -1; } }
</style>
