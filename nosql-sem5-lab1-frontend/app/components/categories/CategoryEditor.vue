<script setup lang="ts">
import type { Category, SaveCategoryRequest } from '~/types/api'

defineOptions({ name: 'CategoryEditorForm' })

const props = defineProps<{
  category: Category | null
  saving: boolean
}>()

const emit = defineEmits<{
  save: [request: SaveCategoryRequest]
  cancel: []
}>()

const form = reactive({ name: '', description: '' })
const validationError = ref('')

/** При выборе карточки форма получает отдельную копию и не меняет список до PUT. */
watch(() => props.category, (category) => {
  form.name = category?.name ?? ''
  form.description = category?.description ?? ''
  validationError.value = ''
}, { immediate: true })

const submitForm = () => {
  const name = form.name.trim()
  const description = form.description.trim()
  validationError.value = ''

  if (!name || !description) {
    validationError.value = 'Заполните название и описание категории'
    return
  }

  emit('save', { name, description })
}
</script>

<template>
  <aside class="category-editor card">
    <div class="editor-heading">
      <span aria-hidden="true">{{ category ? '✎' : '+' }}</span>
      <div>
        <p>{{ category ? `Категория #${category.id}` : 'Новая запись' }}</p>
        <h3>{{ category ? 'Изменить категорию' : 'Добавить категорию' }}</h3>
      </div>
    </div>

    <form class="editor-form" @submit.prevent="submitForm">
      <label class="field">
        <span class="field-label">Название</span>
        <input v-model="form.name" class="field-input" maxlength="80" placeholder="Например, Оплата" required>
      </label>

      <label class="field">
        <span class="field-label">Описание</span>
        <textarea
          v-model="form.description"
          class="field-input editor-textarea"
          maxlength="240"
          placeholder="Какие уведомления входят в категорию"
          required
        />
      </label>

      <p v-if="validationError" class="form-error" role="alert">{{ validationError }}</p>

      <div class="editor-actions">
        <button v-if="category" class="btn btn-secondary" type="button" :disabled="saving" @click="emit('cancel')">
          Отмена
        </button>
        <button class="btn btn-primary" type="submit" :disabled="saving">
          {{ saving ? 'Сохраняем…' : category ? 'Сохранить' : 'Создать' }}
        </button>
      </div>
    </form>

    <p class="editor-note">
      После изменения backend должен удалить старое значение из Redis, чтобы следующий запрос снова пришёл из DATABASE.
    </p>
  </aside>
</template>

<style scoped>
.category-editor {
  position: sticky;
  top: 24px;
  padding: 22px;
}

.editor-heading {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 17px;
  border-bottom: 1px solid var(--color-border-soft);
}

.editor-heading > span {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 12px;
  background: var(--color-info-soft);
  color: var(--color-info);
  font-size: 19px;
  font-weight: 750;
}

.editor-heading p,
.editor-heading h3 {
  margin: 0;
}

.editor-heading p {
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.editor-heading h3 {
  margin-top: 3px;
  font-size: 18px;
}

.editor-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 18px;
}

.editor-textarea {
  min-height: 112px;
  resize: vertical;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.editor-note {
  margin: 16px 0 0;
  color: var(--color-text-muted);
  font-size: 11px;
  line-height: 1.5;
}

@media (max-width: 960px) {
  .category-editor { position: static; }
}
</style>
