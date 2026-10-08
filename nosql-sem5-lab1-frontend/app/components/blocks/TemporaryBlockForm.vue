<script setup lang="ts">
import type { CreateTemporaryBlockRequest } from '~/types/api'

defineOptions({ name: 'TemporaryBlockCreateForm' })

defineProps<{
  username: string
  submitting: boolean
}>()

const emit = defineEmits<{
  submit: [draft: Pick<CreateTemporaryBlockRequest, 'resourceKey' | 'ttlSeconds'>]
}>()

const form = reactive({
  resourceKey: 'screening:15:seat:7-12',
  ttlSeconds: 30,
})
const validationError = ref('')

const submitForm = () => {
  const resourceKey = form.resourceKey.trim()
  validationError.value = ''

  if (!resourceKey) {
    validationError.value = 'Введите ключ блокируемого ресурса'
    return
  }

  if (!Number.isInteger(form.ttlSeconds) || form.ttlSeconds < 1 || form.ttlSeconds > 3600) {
    validationError.value = 'TTL должен быть целым числом от 1 до 3600 секунд'
    return
  }

  emit('submit', { resourceKey, ttlSeconds: form.ttlSeconds })
}
</script>

<template>
  <section class="block-form card">
    <div class="form-heading">
      <span aria-hidden="true">◷</span>
      <div>
        <p>SET key value EX ttl</p>
        <h3>Создать временную бронь</h3>
      </div>
    </div>

    <form class="form-fields" @submit.prevent="submitForm">
      <label class="field">
        <span class="field-label">Ключ ресурса</span>
        <input
          v-model="form.resourceKey"
          class="field-input"
          maxlength="160"
          placeholder="screening:15:seat:7-12"
          required
        >
      </label>

      <div class="form-row">
        <label class="field">
          <span class="field-label">Владелец</span>
          <input class="field-input" type="text" :value="username" disabled>
        </label>
        <label class="field">
          <span class="field-label">TTL, секунд</span>
          <input v-model.number="form.ttlSeconds" class="field-input" type="number" min="1" max="3600" required>
        </label>
      </div>

      <div class="ttl-presets" aria-label="Быстрый выбор TTL">
        <button v-for="seconds in [10, 30, 60, 300]" :key="seconds" type="button" @click="form.ttlSeconds = seconds">
          {{ seconds < 60 ? `${seconds} с` : `${seconds / 60} мин` }}
        </button>
      </div>

      <p v-if="validationError" class="form-error" role="alert">{{ validationError }}</p>

      <button class="btn btn-primary btn-block" type="submit" :disabled="submitting">
        {{ submitting ? 'Создаём ключ…' : 'Заблокировать ресурс' }}
      </button>
    </form>
  </section>
</template>

<style scoped>
.block-form {
  padding: 22px;
}

.form-heading {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 17px;
  border-bottom: 1px solid var(--color-border-soft);
}

.form-heading > span {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 12px;
  background: var(--color-success-soft);
  color: var(--color-success);
  font-size: 21px;
}

.form-heading p,
.form-heading h3 {
  margin: 0;
}

.form-heading p {
  color: var(--color-text-muted);
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 10px;
}

.form-heading h3 {
  margin-top: 3px;
  font-size: 18px;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 18px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 130px;
  gap: 12px;
}

.ttl-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ttl-presets button {
  padding: 6px 9px;
  border: 1px solid var(--color-border-soft);
  border-radius: 7px;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 11px;
}

.ttl-presets button:hover {
  border-color: var(--color-success);
  color: var(--color-success);
}

@media (max-width: 520px) {
  .form-row { grid-template-columns: 1fr; }
}
</style>
