<script setup lang="ts">
import type { ScreeningSeat } from '~/types/api'

const props = defineProps<{
  seats: ScreeningSeat[]
  selectedSeatIds: Set<number>
}>()

const emit = defineEmits<{ toggle: [seatId: number] }>()

const rows = computed(() => Map.groupBy(props.seats, seat => seat.rowNumber))
</script>

<template>
  <section class="seat-map card" aria-label="Схема мест">
    <div class="screen">Экран</div>
    <div class="seat-rows">
      <div v-for="[rowNumber, rowSeats] in rows" :key="rowNumber" class="seat-row">
        <span class="row-number">{{ rowNumber }}</span>
        <button
          v-for="seat in rowSeats"
          :key="seat.id"
          class="seat"
          :class="[
            `seat--${seat.type.toLowerCase()}`,
            `seat--${seat.status.toLowerCase()}`,
            { 'seat--selected': selectedSeatIds.has(seat.id) },
          ]"
          type="button"
          :disabled="seat.status !== 'AVAILABLE'"
          :aria-pressed="selectedSeatIds.has(seat.id)"
          :aria-label="`Ряд ${seat.rowNumber}, место ${seat.seatNumber}`"
          @click="emit('toggle', seat.id)"
        >
          {{ seat.seatNumber }}
        </button>
      </div>
    </div>
    <div class="legend" aria-label="Обозначения">
      <span><i class="seat-sample seat-sample--available" />Свободно</span>
      <span><i class="seat-sample seat-sample--selected" />Выбрано</span>
      <span><i class="seat-sample seat-sample--premium" />Премиум</span>
      <span><i class="seat-sample seat-sample--unavailable" />Недоступно</span>
    </div>
  </section>
</template>

<style scoped>
.seat-map { overflow-x: auto; }
.screen { width: min(82%, 620px); margin: 0 auto 40px; padding: 9px; border-radius: 50% 50% 8px 8px; background: linear-gradient(#dbeafe, #fff); color: var(--color-text-muted); font-size: 11px; font-weight: 750; letter-spacing: .16em; text-align: center; text-transform: uppercase; box-shadow: 0 -8px 20px rgb(37 99 235 / 10%); }
.seat-rows { display: grid; min-width: 520px; gap: 10px; }
.seat-row { display: flex; align-items: center; justify-content: center; gap: 8px; }
.row-number { width: 24px; color: var(--color-text-muted); font-size: 11px; text-align: center; }
.seat { width: 42px; height: 36px; border: 1px solid #cbd5e1; border-radius: 9px 9px 5px 5px; background: var(--color-surface); color: var(--color-text-secondary); cursor: pointer; font-size: 11px; transition: transform .12s ease, background .12s ease, color .12s ease; }
.seat:hover:not(:disabled) { transform: translateY(-2px); }
.seat--premium { border-color: #c4b5fd; background: #f5f3ff; }
.seat--accessible { border-color: #93c5fd; background: #eff6ff; }
.seat--selected { border-color: var(--color-primary); background: var(--color-primary); color: #fff; }
.seat--held, .seat--booked { border-color: #d1d5db; background: #e5e7eb; color: #9ca3af; cursor: not-allowed; }
.legend { display: flex; flex-wrap: wrap; justify-content: center; gap: 18px; margin-top: 28px; color: var(--color-text-secondary); font-size: 12px; }
.legend span { display: inline-flex; align-items: center; gap: 7px; }
.seat-sample { width: 16px; height: 14px; border: 1px solid #cbd5e1; border-radius: 4px; background: #fff; }
.seat-sample--selected { border-color: var(--color-primary); background: var(--color-primary); }
.seat-sample--premium { border-color: #c4b5fd; background: #f5f3ff; }
.seat-sample--unavailable { border-color: #d1d5db; background: #e5e7eb; }
</style>
