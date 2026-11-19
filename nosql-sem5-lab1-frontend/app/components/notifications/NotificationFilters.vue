<script setup lang="ts">
import { DEFAULT_NOTIFICATION_PAGE_SIZE } from '~/composables/useNotifications'
import type {
  Category,
  NotificationActionType,
  NotificationSearchQuery,
  NotificationSort,
  NotificationStatus,
} from '~/types/api'

defineOptions({ name: 'NotificationFiltersForm' })

defineProps<{ categories: Category[] }>()

const emit = defineEmits<{
  apply: [query: NotificationSearchQuery]
  reset: []
}>()

const emptyForm = () => ({
  q: '',
  categoryId: '' as number | '',
  status: '' as NotificationStatus | '',
  tag: '',
  processingRuleId: '' as number | '',
  actionType: '' as NotificationActionType | '',
  actorId: '',
  updatedFrom: '',
  updatedTo: '',
  sort: 'UPDATED_AT_DESC' as NotificationSort,
  size: DEFAULT_NOTIFICATION_PAGE_SIZE,
})

const form = reactive(emptyForm())
const error = ref('')
const extraFiltersActive = computed(() => Boolean(
  form.tag || form.processingRuleId || form.actionType || form.actorId || form.updatedFrom || form.updatedTo,
))

const apply = () => {
  error.value = ''
  const from = form.updatedFrom ? new Date(form.updatedFrom) : undefined
  const to = form.updatedTo ? new Date(form.updatedTo) : undefined

  if ((from && Number.isNaN(from.getTime())) || (to && Number.isNaN(to.getTime()))) {
    error.value = 'Проверьте даты изменения'
    return
  }
  if (from && to && from.getTime() >= to.getTime()) {
    error.value = 'Начало диапазона должно быть раньше окончания'
    return
  }

  emit('apply', {
    q: form.q || undefined,
    categoryId: form.categoryId || undefined,
    status: form.status || undefined,
    tag: form.tag || undefined,
    processingRuleId: form.processingRuleId || undefined,
    actionType: form.actionType || undefined,
    actorId: form.actorId || undefined,
    updatedFrom: from?.toISOString(),
    updatedTo: to?.toISOString(),
    sort: form.sort,
    size: form.size,
  })
}

const reset = () => {
  Object.assign(form, emptyForm())
  error.value = ''
  emit('reset')
}
</script>

<template>
  <form class="filters card" aria-label="Фильтры уведомлений" @submit.prevent="apply" @reset.prevent="reset">
    <div class="filter-grid">
      <label class="field">
        <span class="field-label">Поиск</span>
        <input v-model.trim="form.q" class="field-input" type="search" maxlength="200" placeholder="Заголовок или текст">
      </label>

      <label class="field">
        <span class="field-label">Категория</span>
        <select v-model="form.categoryId" class="field-input">
          <option value="">Все категории</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </label>

      <label class="field">
        <span class="field-label">Статус</span>
        <select v-model="form.status" class="field-input">
          <option value="">Все статусы</option>
          <option value="SENT">Новые</option>
          <option value="READ">Прочитанные</option>
        </select>
      </label>

      <label class="field">
        <span class="field-label">Сортировка</span>
        <select v-model="form.sort" class="field-input">
          <option value="UPDATED_AT_DESC">Сначала недавно изменённые</option>
          <option value="UPDATED_AT_ASC">Сначала давно изменённые</option>
          <option value="CREATED_AT_DESC">Сначала новые по дате создания</option>
          <option value="CREATED_AT_ASC">Сначала старые по дате создания</option>
        </select>
      </label>

      <label class="field">
        <span class="field-label">На странице</span>
        <select v-model="form.size" class="field-input">
          <option v-for="size in [1, 6, 12, 24]" :key="size" :value="size">{{ size }}</option>
        </select>
      </label>
    </div>

    <details class="filter-details">
      <summary>Дополнительные фильтры<span v-if="extraFiltersActive"> · заполнены</span></summary>
      <div class="filter-grid">
        <label class="field">
          <span class="field-label">Метка</span>
          <input v-model.trim="form.tag" class="field-input" type="text" placeholder="booking">
        </label>
        <label class="field">
          <span class="field-label">ID правила</span>
          <input v-model.number="form.processingRuleId" class="field-input" type="number" min="1" step="1" max="9007199254740991">
        </label>
        <label class="field">
          <span class="field-label">Действие в журнале</span>
          <select v-model="form.actionType" class="field-input">
            <option value="">Все действия</option>
            <option value="CREATED">Создано</option>
            <option value="READ">Прочитано</option>
            <option value="RULE_APPLIED">Применено правило</option>
          </select>
        </label>
        <label class="field">
          <span class="field-label">Автор действия</span>
          <input v-model.trim="form.actorId" class="field-input" type="text" placeholder="admin">
        </label>
        <label class="field">
          <span class="field-label">Изменено с</span>
          <input v-model="form.updatedFrom" class="field-input" type="datetime-local">
        </label>
        <label class="field">
          <span class="field-label">Изменено до</span>
          <input v-model="form.updatedTo" class="field-input" type="datetime-local">
        </label>
      </div>
    </details>

    <p v-if="error" class="filter-error" role="alert">{{ error }}</p>

    <div class="filter-actions">
      <button class="btn btn-primary" type="submit">Применить</button>
      <button class="btn btn-secondary" type="reset">Сбросить</button>
    </div>
  </form>
</template>

<style scoped>
.filters {
  display: grid;
  gap: 14px;
  margin-bottom: 14px;
  padding: 18px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 190px), 1fr));
  align-items: end;
  gap: 12px 14px;
}

.filter-grid .field,
.filter-grid .field-input {
  min-width: 0;
}

.filter-details {
  border-top: 1px solid var(--color-border-soft);
  padding-top: 12px;
}

.filter-details summary {
  width: fit-content;
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 650;
  cursor: pointer;
}

.filter-details .filter-grid { margin-top: 12px; }

.filter-actions { display: flex; flex-wrap: wrap; gap: 8px; }

.filter-error { margin: 0; color: var(--color-error); font-size: 13px; }
</style>
