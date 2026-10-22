<script setup lang="ts">
import type { ConsistencyExperimentRequest, Screening } from '~/types/api'

defineOptions({ name: 'ConsistencyExperimentForm' })
const props = defineProps<{ running: boolean, screenings: Screening[] }>()
const emit = defineEmits<{ run: [request: ConsistencyExperimentRequest] }>()

const form = reactive<ConsistencyExperimentRequest>({
  screeningId: 0,
  readTarget: 'REPLICA',
  mode: 'EVENTUAL',
  waitTimeoutMs: 500,
})
const validationError = ref('')

/** Не отправляет значения, которые backend заведомо отклонит с 400. */
const submit = () => {
  validationError.value = ''
  const request = { ...form }
  if (!props.screenings.some(screening => screening.id === request.screeningId)) validationError.value = 'Выберите реальный сеанс'
  else if (!Number.isInteger(request.waitTimeoutMs) || request.waitTimeoutMs < 1 || request.waitTimeoutMs > 5000) {
    validationError.value = 'Таймаут WAIT должен быть целым числом от 1 до 5000 мс'
  }
  else emit('run', request)
}
</script>

<template>
  <section class="card experiment-form">
    <p class="section-label">Primary → replica</p>
    <h3 class="card-title">Параметры чтения</h3>
    <form @submit.prevent="submit">
      <label class="field"><span class="field-label">Сеанс кинотеатра</span><select v-model.number="form.screeningId" class="field-input" required><option :value="0" disabled>Выберите сеанс</option><option v-for="screening in screenings" :key="screening.id" :value="screening.id">{{ screening.movieTitle }} · {{ new Date(screening.startsAt).toLocaleString('ru-RU') }}</option></select></label>
      <div class="form-row">
        <label class="field"><span class="field-label">Читать из</span><select v-model="form.readTarget" class="field-input"><option value="PRIMARY">PRIMARY</option><option value="REPLICA">REPLICA</option></select></label>
        <label class="field"><span class="field-label">Режим записи</span><select v-model="form.mode" class="field-input"><option value="EVENTUAL">EVENTUAL</option><option value="WAIT_FOR_REPLICA">WAIT_FOR_REPLICA</option></select></label>
      </div>
      <label class="field"><span class="field-label">WAIT timeout, мс</span><input v-model.number="form.waitTimeoutMs" class="field-input" type="number" min="1" max="5000" required></label>
      <p v-if="validationError" class="form-error" role="alert">{{ validationError }}</p>
      <button class="btn btn-primary btn-block" type="submit" :disabled="running">{{ running ? 'Измеряем…' : 'Выполнить эксперимент' }}</button>
    </form>
  </section>
</template>

<style scoped>
.experiment-form { padding: 22px; }
.section-label { margin: 0 0 4px; color: var(--color-text-muted); font-size: 10px; font-weight: 800; text-transform: uppercase; }
.experiment-form form { display: grid; gap: 14px; margin-top: 20px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (max-width: 560px) { .form-row { grid-template-columns: 1fr; } }
</style>
