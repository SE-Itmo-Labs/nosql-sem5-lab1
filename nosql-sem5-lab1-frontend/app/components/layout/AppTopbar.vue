<script setup lang="ts">
const emit = defineEmits<{
  toggleNavigation: []
}>()

const route = useRoute()
const router = useRouter()
const { user, logout } = useAuth()
const isMockMode = useRuntimeConfig().public.apiMode === 'mock'

const pageTitle = computed(() => String(route.meta.title ?? 'Личный кабинет'))

/** Выход очищает сессию до перехода, чтобы auth middleware не увидел старый токен. */
const onLogout = async () => {
  logout()
  await router.push('/login')
}
</script>

<template>
  <header class="topbar">
    <div class="topbar-heading">
      <button
        class="menu-button"
        type="button"
        aria-label="Открыть навигацию"
        @click="emit('toggleNavigation')"
      >
        <span aria-hidden="true">☰</span>
      </button>
      <div>
        <p class="topbar-caption">Личный кабинет клиента</p>
        <h1 class="topbar-title">{{ pageTitle }}</h1>
      </div>
    </div>

    <div class="topbar-actions">
      <!-- Режим данных всегда виден, чтобы mock нельзя было принять за backend. -->
      <span class="mode-badge" :class="{ 'mode-badge--real': !isMockMode }">
        <span class="mode-dot" aria-hidden="true" />
        {{ isMockMode ? 'Mock API' : 'Real API' }}
      </span>

      <div v-if="user" class="user-summary">
        <span class="user-avatar" aria-hidden="true">{{ user.username.slice(0, 1).toUpperCase() }}</span>
        <span class="user-copy">
          <strong>{{ user.username }}</strong>
          <small>клиент</small>
        </span>
      </div>

      <button class="logout-button" type="button" @click="onLogout">
        Выйти
      </button>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  position: sticky;
  z-index: 20;
  top: 0;
  display: flex;
  min-height: 76px;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 12px 28px;
  border-bottom: 1px solid var(--color-border-soft);
  background: rgb(255 255 255 / 92%);
  backdrop-filter: blur(12px);
}

.topbar-heading,
.topbar-actions,
.user-summary {
  display: flex;
  align-items: center;
}

.topbar-heading {
  gap: 12px;
}

.topbar-caption,
.topbar-title {
  margin: 0;
}

.topbar-caption {
  color: var(--color-text-muted);
  font-size: 11px;
}

.topbar-title {
  margin-top: 2px;
  font-size: 19px;
}

.topbar-actions {
  gap: 14px;
}

.menu-button,
.logout-button {
  border: 0;
  cursor: pointer;
}

.menu-button {
  display: none;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  background: var(--color-bg-subtle);
  color: var(--color-text);
  font-size: 20px;
}

.mode-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--color-warning-soft);
  color: var(--color-warning);
  font-size: 12px;
  font-weight: 700;
}

.mode-badge--real {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.mode-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentcolor;
}

.user-summary {
  gap: 9px;
}

.user-avatar {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 50%;
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
  font-weight: 800;
}

.user-copy strong,
.user-copy small {
  display: block;
}

.user-copy strong {
  font-size: 13px;
}

.user-copy small {
  margin-top: 2px;
  color: var(--color-text-muted);
  font-size: 11px;
}

.logout-button {
  padding: 8px 10px;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 650;
}

.logout-button:hover {
  color: var(--color-error);
}

@media (max-width: 900px) {
  .menu-button {
    display: grid;
    place-items: center;
  }
}

@media (max-width: 680px) {
  .topbar {
    padding-inline: 16px;
  }

  .topbar-caption,
  .user-summary {
    display: none;
  }

  .mode-badge {
    padding-inline: 8px;
  }
}

@media (max-width: 460px) {
  .mode-badge {
    display: none;
  }
}
</style>
