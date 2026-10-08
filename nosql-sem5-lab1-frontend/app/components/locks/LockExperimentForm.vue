<script setup lang="ts">
import type { ExecuteLockRequest } from '~/types/api'

defineOptions({ name: 'LockExperimentForm' })

defineProps<{ running: boolean }>()

const emit = defineEmits<{ run: [request: ExecuteLockRequest] }>()
const form = reactive({ resourceKey: 'notification:41:delivery', holdMillis: 1500 })
const validationError = ref('')

/** Ограничения повторяют Jakarta Validation из ExecuteLockRequest backend. */
const submit = () => {
  const resourceKey = form.resourceKey.trim()
  validationError.value = ''

  if (!resourceKey) validationError.value = 'Введите ключ ресурса'
  else if (!Number.isInteger(form.holdMillis) || form.holdMillis < 0 || form.holdMillis > 10_000) {
    validationError.value = 'Время удержания должно быть целым числом от 0 до 10000 мс'
  }
  else emit('run', { resourceKey, holdMillis: form.holdMillis })
}
</script>

<template>
  <section class="card lock-form">
    <p class="section-label">Redisson RLock</p>
    <h3 class="card-title">Параллельный запуск</h3>
    <p class="card-text">Два запроса одновременно пытаются выполнить одну критическую секцию.</p>

    <form @submit.prevent="submit">
      <label class="field">
        <span class="field-label">Ключ ресурса</span>
        <input v-model="form.resourceKey" class="field-input" maxlength="160" required>
      </label>
      <label class="field">
        <span class="field-label">Удержание lock, мс</span>
        <input v-model.number="form.holdMillis" class="field-input" type="number" min="0" max="10000" required>
      </label>
      <p v-if="validationError" class="form-error" role="alert">{{ validationError }}</p>
      <button class="btn btn-primary btn-block" type="submit" :disabled="running">
        {{ running ? 'Запросы выполняются…' : 'Запустить две попытки' }}
      </button>
    </form>
  </section>
</template>

<style scoped>
.lock-form { padding: 22px; }
.section-label { margin: 0 0 4px; color: var(--color-text-muted); font-size: 10px; font-weight: 800; text-transform: uppercase; }
.lock-form form { display: grid; gap: 14px; margin-top: 20px; }
</style>
