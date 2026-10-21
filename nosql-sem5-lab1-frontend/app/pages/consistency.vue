<script setup lang="ts">
import ConsistencyExperimentForm from '~/components/consistency/ConsistencyExperimentForm.vue'
import ConsistencyExperimentResult from '~/components/consistency/ConsistencyExperimentResult.vue'
import PageHeader from '~/components/ui/PageHeader.vue'
import type { Screening } from '~/types/api'

defineOptions({ name: 'ConsistencyResearchPage' })

definePageMeta({ layout: 'dashboard', middleware: ['auth', 'admin'], title: 'Согласованность' })

const { error, isRunning, result, runExperiment } = useConsistencyExperiment()
const api = useApi()
const screenings = ref<Screening[]>([])

onMounted(async () => {
  const movies = await api.catalog.getMovies()
  screenings.value = (await Promise.all(movies.map(movie => api.catalog.getMovieScreenings(movie.id)))).flat()
})
</script>

<template>
  <div>
    <PageHeader eyebrow="Лаборатория Redis" title="Согласованность проекции сеанса" description="Эксперимент копирует проекцию реального сеанса в изолированный Redis-ключ и сравнивает чтение primary и replica." />
    <p v-if="error" class="form-error" role="alert">{{ error }}</p>
    <div class="experiment-layout">
      <ConsistencyExperimentForm :running="isRunning" :screenings="screenings" @run="runExperiment" />
      <ConsistencyExperimentResult :result="result" :running="isRunning" />
    </div>
  </div>
</template>

<style scoped>
.experiment-layout { display: grid; grid-template-columns: minmax(330px, 0.9fr) minmax(0, 1.1fr); align-items: start; gap: 18px; }
.form-error { margin-bottom: 14px; }
@media (max-width: 900px) { .experiment-layout { grid-template-columns: 1fr; } }
</style>
