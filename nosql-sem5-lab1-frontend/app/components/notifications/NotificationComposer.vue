<script setup lang="ts">
import type { Category, CreateNotificationRequest } from '~/types/api'

defineOptions({ name: 'NotificationComposerForm' })

defineProps<{
  categories: Category[]
  username: string
  submitting: boolean
}>()

const emit = defineEmits<{
  submit: [draft: Omit<CreateNotificationRequest, 'userId'>]
}>()

const form = reactive({
  title: '',
  text: '',
  categoryId: '' as number | '',
})
const validationError = ref('')

/** Проверяем обязательные поля до API, чтобы пользователь сразу видел причину. */
const submitForm = () => {
  validationError.value = ''
  const title = form.title.trim()
  const text = form.text.trim()

  if (!title || !text || form.categoryId === '') {
    validationError.value = 'Заполните заголовок, текст и категорию'
    return
  }

  emit('submit', { title, text, categoryId: Number(form.categoryId) })
}
</script>

<template>
  <aside id="notification-composer" class="composer card">
    <div class="composer-heading">
      <span aria-hidden="true">✦</span>
      <div>
        <p>Новое уведомление</p>
        <h3>Отправить клиенту</h3>
      </div>
    </div>

    <form class="composer-form" @submit.prevent="submitForm">
      <label class="field">
        <span class="field-label">Получатель</span>
        <input class="field-input" type="text" :value="username" disabled>
      </label>

      <label class="field">
        <span class="field-label">Категория</span>
        <select v-model="form.categoryId" class="field-input" required>
          <option value="" disabled>Выберите категорию</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </label>

      <label class="field">
        <span class="field-label">Заголовок</span>
        <input
          v-model="form.title"
          class="field-input"
          type="text"
          maxlength="120"
          placeholder="Например, бронь подтверждена"
          required
        >
      </label>

      <label class="field">
        <span class="field-label">Текст</span>
        <textarea
          v-model="form.text"
          class="field-input composer-textarea"
          maxlength="500"
          placeholder="Сообщение для клиента"
          required
        />
      </label>

      <p v-if="validationError" class="form-error" role="alert">{{ validationError }}</p>

      <button class="btn btn-primary btn-block" type="submit" :disabled="submitting || !categories.length">
        {{ submitting ? 'Отправляем…' : 'Отправить уведомление' }}
      </button>
    </form>

    <p class="composer-note">
      В mock-режиме запись сохраняется локально. В real-режиме форма вызывает тот же REST-контракт.
    </p>
  </aside>
</template>

<style scoped>
.composer {
  position: sticky;
  top: 24px;
  padding: 22px;
}

.composer-heading {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--color-border-soft);
}

.composer-heading > span {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 12px;
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
  font-size: 18px;
}

.composer-heading p,
.composer-heading h3 {
  margin: 0;
}

.composer-heading p {
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.composer-heading h3 {
  margin-top: 3px;
  font-size: 18px;
}

.composer-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 18px;
}

.composer-textarea {
  min-height: 116px;
  resize: vertical;
}

.composer-note {
  margin: 16px 0 0;
  color: var(--color-text-muted);
  font-size: 11px;
  line-height: 1.5;
}

@media (max-width: 980px) {
  .composer { position: static; }
}
</style>
