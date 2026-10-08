<script setup lang="ts">
import LockExperimentForm from '~/components/locks/LockExperimentForm.vue'
import LockExperimentResult from '~/components/locks/LockExperimentResult.vue'
import PageHeader from '~/components/ui/PageHeader.vue'

defineOptions({ name: 'DistributedLocksPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Распределённый lock' })

const { error, isRunning, results, runCompetition } = useDistributedLock()
</script>

<template>
  <div>
    <PageHeader eyebrow="Redis atomicity" title="Распределённая блокировка" description="Запустите два параллельных запроса: Redisson разрешит выполнить критическую секцию только одному из них." />
    <p v-if="error" class="form-error" role="alert">{{ error }}</p>
    <div class="experiment-layout">
      <LockExperimentForm :running="isRunning" @run="runCompetition" />
      <LockExperimentResult :results="results" :running="isRunning" />
    </div>
  </div>
</template>

<style scoped>
.experiment-layout { display: grid; grid-template-columns: minmax(300px, 0.75fr) minmax(0, 1.25fr); align-items: start; gap: 18px; }
.form-error { margin-bottom: 14px; }
@media (max-width: 840px) { .experiment-layout { grid-template-columns: 1fr; } }
</style>
