<script setup lang="ts">
defineOptions({ name: 'AppErrorPage' })

interface AppError {
  statusCode?: number
  statusMessage?: string
  message?: string
}

const props = defineProps<{
  error: AppError
}>()

const title = computed(() => props.error.statusCode === 404
  ? 'Страница не найдена'
  : 'Не удалось открыть страницу')

/** clearError сбрасывает состояние Nuxt до перехода на безопасный маршрут. */
const returnHome = () => clearError({ redirect: '/' })
</script>

<template>
  <main class="error-page">
    <section class="error-card">
      <span class="error-code">{{ error.statusCode ?? 500 }}</span>
      <h1>{{ title }}</h1>
      <p>
        {{ error.statusMessage || error.message || 'Произошла непредвиденная ошибка.' }}
      </p>
      <button class="btn btn-primary" type="button" @click="returnHome">
        Вернуться на главную
      </button>
    </section>
  </main>
</template>

<style scoped>
.error-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  background: var(--color-bg-page);
}

.error-card {
  width: min(100%, 520px);
  padding: 42px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-md);
  text-align: center;
}

.error-code {
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.1em;
}

.error-card h1 {
  margin: 12px 0 0;
  font-size: 28px;
}

.error-card p {
  margin: 12px 0 24px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}
</style>
