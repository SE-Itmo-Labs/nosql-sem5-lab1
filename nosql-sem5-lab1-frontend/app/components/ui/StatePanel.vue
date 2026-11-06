<script setup lang="ts">
type StateKind = 'empty' | 'loading' | 'error' | 'info'

withDefaults(defineProps<{
  kind?: StateKind
  icon?: string
  title: string
  description: string
}>(), {
  kind: 'info',
  icon: '·',
})
</script>

<template>
  <section
    class="state-panel"
    :class="`state-panel--${kind}`"
    :aria-live="kind === 'loading' || kind === 'error' ? 'polite' : undefined"
  >
    <span class="state-icon" :class="{ 'state-icon--loading': kind === 'loading' }" aria-hidden="true">
      {{ icon }}
    </span>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>

    <div v-if="$slots.actions" class="state-actions">
      <slot name="actions" />
    </div>
  </section>
</template>

<style scoped>
.state-panel {
  display: grid;
  min-height: 280px;
  place-items: center;
  align-content: center;
  padding: 42px 24px;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  text-align: center;
}

.state-icon {
  display: grid;
  width: 52px;
  height: 52px;
  margin-bottom: 18px;
  place-items: center;
  border-radius: 16px;
  background: var(--color-info-soft);
  color: var(--color-info);
  font-size: 25px;
  font-weight: 800;
}

.state-panel--empty .state-icon {
  background: var(--color-bg-subtle);
  color: var(--color-text-secondary);
}

.state-panel--error .state-icon {
  background: var(--color-error-bg);
  color: var(--color-error);
}

.state-panel--loading .state-icon {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.state-icon--loading {
  animation: pulse 1.1s ease-in-out infinite alternate;
}

.state-panel h3,
.state-panel p {
  margin: 0;
}

.state-panel h3 {
  font-size: 18px;
}

.state-panel p {
  max-width: 560px;
  margin-top: 8px;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.state-actions {
  display: flex;
  gap: 10px;
  margin-top: 22px;
}

@keyframes pulse {
  from { transform: scale(0.92); opacity: 0.65; }
  to { transform: scale(1); opacity: 1; }
}
</style>
