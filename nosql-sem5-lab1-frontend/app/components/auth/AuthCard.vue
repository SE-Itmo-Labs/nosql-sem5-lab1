<script setup lang="ts">
defineProps<{ eyebrow: string, title: string, description: string }>()
</script>

<template>
  <section class="auth-card">
    <header class="auth-heading">
      <p class="auth-eyebrow">{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </header>
    <slot />
    <footer class="auth-switch"><slot name="footer" /></footer>
  </section>
</template>

<style scoped>

.auth-card {
  width: min(100%, 430px);
  padding: 36px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-md);
}

.auth-heading {
  margin-bottom: 20px;
}

.auth-eyebrow,
.auth-heading h2,
.auth-heading p {
  margin: 0;
}

.auth-eyebrow {
  color: var(--color-primary-hover);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.auth-heading h2 {
  margin-top: 8px;
  font-size: 28px;
  letter-spacing: -0.02em;
}

.auth-heading p:last-child {
  margin-top: 8px;
  color: var(--color-text-secondary);
  font-size: 14px;
}


.auth-switch {
  margin-top: 24px;
  color: var(--color-text-secondary);
  font-size: 14px;
  text-align: center;
}

.auth-switch :deep(a) {
  color: var(--color-primary-hover);
  font-weight: 650;
}

@media (max-width: 480px) {
  .auth-card { padding: 24px; }
}
</style>
