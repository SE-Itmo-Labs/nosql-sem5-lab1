<script setup lang="ts">
import type { CategoryResponse } from '~/types/api'

defineOptions({ name: 'CategoryCacheInspector' })

defineProps<{
  result: CategoryResponse | null
  remainingTtl: number | null
  loading: boolean
}>()
</script>

<template>
  <section class="cache-inspector" :class="result ? `cache-inspector--${result.cache.source.toLowerCase()}` : ''">
    <div class="cache-copy">
      <p>Cache-aside inspector</p>
      <h3 v-if="!result">Выберите категорию и проверьте кэш</h3>
      <h3 v-else>{{ result.category.name }}: источник {{ result.cache.source }}</h3>
      <span v-if="!result">
        Первый запрос после очистки читает DATABASE и заполняет Redis. Повторный запрос должен вернуть CACHE.
      </span>
      <span v-else-if="result.cache.source === 'DATABASE'">
        Cache miss: данные прочитаны из постоянного источника и помещены в Redis. Нажмите «Проверить кэш» ещё раз.
      </span>
      <span v-else>
        Cache hit: backend вернул готовое значение из Redis без обращения к постоянному источнику.
      </span>
    </div>

    <div class="cache-metrics" :aria-busy="loading">
      <div>
        <span>Источник</span>
        <strong>{{ loading ? 'WAIT' : result?.cache.source ?? '—' }}</strong>
      </div>
      <div>
        <span>Cached</span>
        <strong>{{ loading ? '…' : result ? String(result.cache.cached) : '—' }}</strong>
      </div>
      <div>
        <span>TTL</span>
        <strong>{{ remainingTtl === null ? '—' : `${remainingTtl} с` }}</strong>
      </div>
    </div>
  </section>
</template>

<style scoped>
.cache-inspector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 22px;
  padding: 20px 22px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
}

.cache-inspector--database { border-color: #fdba74; background: var(--color-primary-soft); }
.cache-inspector--cache { border-color: #86efac; background: var(--color-success-soft); }

.cache-copy {
  max-width: 650px;
}

.cache-copy p,
.cache-copy h3,
.cache-copy span {
  margin: 0;
}

.cache-copy p {
  color: var(--color-text-muted);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.cache-copy h3 {
  margin-top: 5px;
  font-size: 17px;
}

.cache-copy span {
  display: block;
  margin-top: 6px;
  color: var(--color-text-secondary);
  font-size: 12px;
  line-height: 1.55;
}

.cache-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(74px, 1fr));
  flex: 0 0 auto;
  gap: 1px;
  overflow: hidden;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-sm);
  background: var(--color-border-soft);
}

.cache-metrics > div {
  padding: 10px 13px;
  background: var(--color-surface);
  text-align: center;
}

.cache-metrics span,
.cache-metrics strong {
  display: block;
}

.cache-metrics span {
  color: var(--color-text-muted);
  font-size: 9px;
  font-weight: 700;
  text-transform: uppercase;
}

.cache-metrics strong {
  margin-top: 4px;
  font-size: 12px;
}

@media (max-width: 760px) {
  .cache-inspector { align-items: stretch; flex-direction: column; }
  .cache-metrics { width: 100%; }
}
</style>
