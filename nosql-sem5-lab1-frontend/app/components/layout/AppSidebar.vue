<script setup lang="ts">
import { APP_NAVIGATION } from '~/config/navigation'

defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const route = useRoute()

/** Главная считается активной только при точном совпадении, вложенные — по префиксу. */
const isActive = (path: string): boolean => path === '/'
  ? route.path === path
  : route.path.startsWith(path)
</script>

<template>
  <aside class="sidebar" :class="{ 'sidebar--open': open }" aria-label="Основная навигация">
    <!-- Бренд одновременно возвращает пользователя на обзор приложения. -->
    <NuxtLink class="brand" to="/" @click="emit('close')">
      <span class="brand-mark" aria-hidden="true">К</span>
      <span>
        <strong class="brand-name">Кинотеатр</strong>
        <span class="brand-caption">NoSQL Lab · Redis</span>
      </span>
    </NuxtLink>

    <!-- Пункты строятся из единой конфигурации, общей с главной страницей. -->
    <nav class="navigation">
      <p class="navigation-label">Личный кабинет</p>
      <NuxtLink
        v-for="item in APP_NAVIGATION"
        :key="item.to"
        :to="item.to"
        class="navigation-link"
        :class="{ 'navigation-link--active': isActive(item.to) }"
        @click="emit('close')"
      >
        <span class="navigation-icon" aria-hidden="true">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </NuxtLink>
    </nav>

    <!-- Подпись объясняет учебное назначение экранов на защите лабораторной. -->
    <div class="sidebar-note">
      <strong>Вариант 1869847</strong>
      <span>Уведомления, TTL, кэш, lock и согласованность.</span>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  z-index: 40;
  inset: 0 auto 0 0;
  display: flex;
  width: var(--sidebar-width);
  flex-direction: column;
  padding: 22px 16px;
  background: var(--color-bg-dark);
  color: var(--color-text-on-dark);
  transition: transform 0.2s ease;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px 24px;
  text-decoration: none;
}

.brand-mark {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 12px;
  background: var(--color-primary);
  font-size: 20px;
  font-weight: 800;
}

.brand-name,
.brand-caption {
  display: block;
}

.brand-name {
  font-size: 16px;
}

.brand-caption {
  margin-top: 3px;
  color: var(--color-text-on-dark-muted);
  font-size: 11px;
}

.navigation {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.navigation-label {
  margin: 0 10px 8px;
  color: #98a2b3;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.navigation-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 12px;
  border-radius: 10px;
  color: var(--color-text-on-dark-muted);
  font-size: 14px;
  font-weight: 550;
  text-decoration: none;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.navigation-link:hover {
  background: rgb(255 255 255 / 7%);
  color: var(--color-text-on-dark);
}

.navigation-link--active {
  background: rgb(249 115 22 / 16%);
  color: #fdba74;
}

.navigation-icon {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  font-size: 18px;
}

.sidebar-note {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-top: auto;
  padding: 14px;
  border: 1px solid rgb(255 255 255 / 9%);
  border-radius: 12px;
  background: rgb(255 255 255 / 4%);
  color: var(--color-text-on-dark-muted);
  font-size: 11px;
  line-height: 1.45;
}

.sidebar-note strong {
  color: var(--color-text-on-dark);
}

@media (max-width: 900px) {
  .sidebar {
    box-shadow: var(--shadow-lg);
    transform: translateX(-105%);
  }

  .sidebar--open {
    transform: translateX(0);
  }
}
</style>
