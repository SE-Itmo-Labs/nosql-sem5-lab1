<script setup lang="ts">
import StatePanel from '~/components/ui/StatePanel.vue'
import type { ConsistencyExperimentResponse } from '~/types/api'

defineOptions({ name: 'ConsistencyExperimentResult' })
defineProps<{ result: ConsistencyExperimentResponse | null, running: boolean }>()
</script>

<template>
  <section class="card result-card">
    <StatePanel v-if="running" kind="loading" icon="⋯" title="Выполняем запись и чтение" description="Backend измеряет запись, ожидание реплики и контрольное чтение." />
    <StatePanel v-else-if="!result" kind="empty" icon="⇄" title="Нет измерений" description="Выберите режим и выполните первый эксперимент." />

    <template v-else>
      <div class="verdict" :class="{ 'verdict--ok': result.consistent }">
        <span>{{ result.consistent ? '✓' : '!' }}</span>
        <div><small>Результат чтения</small><h3>{{ result.consistent ? 'Значения согласованы' : 'Реплика вернула старое значение' }}</h3></div>
      </div>
      <dl class="values">
        <div><dt>Записано</dt><dd>{{ result.writtenValue }}</dd></div>
        <div><dt>Прочитано</dt><dd>{{ result.readValue ?? 'null' }}</dd></div>
        <div><dt>Источник</dt><dd>{{ result.sourceNode }}</dd></div>
        <div><dt>Подтверждения</dt><dd>{{ result.replicasAcked ?? 'не ожидались' }}</dd></div>
      </dl>
      <div class="timings">
        <div><strong>{{ result.writeDurationMs }}</strong><span>запись, мс</span></div>
        <div><strong>{{ result.waitDurationMs }}</strong><span>WAIT, мс</span></div>
        <div><strong>{{ result.readDurationMs }}</strong><span>чтение, мс</span></div>
      </div>
      <p class="mode">{{ result.mode }} · {{ result.readTarget }} · {{ result.key }}</p>
    </template>
  </section>
</template>

<style scoped>
.result-card { min-height: 360px; padding: 22px; }
.result-card :deep(.state-panel) { min-height: 310px; }
.verdict { display: flex; align-items: center; gap: 12px; padding: 15px; border-radius: var(--radius-sm); background: var(--color-warning-soft); color: var(--color-warning); }
.verdict--ok { background: var(--color-success-soft); color: var(--color-success); }
.verdict > span { font-size: 26px; font-weight: 800; }
.verdict small, .verdict h3 { margin: 0; }
.verdict small { font-size: 10px; text-transform: uppercase; }
.verdict h3 { margin-top: 2px; font-size: 16px; }
.values { display: grid; margin: 18px 0; }
.values div { display: grid; grid-template-columns: 110px minmax(0, 1fr); gap: 10px; padding: 9px 0; border-bottom: 1px solid var(--color-border-soft); }
.values dt { color: var(--color-text-muted); font-size: 11px; }
.values dd { overflow-wrap: anywhere; margin: 0; text-align: right; font-family: ui-monospace, monospace; font-size: 11px; }
.timings { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.timings div { padding: 12px; border-radius: var(--radius-sm); background: var(--color-bg-subtle); text-align: center; }
.timings strong, .timings span { display: block; }
.timings strong { font-size: 21px; }
.timings span { margin-top: 3px; color: var(--color-text-muted); font-size: 10px; }
.mode { margin: 15px 0 0; color: var(--color-text-muted); font-family: ui-monospace, monospace; font-size: 10px; text-align: center; overflow-wrap: anywhere; }
</style>
