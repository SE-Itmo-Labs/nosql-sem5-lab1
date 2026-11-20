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
  actionsBusy: boolean
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
  <section aria-labelledby="notification-history-title">
    <div class="tw:mt-6 tw:mb-3 tw:flex tw:flex-wrap tw:items-baseline tw:justify-between tw:gap-2">
      <h3 id="notification-history-title" class="tw:m-0 tw:text-xl tw:font-semibold">История</h3>
      <span v-if="!loading && !error" class="tw:text-xs tw:text-(--color-text-secondary)">Найдено: {{ page.totalElements }}</span>
    </div>

    <StatePanel
      v-if="loading"
      kind="loading"
      icon=""
      title="Загружаем уведомления"
    />

    <StatePanel
      v-else-if="error"
      kind="error"
      icon=""
      title="Не удалось загрузить уведомления"
      :description="error"
    >
      <template #actions>
        <button class="btn btn-secondary" type="button" @click="emit('retry')">Повторить</button>
      </template>
    </StatePanel>

    <StatePanel
      v-else-if="page.empty"
      kind="empty"
      icon=""
      title="Уведомлений не найдено"
      description="Попробуйте изменить фильтры."
    />

    <div v-else class="tw:flex tw:flex-col tw:gap-2">
      <NotificationCard
        v-for="notification in page.content"
        :key="notification.id"
        :notification="notification"
        :category-name="categoryNames.get(notification.categoryId) ?? `Категория #${notification.categoryId}`"
        :busy="actionsBusy || busyId === notification.id"
        :allow-actions="allowActions"
        @read="emit('read', notification)"
        @delete="emit('delete', notification)"
      />
    </div>

    <nav v-if="!loading && !error && page.totalPages > 1" class="tw:mt-5 tw:flex tw:flex-wrap tw:items-center tw:justify-center tw:gap-2 tw:min-[431px]:gap-4" aria-label="Страницы уведомлений">
      <button class="btn btn-secondary" type="button" :disabled="page.page === 0" @click="emit('page', page.page - 1)">
        Назад
      </button>
      <span class="tw:text-xs tw:text-(--color-text-secondary)">Страница {{ page.page + 1 }} из {{ page.totalPages }}</span>
      <button class="btn btn-secondary" type="button" :disabled="page.last" @click="emit('page', page.page + 1)">
        Далее
      </button>
    </nav>
  </section>
</template>
