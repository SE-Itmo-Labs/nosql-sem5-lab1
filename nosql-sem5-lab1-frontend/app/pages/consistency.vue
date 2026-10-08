<script setup lang="ts">
import ConsistencyExperimentForm from '~/components/consistency/ConsistencyExperimentForm.vue'
import ConsistencyExperimentResult from '~/components/consistency/ConsistencyExperimentResult.vue'
import PageHeader from '~/components/ui/PageHeader.vue'

defineOptions({ name: 'ConsistencyResearchPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Согласованность' })

const { error, isRunning, result, runExperiment } = useConsistencyExperiment()
</script>

<template>
  <div>
    <PageHeader eyebrow="Redis replication" title="Согласованность чтения и записи" description="Сравните немедленное чтение primary или replica с записью, ожидающей подтверждения Redis WAIT." />
    <p v-if="error" class="form-error" role="alert">{{ error }}</p>
    <div class="experiment-layout">
      <ConsistencyExperimentForm :running="isRunning" @run="runExperiment" />
      <ConsistencyExperimentResult :result="result" :running="isRunning" />
    </div>
  </div>
</template>

<style scoped>
.experiment-layout { display: grid; grid-template-columns: minmax(330px, 0.9fr) minmax(0, 1.1fr); align-items: start; gap: 18px; }
.form-error { margin-bottom: 14px; }
@media (max-width: 900px) { .experiment-layout { grid-template-columns: 1fr; } }
</style>
