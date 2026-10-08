<script setup lang="ts">
import StatePanel from '~/components/ui/StatePanel.vue'
import type { ExecuteLockResponse } from '~/types/api'

defineOptions({ name: 'LockExperimentResult' })
defineProps<{ results: ExecuteLockResponse[], running: boolean }>()
</script>

<template>
  <section class="card result-card">
    <div class="result-heading">
      <div><p>Результат</p><h3>Конкуренция запросов</h3></div>
      <span>POST /api/v1/locks/execute</span>
    </div>

    <StatePanel v-if="running" kind="loading" icon="⋯" title="Lock удерживается" description="Один запрос выполняет критическую секцию, второй конкурирует за тот же ключ." />
    <StatePanel v-else-if="results.length === 0" kind="empty" icon="◇" title="Эксперимент ещё не запущен" description="После запуска здесь будут два независимых ответа backend." />

    <div v-else class="attempts">
      <article v-for="(result, index) in results" :key="index" :class="['attempt', { 'attempt--success': result.acquired }]">
        <div><strong>Попытка {{ index + 1 }}</strong><span>{{ result.acquired ? 'LOCK ПОЛУЧЕН' : 'ОТКАЗ' }}</span></div>
        <p>{{ result.message }}</p>
        <small>{{ result.resourceKey }} · {{ result.owner }}</small>
      </article>
    </div>
  </section>
</template>

<style scoped>
.result-card { padding: 22px; }
.result-heading { display: flex; justify-content: space-between; gap: 12px; padding-bottom: 17px; border-bottom: 1px solid var(--color-border-soft); }
.result-heading p, .result-heading h3 { margin: 0; }
.result-heading p { color: var(--color-text-muted); font-size: 10px; font-weight: 800; text-transform: uppercase; }
.result-heading h3 { margin-top: 3px; font-size: 18px; }
.result-heading > span { color: var(--color-text-muted); font-family: ui-monospace, monospace; font-size: 10px; }
.result-card :deep(.state-panel) { min-height: 260px; margin-top: 18px; }
.attempts { display: grid; gap: 12px; margin-top: 18px; }
.attempt { padding: 15px; border: 1px solid var(--color-border-soft); border-left: 4px solid var(--color-warning); border-radius: var(--radius-sm); }
.attempt--success { border-left-color: var(--color-success); }
.attempt div { display: flex; justify-content: space-between; gap: 10px; }
.attempt span { color: var(--color-text-muted); font-size: 10px; font-weight: 800; }
.attempt p { margin: 8px 0; color: var(--color-text-secondary); font-size: 13px; }
.attempt small { color: var(--color-text-muted); overflow-wrap: anywhere; }
</style>
