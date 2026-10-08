<script setup lang="ts">
import CacheInspector from '~/components/categories/CacheInspector.vue'
import CategoryEditor from '~/components/categories/CategoryEditor.vue'
import CategoryList from '~/components/categories/CategoryList.vue'
import PageHeader from '~/components/ui/PageHeader.vue'

defineOptions({ name: 'CategoriesPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Категории' })

const {
  actionError,
  busyCategoryId,
  categories,
  deleteCategory,
  editorCategory,
  editorVersion,
  feedback,
  inspectCategory,
  inspected,
  isInspecting,
  isLoading,
  isSaving,
  loadCategories,
  loadError,
  saveCategory,
  startCreate,
  startEdit,
  visibleCacheTtl,
} = useCategories()
</script>

<template>
  <div>
    <PageHeader
      eyebrow="Cache-aside"
      title="Справочник категорий"
      description="Проверьте источник данных конкретной категории: первый запрос заполняет Redis, повторный демонстрирует cache hit и оставшийся TTL."
    >
      <template #actions>
        <button class="btn btn-primary" type="button" @click="startCreate">Новая категория</button>
      </template>
    </PageHeader>

    <CacheInspector :result="inspected" :remaining-ttl="visibleCacheTtl" :loading="isInspecting" />

    <p v-if="feedback" class="page-message page-message--success" role="status">{{ feedback }}</p>
    <p v-if="actionError" class="page-message page-message--error" role="alert">{{ actionError }}</p>

    <div class="categories-layout">
      <CategoryList
        :categories="categories"
        :loading="isLoading"
        :error="loadError"
        :busy-id="busyCategoryId"
        :inspected-id="inspected?.category.id ?? null"
        @inspect="inspectCategory"
        @edit="startEdit"
        @delete="deleteCategory"
        @retry="loadCategories"
      />

      <CategoryEditor
        :key="editorVersion"
        :category="editorCategory"
        :saving="isSaving"
        @save="saveCategory"
        @cancel="startCreate"
      />
    </div>
  </div>
</template>

<style scoped>
.categories-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 330px;
  align-items: start;
  gap: 18px;
}

.page-message {
  margin: 0 0 14px;
  padding: 11px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.page-message--success { background: var(--color-success-soft); color: var(--color-success); }
.page-message--error { background: var(--color-error-bg); color: var(--color-error); }

@media (max-width: 960px) {
  .categories-layout { grid-template-columns: 1fr; }
}
</style>
