<script setup lang="ts">
import type { Notification } from '~/types/api'

defineOptions({ name: 'NotificationHistoryCard' })

defineProps<{
  notification: Notification
  categoryName: string
  busy: boolean
  allowActions: boolean
}>()

const emit = defineEmits<{
  read: []
  delete: []
}>()

const dateFormatter = new Intl.DateTimeFormat('ru-RU', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

const formatDate = (value: string): string => dateFormatter.format(new Date(value))
</script>

<template>
  <article class="notification-card">
    <div class="notification-marker" :class="`notification-marker--${notification.status.toLowerCase()}`">
      {{ notification.status === 'READ' ? '✓' : '✦' }}
    </div>

    <div class="notification-body">
      <div class="notification-meta">
        <span class="status-pill" :class="`status-pill--${notification.status.toLowerCase()}`">
          {{ notification.status === 'READ' ? 'Прочитано' : 'Отправлено' }}
        </span>
        <span>{{ categoryName }}</span>
        <time :datetime="notification.createdAt">{{ formatDate(notification.createdAt) }}</time>
      </div>
      <h4>{{ notification.title }}</h4>
      <p>{{ notification.text }}</p>
    </div>

    <!-- Изменяющие действия блокируются на время запроса от повторного клика. -->
    <div v-if="allowActions" class="notification-actions">
      <button
        v-if="notification.status !== 'READ'"
        class="notification-action"
        type="button"
        :disabled="busy"
        @click="emit('read')"
      >
        Прочитано
      </button>
      <button
        class="notification-action notification-action--danger"
        type="button"
        :disabled="busy"
        @click="emit('delete')"
      >
        Удалить
      </button>
    </div>
  </article>
</template>

<style scoped>
.notification-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 15px;
  padding: 18px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
}

.notification-marker {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 11px;
  font-weight: 800;
}

.notification-marker--sent {
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
}

.notification-marker--read {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.notification-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 7px 10px;
  color: var(--color-text-muted);
  font-size: 11px;
}

.status-pill {
  padding: 3px 7px;
  border-radius: 999px;
  font-weight: 750;
}

.status-pill--sent {
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
}

.status-pill--read {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.notification-body h4,
.notification-body p {
  margin: 0;
}

.notification-body h4 {
  margin-top: 8px;
  font-size: 16px;
}

.notification-body p {
  margin-top: 5px;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.55;
}

.notification-actions {
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.notification-action {
  padding: 7px 9px;
  border: 1px solid var(--color-border-soft);
  border-radius: 7px;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 11px;
  font-weight: 650;
}

.notification-action:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary-hover);
}

.notification-action--danger:hover:not(:disabled) {
  border-color: var(--color-error);
  color: var(--color-error);
}

.notification-action:disabled { cursor: wait; opacity: 0.5; }

@media (max-width: 680px) {
  .notification-card { grid-template-columns: auto minmax(0, 1fr); }
  .notification-actions { grid-column: 2; }
}

@media (max-width: 430px) {
  .notification-card { grid-template-columns: 1fr; }
  .notification-marker { display: none; }
  .notification-actions { grid-column: auto; }
}
</style>
