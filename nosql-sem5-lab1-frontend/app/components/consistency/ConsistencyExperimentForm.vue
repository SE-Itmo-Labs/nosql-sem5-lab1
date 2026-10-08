<script setup lang="ts">
import type { ConsistencyExperimentRequest } from '~/types/api'

defineOptions({ name: 'ConsistencyExperimentForm' })
defineProps<{ running: boolean }>()
const emit = defineEmits<{ run: [request: ConsistencyExperimentRequest] }>()

const form = reactive<ConsistencyExperimentRequest>({
  key: 'consistency:demo',
  value: `version-${Date.now()}`,
  readTarget: 'REPLICA',
  mode: 'EVENTUAL',
  waitTimeoutMs: 500,
})
const validationError = ref('')

/** Не отправляет значения, которые backend заведомо отклонит с 400. */
const submit = () => {
  validationError.value = ''
  const request = { ...form, key: form.key.trim(), value: form.value.trim() }
  if (!request.key || !request.value) validationError.value = 'Ключ и значение не могут быть пустыми'
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
      <label class="field"><span class="field-label">Ключ</span><input v-model="form.key" class="field-input" required></label>
      <label class="field"><span class="field-label">Новое значение</span><input v-model="form.value" class="field-input" required></label>
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
