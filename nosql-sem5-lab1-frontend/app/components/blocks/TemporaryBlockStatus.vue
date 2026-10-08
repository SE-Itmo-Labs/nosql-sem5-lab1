<script setup lang="ts">
import StatePanel from '~/components/ui/StatePanel.vue'
import type { TemporaryBlockViewStatus } from '~/composables/useTemporaryBlocks'
import type { TemporaryBlock } from '~/types/api'

defineOptions({ name: 'TemporaryBlockStatusPanel' })

const props = defineProps<{
  block: TemporaryBlock | null
  status: TemporaryBlockViewStatus
  remainingTtl: number
  checking: boolean
  releasing: boolean
}>()

const emit = defineEmits<{
  release: []
  recheck: []
}>()

const progress = computed(() => {
  if (!props.block?.ttlSeconds) return 0
  return Math.min(100, Math.max(0, (props.remainingTtl / props.block.ttlSeconds) * 100))
})

const dateFormatter = new Intl.DateTimeFormat('ru-RU', {
  dateStyle: 'medium',
  timeStyle: 'medium',
})
</script>

<template>
  <section class="status-card card">
    <div class="status-heading">
      <div>
        <p>Состояние Redis-ключа</p>
        <h3>Контроль TTL</h3>
      </div>
      <span class="status-badge" :class="`status-badge--${status}`">{{ status }}</span>
    </div>

    <div v-if="status === 'active' && block" class="active-block">
      <div class="countdown">
        <strong>{{ remainingTtl }}</strong>
        <span>секунд осталось</span>
      </div>

      <div class="progress-track" aria-label="Оставшееся время">
        <span :style="{ width: `${progress}%` }" />
      </div>

      <dl class="block-details">
        <div><dt>Ресурс</dt><dd>{{ block.resourceKey }}</dd></div>
        <div><dt>Владелец</dt><dd>{{ block.owner }}</dd></div>
        <div><dt>Создан</dt><dd>{{ dateFormatter.format(new Date(block.createdAt)) }}</dd></div>
        <div><dt>Истекает</dt><dd>{{ dateFormatter.format(new Date(block.expiresAt)) }}</dd></div>
      </dl>

      <button class="btn btn-secondary btn-block" type="button" :disabled="releasing" @click="emit('release')">
        {{ releasing ? 'Освобождаем…' : 'Освободить досрочно' }}
      </button>
    </div>

    <StatePanel
      v-else-if="checking"
      kind="loading"
      icon="⋯"
      title="Проверяем Redis-ключ"
      description="Ожидаем ответ endpoint проверки временной блокировки."
    />

    <StatePanel
      v-else-if="status === 'expired'"
      kind="info"
      icon="✓"
      title="TTL истёк"
      description="Ключ больше не существует: Redis удалил временную блокировку автоматически."
    >
      <template #actions>
        <button class="btn btn-secondary" type="button" @click="emit('recheck')">Проверить ещё раз</button>
      </template>
    </StatePanel>

    <StatePanel
      v-else-if="status === 'released'"
      kind="info"
      icon="↗"
      title="Блокировка снята"
      description="Ключ удалён досрочно через DELETE, не дожидаясь окончания TTL."
    />

    <StatePanel
      v-else-if="status === 'missing'"
      kind="empty"
      icon="○"
      title="Активного ключа нет"
      description="Ресурс свободен или его временная блокировка уже истекла."
    />

    <StatePanel
      v-else
      kind="empty"
      icon="◷"
      title="Создайте временную бронь"
      description="После создания здесь появятся оставшийся TTL и точное время автоматического удаления."
    />
  </section>
</template>

<style scoped>
.status-card {
  padding: 22px;
}

.status-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 17px;
  border-bottom: 1px solid var(--color-border-soft);
}

.status-heading p,
.status-heading h3 {
  margin: 0;
}

.status-heading p {
  color: var(--color-text-muted);
  font-size: 10px;
  font-weight: 750;
  text-transform: uppercase;
}

.status-heading h3 {
  margin-top: 3px;
  font-size: 18px;
}

.status-badge {
  padding: 5px 8px;
  border-radius: 999px;
  background: var(--color-bg-subtle);
  color: var(--color-text-secondary);
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 10px;
  font-weight: 800;
  text-transform: uppercase;
}

.status-badge--active { background: var(--color-success-soft); color: var(--color-success); }
.status-badge--expired { background: var(--color-warning-soft); color: var(--color-warning); }
.status-badge--released { background: var(--color-info-soft); color: var(--color-info); }

.active-block {
  margin-top: 22px;
}

.countdown {
  text-align: center;
}

.countdown strong,
.countdown span {
  display: block;
}

.countdown strong {
  color: var(--color-success);
  font-size: 58px;
  letter-spacing: -0.06em;
  line-height: 1;
}

.countdown span {
  margin-top: 6px;
  color: var(--color-text-muted);
  font-size: 11px;
}

.progress-track {
  height: 7px;
  overflow: hidden;
  margin-top: 18px;
  border-radius: 999px;
  background: var(--color-bg-subtle);
}

.progress-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--color-success);
  transition: width 0.25s linear;
}

.block-details {
  display: grid;
  gap: 0;
  margin: 20px 0;
}

.block-details div {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 10px;
  padding: 9px 0;
  border-bottom: 1px solid var(--color-border-soft);
}

.block-details dt {
  color: var(--color-text-muted);
  font-size: 11px;
}

.block-details dd {
  overflow-wrap: anywhere;
  margin: 0;
  color: var(--color-text);
  font-size: 11px;
  text-align: right;
}

.status-card :deep(.state-panel) {
  min-height: 320px;
  margin-top: 18px;
}
</style>
