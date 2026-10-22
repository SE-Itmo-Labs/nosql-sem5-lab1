<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  description: string
  eyebrow?: string
}>(), {
  eyebrow: 'Личный кабинет',
})
</script>

<template>
  <header class="page-heading">
    <div class="page-heading-copy">
      <p class="page-eyebrow">{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
      <p class="page-description">{{ description }}</p>
    </div>

    <!-- Slot не резервирует место, если странице пока не нужны действия. -->
    <div v-if="$slots.actions" class="page-actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped>
.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 26px;
}

.page-heading-copy {
  max-width: 760px;
}

.page-eyebrow,
.page-heading h2,
.page-description {
  margin: 0;
}

.page-eyebrow {
  margin-bottom: 8px;
  color: var(--color-primary-hover);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.page-heading h2 {
  font-size: clamp(27px, 3vw, 38px);
  letter-spacing: -0.025em;
}

.page-description {
  margin-top: 10px;
  color: var(--color-text-secondary);
  font-size: 15px;
  line-height: 1.65;
}

.page-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 10px;
}

@media (max-width: 680px) {
  .page-heading {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
