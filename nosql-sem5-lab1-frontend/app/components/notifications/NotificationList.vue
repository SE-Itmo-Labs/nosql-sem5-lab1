<script setup lang="ts">
import NotificationCard from '~/components/notifications/NotificationCard.vue'
import StatePanel from '~/components/ui/StatePanel.vue'
import type { Category, Notification, PageResponse } from '~/types/api'

defineOptions({ name: 'NotificationHistoryList' })

const props = defineProps<{
  page: PageResponse<Notification>
  categories: Category[]
  loading: boolean
  error: string
  busyId: number | null
  allowActions: boolean
}>()

const emit = defineEmits<{
  retry: []
  page: [value: number]
  read: [notification: Notification]
  delete: [notification: Notification]
}>()

const categoryNames = computed(() => new Map(
  props.categories.map(category => [category.id, category.name]),
))
</script>

<template>
  <section class="history" aria-label="История уведомлений">
    <div class="history-heading">
      <div>
        <p>История отправки</p>
        <h3>Уведомления клиента</h3>
      </div>
      <span v-if="!loading && !error">{{ page.totalElements }} записей</span>
    </div>

    <StatePanel
      v-if="loading"
      kind="loading"
      icon="⋯"
      title="Загружаем уведомления"
      description="Получаем список и актуальную статистику через ApiClient."
    />

    <StatePanel
      v-else-if="error"
      kind="error"
      icon="!"
      title="Не удалось загрузить историю"
      :description="error"
    >
      <template #actions>
        <button class="btn btn-secondary" type="button" @click="emit('retry')">Повторить</button>
      </template>
    </StatePanel>

    <StatePanel
      v-else-if="page.empty"
      kind="empty"
      icon="○"
      title="Уведомлений не найдено"
      description="Измените фильтры или отправьте первое уведомление через форму."
    />

    <div v-else class="notification-list">
      <NotificationCard
        v-for="notification in page.content"
        :key="notification.id"
        :notification="notification"
        :category-name="categoryNames.get(notification.categoryId) ?? `Категория #${notification.categoryId}`"
        :busy="busyId === notification.id"
        :allow-actions="allowActions"
        @read="emit('read', notification)"
        @delete="emit('delete', notification)"
      />
    </div>

    <nav v-if="!loading && !error && page.totalPages > 1" class="pagination" aria-label="Страницы уведомлений">
      <button class="btn btn-secondary" type="button" :disabled="page.page === 0" @click="emit('page', page.page - 1)">
        Назад
      </button>
      <span>Страница {{ page.page + 1 }} из {{ page.totalPages }}</span>
      <button class="btn btn-secondary" type="button" :disabled="page.last" @click="emit('page', page.page + 1)">
        Далее
      </button>
    </nav>
  </section>
</template>

<style scoped>
.history-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin: 24px 0 14px;
}

.history-heading p,
.history-heading h3 {
  margin: 0;
}

.history-heading p {
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.history-heading h3 {
  margin-top: 4px;
  font-size: 20px;
}

.history-heading > span {
  color: var(--color-text-muted);
  font-size: 12px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 18px;
}

.pagination span {
  color: var(--color-text-secondary);
  font-size: 12px;
}

@media (max-width: 430px) {
  .pagination { gap: 8px; }
}
</style>
