<script setup lang="ts">
import PageHeader from '~/components/ui/PageHeader.vue'
import StatePanel from '~/components/ui/StatePanel.vue'
import type { NotificationReadAnalytics } from '~/types/api'

definePageMeta({ layout: 'dashboard', middleware: ['auth', 'admin'], title: 'Аналитика уведомлений' })

const api = useApi()
const periods = [7, 30, 90]
const days = ref(30)
const result = ref<NotificationReadAnalytics | null>(null)
const loading = ref(true)
const error = ref('')
let latestRequest = 0

const dateFormat = new Intl.DateTimeFormat('ru-RU', { day: 'numeric', month: 'short', year: 'numeric', timeZone: 'UTC' })
const numbers = new Intl.NumberFormat('ru-RU')
const longestAverage = computed(() => Math.max(1, ...result.value?.byDay.map(day => day.averageMs ?? 0) ?? []))

function duration(ms: number | null) {
  if (ms === null) return '—'
  if (ms < 1000) return 'меньше 1 с'
  const seconds = Math.round(ms / 1000)
  if (seconds < 60) return `${seconds} с`
  const minutes = Math.round(seconds / 60)
  if (minutes < 60) return `${minutes} мин`
  const hours = Math.floor(minutes / 60)
  return minutes % 60 ? `${hours} ч ${minutes % 60} мин` : `${hours} ч`
}

async function load() {
  const request = ++latestRequest
  loading.value = true
  error.value = ''
  try {
    const response = await api.notifications.getReadAnalytics(days.value)
    if (request === latestRequest) result.value = response
  }
  catch (cause) {
    if (request === latestRequest) error.value = cause instanceof Error ? cause.message : 'Не удалось загрузить статистику'
  }
  finally {
    if (request === latestRequest) loading.value = false
  }
}

onMounted(load)
watch(days, load)
onBeforeUnmount(() => { latestRequest++ })
</script>

<template>
  <div>
    <PageHeader
      eyebrow=""
      title="Аналитика уведомлений"
      description="Время от создания уведомления до первого прочтения. Период определяется по дате создания в UTC."
    >
      <template #actions>
        <label class="field period">
          <span class="field-label">Период</span>
          <select v-model="days" class="field-input">
            <option v-for="period in periods" :key="period" :value="period">Последние {{ period }} дней</option>
          </select>
        </label>
      </template>
    </PageHeader>

    <StatePanel v-if="loading" kind="loading" title="Загружаем аналитику" />
    <StatePanel v-else-if="error" kind="error" title="Не удалось загрузить аналитику" :description="error">
      <template #actions><button class="btn btn-secondary" type="button" @click="load">Повторить</button></template>
    </StatePanel>
    <StatePanel v-else-if="!result?.summary.totalCount" kind="empty" title="За этот период уведомлений нет" />
    <template v-else>
      <dl class="grid metrics">
        <div class="card">
          <dt>Среднее время до прочтения</dt>
          <dd>{{ duration(result.summary.averageMs) }}</dd>
        </div>
        <div class="card">
          <dt>С записью о прочтении</dt>
          <dd>{{ numbers.format(result.summary.readCount) }}</dd>
        </div>
        <div class="card">
          <dt>Создано за период</dt>
          <dd>{{ numbers.format(result.summary.totalCount) }}</dd>
        </div>
      </dl>

      <section class="card report" aria-labelledby="daily-report">
        <h3 id="daily-report" class="card-title">По дням создания</h3>
        <p v-if="result.summary.readCount === 0" class="card-text">За этот период в журнале нет времени прочтения.</p>
        <div class="table-scroll">
          <table>
            <thead>
              <tr><th scope="col">Дата, UTC</th><th scope="col">Создано</th><th scope="col">С записью о прочтении</th><th scope="col">Среднее время</th></tr>
            </thead>
            <tbody>
              <tr v-for="day in result.byDay" :key="day.day">
                <th scope="row">{{ dateFormat.format(new Date(day.day + 'T00:00:00Z')) }}</th>
                <td>{{ numbers.format(day.totalCount) }}</td>
                <td>{{ numbers.format(day.readCount) }}</td>
                <td>
                  <span>{{ duration(day.averageMs) }}</span>
                  <meter v-if="day.averageMs !== null" :value="day.averageMs" :max="longestAverage" aria-hidden="true" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.period { min-width: 180px; }
.metrics { margin: 0 0 18px; }
.metrics dt { color: var(--color-text-secondary); font-size: 14px; }
.metrics dd { margin: 8px 0 0; font-size: clamp(22px, 3vw, 32px); font-weight: 750; }
.table-scroll { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-variant-numeric: tabular-nums; }
th,
td { padding: 12px 10px; border-bottom: 1px solid var(--color-border-soft); text-align: start; }
thead th { color: var(--color-text-secondary); font-size: 13px; }
tbody th { white-space: nowrap; font-weight: 600; }
tbody tr:last-child > * { border-bottom: 0; }
meter { display: block; width: min(100%, 190px); height: 8px; margin-top: 4px; }
@media (max-width: 600px) {
  .report { padding: 14px; }
  th,
  td { padding: 8px 5px; }
}
</style>
