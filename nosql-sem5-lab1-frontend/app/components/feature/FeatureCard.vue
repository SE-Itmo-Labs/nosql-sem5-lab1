<script setup lang="ts">
import type { NavigationItem } from '~/config/navigation'

defineProps<{
  feature: NavigationItem
}>()
</script>

<template>
  <NuxtLink class="feature-card" :class="`feature-card--${feature.accent}`" :to="feature.to">
    <span class="feature-icon" aria-hidden="true">{{ feature.icon }}</span>
    <span class="feature-copy">
      <strong>{{ feature.label }}</strong>
      <span>{{ feature.description }}</span>
    </span>
    <span class="feature-arrow" aria-hidden="true">→</span>
  </NuxtLink>
</template>

<style scoped>
.feature-card {
  display: grid;
  min-height: 168px;
  grid-template-columns: auto 1fr auto;
  align-items: start;
  gap: 16px;
  padding: 22px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
  text-decoration: none;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.feature-card:hover {
  border-color: #fdba74;
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
}

.feature-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 12px;
  background: var(--color-bg-subtle);
  color: var(--color-text);
  font-size: 20px;
  font-weight: 800;
}

.feature-card--orange .feature-icon { background: var(--color-primary-soft); color: var(--color-primary-hover); }
.feature-card--blue .feature-icon { background: var(--color-info-soft); color: var(--color-info); }
.feature-card--green .feature-icon { background: var(--color-success-soft); color: var(--color-success); }
.feature-card--violet .feature-icon { background: #f5f3ff; color: #7c3aed; }

.feature-copy {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-copy strong {
  color: var(--color-text);
  font-size: 16px;
}

.feature-copy span {
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.55;
}

.feature-arrow {
  color: var(--color-text-muted);
  font-size: 20px;
  transition: transform 0.18s ease;
}

.feature-card:hover .feature-arrow {
  color: var(--color-primary-hover);
  transform: translateX(3px);
}
</style>
