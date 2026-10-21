<script setup lang="ts">
import type { Category } from '~/types/api'

defineOptions({ name: 'NotificationFiltersForm' })

const props = defineProps<{
  categories: Category[]
  search: string
  categoryId: number | null
  showCategories: boolean
}>()

const emit = defineEmits<{
  'reset': []
  'update:search': [value: string]
  'update:categoryId': [value: number | null]
}>()

const categoryModel = computed({
  get: () => props.categoryId?.toString() ?? '',
  set: value => emit('update:categoryId', value ? Number(value) : null),
})

</script>

<template>
  <div class="filters card" :class="{ 'filters--admin': showCategories }">
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

    <label v-if="showCategories" class="field">
      <span class="field-label">Категория</span>
      <select v-model="categoryModel" class="field-input">
        <option value="">Все категории</option>
        <option v-for="category in categories" :key="category.id" :value="category.id">
          {{ category.name }}
        </option>
      </select>
    </label>

    <div class="filter-actions">
      <button class="btn btn-secondary" type="button" @click="emit('reset')">
        Сбросить
      </button>
    </div>
  </div>
</template>

<style scoped>
.filters {
  display: grid;
  grid-template-columns: minmax(180px, 1fr) auto;
  align-items: end;
  gap: 14px;
  margin-bottom: 14px;
  padding: 18px;
}

.filters--admin {
  grid-template-columns: minmax(180px, 1fr) minmax(140px, 0.7fr) auto;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 940px) {
  .filters, .filters--admin { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .filters-search { grid-column: 1 / -1; }
  .filter-actions { justify-content: flex-end; }
}

@media (max-width: 560px) {
  .filters, .filters--admin { grid-template-columns: 1fr; }
  .filters-search { grid-column: auto; }
  .filter-actions { justify-content: stretch; }
  .filter-actions .btn { flex: 1; }
}
</style>
