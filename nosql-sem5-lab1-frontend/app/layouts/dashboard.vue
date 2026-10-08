<script setup lang="ts">
import AppSidebar from '~/components/layout/AppSidebar.vue'
import AppTopbar from '~/components/layout/AppTopbar.vue'

defineOptions({ name: 'DashboardLayout' })

const sidebarOpen = ref(false)
const route = useRoute()

/** Любая навигация закрывает мобильное меню и возвращает контент пользователю. */
watch(() => route.path, () => {
  sidebarOpen.value = false
})
</script>

<template>
  <div class="dashboard-layout">
    <AppSidebar :open="sidebarOpen" @close="sidebarOpen = false" />

    <button
      v-if="sidebarOpen"
      class="navigation-backdrop"
      type="button"
      aria-label="Закрыть навигацию"
      @click="sidebarOpen = false"
    />

    <div class="dashboard-body">
      <AppTopbar @toggle-navigation="sidebarOpen = !sidebarOpen" />

      <!-- Ограничение ширины сохраняет читаемость таблиц и форм на больших экранах. -->
      <main class="dashboard-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.dashboard-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at 82% 4%, rgb(249 115 22 / 5%), transparent 24rem),
    var(--color-bg-page);
}

.dashboard-body {
  min-height: 100vh;
  margin-left: var(--sidebar-width);
}

.dashboard-content {
  width: min(100%, var(--content-max-width));
  margin: 0 auto;
  padding: 32px 28px 56px;
}

.navigation-backdrop {
  position: fixed;
  z-index: 30;
  inset: 0;
  display: none;
  border: 0;
  background: rgb(17 24 39 / 52%);
}

@media (max-width: 900px) {
  .dashboard-body {
    margin-left: 0;
  }

  .navigation-backdrop {
    display: block;
  }
}

@media (max-width: 680px) {
  .dashboard-content {
    padding: 24px 16px 40px;
  }
}
</style>
