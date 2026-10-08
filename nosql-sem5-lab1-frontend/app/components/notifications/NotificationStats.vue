<script setup lang="ts">
import type { NotificationStats } from '~/types/api'

defineOptions({ name: 'NotificationStatsCards' })

const props = defineProps<{
  stats: NotificationStats
  loading: boolean
}>()

/** Количество категорий считается по полной агрегации, а не по текущей странице. */
const categoryCount = computed(() => Object.values(props.stats.byCategory)
  .filter(count => count > 0).length)
</script>

<template>
  <section class="stats-grid" aria-label="Статистика уведомлений" :aria-busy="loading">
    <article class="stat-card stat-card--total">
      <span>Всего найдено</span>
      <strong>{{ loading ? '—' : stats.total }}</strong>
    </article>
    <article class="stat-card stat-card--sent">
      <span>Статус SENT</span>
      <strong>{{ loading ? '—' : stats.sent }}</strong>
    </article>
    <article class="stat-card stat-card--read">
      <span>Статус READ</span>
      <strong>{{ loading ? '—' : stats.read }}</strong>
    </article>
    <article class="stat-card stat-card--categories">
      <span>Категорий в выборке</span>
      <strong>{{ loading ? '—' : categoryCount }}</strong>
    </article>
  </section>
</template>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  padding: 18px 20px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
}

.stat-card::before {
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: var(--stat-accent);
  content: '';
}

.stat-card span {
  display: block;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 650;
}

.stat-card strong {
  display: block;
  margin-top: 7px;
  font-size: 26px;
  letter-spacing: -0.03em;
}

.stat-card--total { --stat-accent: var(--color-primary); }
.stat-card--sent { --stat-accent: var(--color-info); }
.stat-card--read { --stat-accent: var(--color-success); }
.stat-card--categories { --stat-accent: var(--color-warning); }

@media (max-width: 780px) {
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 430px) {
  .stats-grid { grid-template-columns: 1fr; }
}
</style>
