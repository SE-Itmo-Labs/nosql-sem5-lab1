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
  <form class="card tw:mb-4 tw:grid tw:gap-4" aria-label="Фильтры уведомлений" @submit.prevent="apply" @reset.prevent="reset">
    <div class="tw:grid tw:grid-cols-[repeat(auto-fit,minmax(min(100%,190px),1fr))] tw:items-end tw:gap-3">
      <label class="field tw:min-w-0">
        <span class="field-label">Поиск</span>
        <input v-model.trim="form.q" class="field-input tw:min-w-0" type="search" maxlength="200" placeholder="Заголовок или текст">
      </label>

      <label class="field tw:min-w-0">
        <span class="field-label">Категория</span>
        <select v-model="form.categoryId" class="field-input tw:min-w-0">
          <option value="">Все категории</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </label>

      <label class="field tw:min-w-0">
        <span class="field-label">Статус</span>
        <select v-model="form.status" class="field-input tw:min-w-0">
          <option value="">Все статусы</option>
          <option value="SENT">Новые</option>
          <option value="READ">Прочитанные</option>
        </select>
      </label>

      <label class="field tw:min-w-0">
        <span class="field-label">Сортировка</span>
        <select v-model="form.sort" class="field-input tw:min-w-0">
          <option value="UPDATED_AT_DESC">Сначала недавно изменённые</option>
          <option value="UPDATED_AT_ASC">Сначала давно изменённые</option>
          <option value="CREATED_AT_DESC">Сначала новые по дате создания</option>
          <option value="CREATED_AT_ASC">Сначала старые по дате создания</option>
        </select>
      </label>

      <label class="field tw:min-w-0">
        <span class="field-label">На странице</span>
        <select v-model="form.size" class="field-input tw:min-w-0">
          <option v-for="size in [1, 6, 12, 24]" :key="size" :value="size">{{ size }}</option>
        </select>
      </label>
    </div>

    <details class="tw:border-0 tw:border-t tw:border-solid tw:border-(--color-border-soft) tw:pt-3">
      <summary class="tw:w-fit tw:cursor-pointer tw:text-sm tw:font-semibold tw:text-(--color-text-secondary)">
        Дополнительные фильтры<span v-if="extraFiltersActive"> (заполнены)</span>
      </summary>
      <div class="tw:mt-3 tw:grid tw:grid-cols-[repeat(auto-fit,minmax(min(100%,190px),1fr))] tw:items-end tw:gap-3">
        <label class="field tw:min-w-0">
          <span class="field-label">Метка</span>
          <input v-model.trim="form.tag" class="field-input tw:min-w-0" type="text" placeholder="booking">
        </label>
        <label class="field tw:min-w-0">
          <span class="field-label">ID правила</span>
          <input v-model.number="form.processingRuleId" class="field-input tw:min-w-0" type="number" min="1" step="1" max="9007199254740991">
        </label>
        <label class="field tw:min-w-0">
          <span class="field-label">Действие в журнале</span>
          <select v-model="form.actionType" class="field-input tw:min-w-0">
            <option value="">Все действия</option>
            <option value="CREATED">Создано</option>
            <option value="READ">Прочитано</option>
            <option value="RULE_APPLIED">Применено правило</option>
          </select>
        </label>
        <label class="field tw:min-w-0">
          <span class="field-label">Автор действия</span>
          <input v-model.trim="form.actorId" class="field-input tw:min-w-0" type="text" placeholder="admin">
        </label>
        <label class="field tw:min-w-0">
          <span class="field-label">Изменено с</span>
          <input v-model="form.updatedFrom" class="field-input tw:min-w-0" type="datetime-local">
        </label>
        <label class="field tw:min-w-0">
          <span class="field-label">Изменено до</span>
          <input v-model="form.updatedTo" class="field-input tw:min-w-0" type="datetime-local">
        </label>
      </div>
    </details>

    <p v-if="error" class="form-error" role="alert">{{ error }}</p>

    <div class="tw:flex tw:flex-wrap tw:gap-2">
      <button class="btn btn-primary" type="submit">Применить</button>
      <button class="btn btn-secondary" type="reset">Сбросить</button>
    </div>
  </form>
</template>
