<script setup lang="ts">
import type { Category, CreateNotificationRequest } from '~/types/api'

defineOptions({ name: 'NotificationComposerForm' })

const props = defineProps<{
  categories: Category[]
  defaultRecipient: string
  submitting: boolean
}>()

const emit = defineEmits<{
  submit: [draft: CreateNotificationRequest]
}>()

const form = reactive({
  title: '',
  text: '',
  categoryId: '' as number | '',
  userId: props.defaultRecipient,
})
const validationError = ref('')

/** Проверяем обязательные поля до API, чтобы пользователь сразу видел причину. */
const submitForm = () => {
  validationError.value = ''
  const title = form.title.trim()
  const text = form.text.trim()

  const userId = form.userId.trim()
  if (!title || !text || !userId || form.categoryId === '') {
    validationError.value = 'Заполните получателя, заголовок, текст и категорию'
    return
  }

  emit('submit', { userId, title, text, categoryId: Number(form.categoryId) })
}

</script>

<template>
  <aside id="notification-composer" class="card tw:min-w-0 tw:scroll-mt-6 tw:min-[981px]:sticky tw:min-[981px]:top-6">
    <h3 class="tw:m-0 tw:text-lg tw:font-semibold">Новое уведомление</h3>

    <form class="tw:mt-4 tw:flex tw:flex-col tw:gap-3" @submit.prevent="submitForm">
      <label class="field">
        <span class="field-label">Получатель</span>
        <input v-model="form.userId" class="field-input" type="text" autocomplete="off" required>
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
          required
        >
      </label>

      <label class="field">
        <span class="field-label">Текст</span>
        <textarea
          v-model="form.text"
          class="field-input tw:min-h-28! tw:resize-y"
          maxlength="500"
          required
        />
      </label>

      <p v-if="validationError" class="form-error" role="alert">{{ validationError }}</p>

      <button class="btn btn-primary btn-block" type="submit" :disabled="submitting || !categories.length">
        {{ submitting ? 'Отправляем…' : 'Отправить уведомление' }}
      </button>
    </form>
  </aside>
</template>
