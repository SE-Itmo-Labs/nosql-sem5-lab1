<script setup lang="ts">
import StatePanel from '~/components/ui/StatePanel.vue'
import type { Category } from '~/types/api'

defineOptions({ name: 'CategoryDirectoryList' })

defineProps<{
  categories: Category[]
  loading: boolean
  error: string
  busyId: number | null
  inspectedId: number | null
}>()

const emit = defineEmits<{
  inspect: [category: Category]
  edit: [category: Category]
  delete: [category: Category]
  retry: []
}>()
</script>

<template>
  <section class="directory" aria-label="Справочник категорий">
    <div class="directory-heading">
      <div>
        <p>Постоянный источник</p>
        <h3>Категории уведомлений</h3>
      </div>
      <span v-if="!loading && !error">{{ categories.length }} записей</span>
    </div>

    <StatePanel
      v-if="loading"
      kind="loading"
      icon="⋯"
      title="Загружаем справочник"
      description="Получаем категории из постоянного источника данных."
    />

    <StatePanel
      v-else-if="error"
      kind="error"
      icon="!"
      title="Справочник недоступен"
      :description="error"
    >
      <template #actions>
        <button class="btn btn-secondary" type="button" @click="emit('retry')">Повторить</button>
      </template>
    </StatePanel>

    <StatePanel
      v-else-if="categories.length === 0"
      kind="empty"
      icon="○"
      title="Категорий пока нет"
      description="Создайте первую запись через форму справа."
    />

    <div v-else class="category-grid">
      <article
        v-for="category in categories"
        :key="category.id"
        class="category-card"
        :class="{ 'category-card--inspected': inspectedId === category.id }"
      >
        <div class="category-id">#{{ category.id }}</div>
        <div class="category-copy">
          <h4>{{ category.name }}</h4>
          <p>{{ category.description }}</p>
        </div>
        <div class="category-actions">
          <button
            class="category-action category-action--cache"
            type="button"
            :disabled="busyId === category.id"
            @click="emit('inspect', category)"
          >
            Проверить кэш
          </button>
          <button class="category-action" type="button" @click="emit('edit', category)">Изменить</button>
          <button class="category-action category-action--danger" type="button" @click="emit('delete', category)">
            Удалить
          </button>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.directory-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.directory-heading p,
.directory-heading h3 {
  margin: 0;
}

.directory-heading p {
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.directory-heading h3 {
  margin-top: 4px;
  font-size: 20px;
}

.directory-heading > span {
  color: var(--color-text-muted);
  font-size: 12px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.category-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  padding: 18px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.category-card--inspected {
  border-color: #93c5fd;
  box-shadow: 0 0 0 3px var(--color-info-soft);
}

.category-id {
  color: var(--color-info);
  font-size: 11px;
  font-weight: 800;
}

.category-copy {
  flex: 1;
}

.category-copy h4,
.category-copy p {
  margin: 0;
}

.category-copy h4 {
  margin-top: 8px;
  font-size: 16px;
}

.category-copy p {
  margin-top: 6px;
  color: var(--color-text-secondary);
  font-size: 12px;
  line-height: 1.55;
}

.category-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-soft);
}

.category-action {
  padding: 6px 8px;
  border: 0;
  border-radius: 6px;
  background: var(--color-bg-subtle);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 10px;
  font-weight: 700;
}

.category-action--cache {
  background: var(--color-info-soft);
  color: var(--color-info);
}

.category-action--danger:hover:not(:disabled) { color: var(--color-error); }
.category-action:disabled { cursor: wait; opacity: 0.5; }

@media (max-width: 680px) {
  .category-grid { grid-template-columns: 1fr; }
}
</style>
