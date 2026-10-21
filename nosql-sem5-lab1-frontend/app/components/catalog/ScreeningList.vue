<script setup lang="ts">
import type { Screening } from '~/types/api'
import { formatPrice, formatScreeningDate, formatScreeningTime } from '~/utils/cinemaFormatters'

defineProps<{ screenings: Screening[] }>()
</script>

<template>
  <div class="screening-list">
    <NuxtLink
      v-for="screening in screenings"
      :key="screening.id"
      class="screening-card"
      :to="`/screenings/${screening.id}`"
    >
      <div>
        <span class="screening-date">{{ formatScreeningDate(screening.startsAt) }}</span>
        <strong>{{ formatScreeningTime(screening.startsAt) }}</strong>
      </div>
      <div>
        <span class="screening-label">Зал</span>
        <strong>{{ screening.auditoriumName }}</strong>
      </div>
      <div>
        <span class="screening-label">Цена от</span>
        <strong>{{ formatPrice(screening.basePrice) }}</strong>
      </div>
      <span class="screening-action">Выбрать места →</span>
    </NuxtLink>
  </div>
</template>

<style scoped>
.screening-list { display: grid; gap: 12px; }
.screening-card { display: grid; grid-template-columns: 1.25fr 1fr 1fr auto; align-items: center; gap: 18px; padding: 18px 20px; border: 1px solid var(--color-border-soft); border-radius: var(--radius-md); background: var(--color-surface); box-shadow: var(--shadow-sm); text-decoration: none; transition: border-color .15s ease, transform .15s ease; }
.screening-card:hover { border-color: #fdba74; transform: translateX(3px); }
.screening-card div { display: flex; flex-direction: column; gap: 4px; }
.screening-date, .screening-label { color: var(--color-text-muted); font-size: 11px; font-weight: 700; text-transform: uppercase; }
.screening-card strong { font-size: 15px; }
.screening-action { color: var(--color-primary-hover); font-size: 13px; font-weight: 750; }
@media (max-width: 700px) { .screening-card { grid-template-columns: 1fr 1fr; } .screening-action { grid-column: 1 / -1; } }
</style>
