<script setup lang="ts">
import NotificationComposer from '~/components/notifications/NotificationComposer.vue'
import NotificationFilters from '~/components/notifications/NotificationFilters.vue'
import NotificationList from '~/components/notifications/NotificationList.vue'
import NotificationStats from '~/components/notifications/NotificationStats.vue'
import PageHeader from '~/components/ui/PageHeader.vue'

defineOptions({ name: 'NotificationsPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Уведомления' })

const {
  actionError,
  applyFilters,
  busyNotificationId,
  canManageHistory,
  canSendNotifications,
  categories,
  composerVersion,
  createNotification,
  deleteNotification,
  feedback,
  filters,
  goToPage,
  isLoading,
  isSubmitting,
  loadError,
  loadNotifications,
  markAsRead,
  pageData,
  resetFilters,
  stats,
} = useNotifications()
</script>

<template>
  <div>
    <PageHeader
      eyebrow="Основной сценарий"
      title="Уведомления"
      description="Получайте системные сообщения кинотеатра и отслеживайте их состояние. Ручная отправка доступна администратору."
    >
      <template v-if="canSendNotifications" #actions>
        <a class="btn btn-primary" href="#notification-composer">Новое уведомление</a>
      </template>
    </PageHeader>

    <NotificationStats :stats="stats" :loading="isLoading" />

    <p v-if="feedback" class="page-message page-message--success" role="status">{{ feedback }}</p>
    <p v-if="actionError" class="page-message page-message--error" role="alert">{{ actionError }}</p>

    <div class="notifications-layout" :class="{ 'notifications-layout--single': !canSendNotifications }">
      <main class="notifications-content">
        <NotificationFilters
          v-model:search="filters.search"
          v-model:category-id="filters.categoryId"
          v-model:status="filters.status"
          :categories="categories"
          :loading="isLoading"
          @apply="applyFilters"
          @reset="resetFilters"
        />

        <NotificationList
          :page="pageData"
          :categories="categories"
          :loading="isLoading"
          :error="loadError"
          :busy-id="busyNotificationId"
          :allow-actions="canManageHistory"
          @retry="loadNotifications"
          @page="goToPage"
          @read="markAsRead"
          @delete="deleteNotification"
        />
      </main>

      <!-- key очищает форму только после подтвержденной отправки. -->
      <NotificationComposer
        v-if="canSendNotifications"
        :key="composerVersion"
        :categories="categories"
        default-recipient="client01"
        :submitting="isSubmitting"
        @submit="createNotification"
      />
    </div>
  </div>
</template>

<style scoped>
.notifications-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 330px;
  align-items: start;
  gap: 18px;
}

.notifications-content {
  min-width: 0;
}

.notifications-layout--single { grid-template-columns: 1fr; }

.page-message {
  margin: 0 0 14px;
  padding: 11px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.page-message--success {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.page-message--error {
  background: var(--color-error-bg);
  color: var(--color-error);
}

@media (max-width: 980px) {
  .notifications-layout { grid-template-columns: 1fr; }
}
</style>
