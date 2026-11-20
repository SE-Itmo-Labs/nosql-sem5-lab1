<script setup lang="ts">
import type { Notification } from '~/types/api'

defineOptions({ name: 'NotificationHistoryCard' })

const props = defineProps<{
  notification: Notification
  categoryName: string
  busy: boolean
  allowActions: boolean
}>()

const emit = defineEmits<{
  read: []
  delete: []
}>()

const card = ref<HTMLElement | null>(null)
const isVisible = ref(false)
let observer: IntersectionObserver | undefined
let readTimer: ReturnType<typeof setTimeout> | undefined
let attempted = false

const cancelRead = () => {
  clearTimeout(readTimer)
  readTimer = undefined
}

const scheduleRead = () => {
  cancelRead()
  if (!isVisible.value || document.visibilityState !== 'visible' || attempted
    || props.busy || !props.allowActions || props.notification.status === 'READ') return

  readTimer = setTimeout(() => {
    attempted = true
    emit('read')
  }, 2000)
}

onMounted(() => {
  if (!card.value) return
  observer = new IntersectionObserver(([entry]) => {
    if (!entry) return
    const visible = entry.isIntersecting && entry.intersectionRect.height
      >= Math.min(entry.boundingClientRect.height, entry.rootBounds?.height ?? window.innerHeight) / 2
    if (visible === isVisible.value) return
    isVisible.value = visible
    if (!visible) attempted = false
    scheduleRead()
  }, { threshold: Array.from({ length: 21 }, (_, index) => index / 20) })
  observer.observe(card.value)
  document.addEventListener('visibilitychange', scheduleRead)
})

watch(() => [props.busy, props.allowActions, props.notification.status], scheduleRead)
onBeforeUnmount(() => {
  cancelRead()
  observer?.disconnect()
  document.removeEventListener('visibilitychange', scheduleRead)
})

const dateFormatter = new Intl.DateTimeFormat('ru-RU', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

const formatDate = (value: string): string => dateFormatter.format(new Date(value))
</script>

<template>
  <article
    ref="card"
    class="card tw:grid tw:min-w-0 tw:gap-3 tw:p-4! tw:shadow-none! tw:min-[681px]:grid-cols-[minmax(0,1fr)_auto]"
  >
    <div class="tw:min-w-0 tw:wrap-anywhere">
      <div class="tw:flex tw:flex-wrap tw:items-center tw:gap-x-3 tw:gap-y-1 tw:text-[11px] tw:text-(--color-text-secondary)">
        <span
          class="tw:rounded-full tw:px-2 tw:py-0.5 tw:font-semibold"
          :class="notification.status === 'READ'
            ? 'tw:bg-(--color-success-soft) tw:text-(--color-success)'
            : 'tw:bg-(--color-bg-subtle) tw:text-(--color-text)'"
        >
          {{ notification.status === 'READ' ? 'Прочитано' : 'Новое' }}
        </span>
        <span>{{ categoryName }}</span>
        <time :datetime="notification.createdAt">{{ formatDate(notification.createdAt) }}</time>
      </div>
      <h4 class="tw:mt-2 tw:mb-0 tw:text-base tw:font-semibold">{{ notification.title }}</h4>
      <p class="tw:mt-1 tw:mb-0 tw:text-[13px] tw:leading-relaxed tw:text-(--color-text-secondary)">{{ notification.text }}</p>
      <div v-if="notification.processingRuleId != null || notification.tags?.length" class="tw:mt-2 tw:flex tw:flex-wrap tw:gap-x-3 tw:gap-y-1 tw:text-[11px] tw:text-(--color-text-secondary)">
        <span v-if="notification.processingRuleId != null">Правило №{{ notification.processingRuleId }}</span>
        <span v-for="tag in notification.tags" :key="tag">{{ tag }}</span>
      </div>
      <time
        v-if="notification.updatedAt && notification.updatedAt !== notification.createdAt"
        class="tw:mt-2 tw:block tw:text-[11px] tw:text-(--color-text-secondary)"
        :datetime="notification.updatedAt"
      >
        Изменено {{ formatDate(notification.updatedAt) }}
      </time>
    </div>

    <!-- Изменяющие действия блокируются на время запроса от повторного клика. -->
    <div v-if="allowActions" class="tw:self-start">
      <button
        class="btn btn-secondary tw:min-h-0! tw:px-2! tw:py-1.5! tw:text-[11px]! tw:enabled:hover:border-(--color-error)! tw:enabled:hover:text-(--color-error)!"
        type="button"
        :disabled="busy"
        @click.stop="emit('delete')"
      >
        Удалить
      </button>
    </div>
  </article>
</template>
