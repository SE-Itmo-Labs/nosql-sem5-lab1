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
  goToPage,
  isLoading,
  isSubmitting,
  loadError,
  loadNotifications,
  markAsRead,
  pageData,
  resetFilters,
  stats,
  statsError,
} = useNotifications()
</script>

<template>
  <div>
    <PageHeader
      eyebrow=""
      title="Уведомления"
    >
      <template v-if="canSendNotifications" #actions>
        <a class="btn btn-primary" href="#notification-composer">Новое уведомление</a>
      </template>
    </PageHeader>

    <NotificationStats v-if="canSendNotifications && stats" :stats="stats" />
    <p v-if="statsError" class="form-error tw:mb-4!" role="status">Сводка недоступна: {{ statsError }}</p>

    <p
      v-if="feedback"
      class="tw:mt-0 tw:mb-4 tw:rounded-(--radius-sm) tw:bg-(--color-success-soft) tw:px-3 tw:py-2.5 tw:text-[13px] tw:text-(--color-success)"
      role="status"
    >
      {{ feedback }}
    </p>
    <p v-if="actionError" class="form-error tw:mb-4!" role="alert">{{ actionError }}</p>

    <div class="tw:grid tw:grid-cols-1 tw:items-start tw:gap-5" :class="{ 'tw:min-[981px]:grid-cols-[minmax(0,1fr)_330px]': canSendNotifications }">
      <div class="tw:min-w-0">
        <NotificationFilters
          :categories="categories"
          @apply="applyFilters"
          @reset="resetFilters"
        />

        <NotificationList
          :page="pageData"
          :categories="categories"
          :loading="isLoading"
          :error="loadError"
          :busy-id="busyNotificationId"
          :actions-busy="busyNotificationId !== null"
          :allow-actions="canManageHistory"
          @retry="loadNotifications()"
          @page="goToPage"
          @read="markAsRead"
          @delete="deleteNotification"
        />
      </div>

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
