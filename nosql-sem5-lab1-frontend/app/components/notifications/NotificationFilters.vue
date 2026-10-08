<script setup lang="ts">
import type { Category, NotificationStatus } from '~/types/api'

defineOptions({ name: 'NotificationFiltersForm' })

const props = defineProps<{
  categories: Category[]
  search: string
  categoryId: number | null
  status: NotificationStatus | null
  loading: boolean
}>()

const emit = defineEmits<{
  'apply': []
  'reset': []
  'update:search': [value: string]
  'update:categoryId': [value: number | null]
  'update:status': [value: NotificationStatus | null]
}>()

const categoryModel = computed({
  get: () => props.categoryId?.toString() ?? '',
  set: value => emit('update:categoryId', value ? Number(value) : null),
})

const statusModel = computed({
  get: () => props.status ?? '',
  set: value => emit('update:status', value ? value as NotificationStatus : null),
})
</script>

<template>
  <form class="filters card" @submit.prevent="emit('apply')">
    <label class="field filters-search">
      <span class="field-label">Поиск</span>
      <input
        class="field-input"
        type="search"
        placeholder="Заголовок или текст"
        :value="search"
        @input="emit('update:search', ($event.target as HTMLInputElement).value)"
      >
    </label>

    <label class="field">
      <span class="field-label">Категория</span>
      <select v-model="categoryModel" class="field-input">
        <option value="">Все категории</option>
        <option v-for="category in categories" :key="category.id" :value="category.id">
          {{ category.name }}
        </option>
      </select>
    </label>

    <label class="field">
      <span class="field-label">Статус</span>
      <select v-model="statusModel" class="field-input">
        <option value="">Все статусы</option>
        <option value="SENT">Отправлено</option>
        <option value="READ">Прочитано</option>
      </select>
    </label>

    <div class="filter-actions">
      <button class="btn btn-secondary" type="button" :disabled="loading" @click="emit('reset')">
        Сбросить
      </button>
      <button class="btn btn-primary" type="submit" :disabled="loading">
        {{ loading ? 'Загрузка…' : 'Применить' }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.filters {
  display: grid;
  grid-template-columns: minmax(210px, 1.5fr) repeat(2, minmax(150px, 0.8fr)) auto;
  align-items: end;
  gap: 14px;
  margin-bottom: 14px;
  padding: 18px;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 940px) {
  .filters { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .filters-search { grid-column: 1 / -1; }
  .filter-actions { justify-content: flex-end; }
}

@media (max-width: 560px) {
  .filters { grid-template-columns: 1fr; }
  .filters-search { grid-column: auto; }
  .filter-actions { justify-content: stretch; }
  .filter-actions .btn { flex: 1; }
}
</style>
