<script setup lang="ts">
import FeatureCard from '~/components/feature/FeatureCard.vue'
import PageHeader from '~/components/ui/PageHeader.vue'
import { APP_NAVIGATION } from '~/config/navigation'

defineOptions({ name: 'DashboardOverviewPage' })

definePageMeta({
  layout: 'dashboard',
  middleware: 'auth',
  title: 'Обзор',
})

const { user } = useAuth()
const featureLinks = APP_NAVIGATION.filter(item => item.to !== '/')
const isMockMode = useRuntimeConfig().public.apiMode === 'mock'
const implementedFeatures = 3
</script>

<template>
  <div>
    <PageHeader
      eyebrow="Лабораторная работа №1"
      :title="`Добро пожаловать${user ? `, ${user.username}` : ''}`"
      description="Личный кабинет объединяет обязательный сценарий отправки уведомлений и дополнительные механизмы Redis из варианта."
    />

    <!-- Верхняя сводка объясняет готовность среды, не подменяя реальные метрики. -->
    <section class="overview-strip" aria-label="Состояние приложения">
      <div class="overview-item">
        <span class="overview-label">Источник данных</span>
        <strong>{{ isMockMode ? 'Локальный mock' : 'Spring Boot API' }}</strong>
      </div>
      <div class="overview-item">
        <span class="overview-label">Хранилище варианта</span>
        <strong>Redis</strong>
      </div>
      <div class="overview-item">
        <span class="overview-label">Сценариев</span>
        <strong>{{ featureLinks.length }}</strong>
      </div>
      <div class="overview-status">
        <span aria-hidden="true" />
        {{ implementedFeatures }} из {{ featureLinks.length }} готовы
      </div>
    </section>

    <!-- Карточки и sidebar используют одну конфигурацию APP_NAVIGATION. -->
    <section class="features-section">
      <div class="section-heading">
        <div>
          <p>Разделы приложения</p>
          <h3>Сценарии лабораторной</h3>
        </div>
        <span>Выберите раздел для перехода</span>
      </div>

      <div class="features-grid">
        <FeatureCard v-for="feature in featureLinks" :key="feature.to" :feature="feature" />
      </div>
    </section>

    <section class="architecture-note">
      <span class="architecture-mark" aria-hidden="true">i</span>
      <div>
        <strong>Mock и backend используют один API-контракт</strong>
        <p>
          Компоненты страниц не содержат временных ветвлений. После готовности
          backend достаточно выбрать <code>NUXT_PUBLIC_API_MODE=real</code>.
        </p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.overview-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(130px, 1fr)) auto;
  gap: 1px;
  overflow: hidden;
  margin-bottom: 30px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-md);
  background: var(--color-border-soft);
  box-shadow: var(--shadow-sm);
}

.overview-item,
.overview-status {
  padding: 18px 20px;
  background: var(--color-surface);
}

.overview-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.overview-label {
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 650;
  text-transform: uppercase;
}

.overview-item strong {
  font-size: 15px;
}

.overview-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-success);
  font-size: 13px;
  font-weight: 750;
}

.overview-status span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-success);
  box-shadow: 0 0 0 4px var(--color-success-soft);
}

.features-section {
  margin-top: 10px;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
}

.section-heading p,
.section-heading h3 {
  margin: 0;
}

.section-heading p {
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.section-heading h3 {
  margin-top: 5px;
  font-size: 21px;
}

.section-heading > span {
  color: var(--color-text-muted);
  font-size: 12px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.architecture-note {
  display: flex;
  gap: 14px;
  margin-top: 24px;
  padding: 18px 20px;
  border: 1px solid #bfdbfe;
  border-radius: var(--radius-md);
  background: var(--color-info-soft);
  color: #1e3a8a;
}

.architecture-mark {
  display: grid;
  width: 24px;
  height: 24px;
  flex: 0 0 24px;
  place-items: center;
  border-radius: 50%;
  background: var(--color-info);
  color: #fff;
  font-family: Georgia, serif;
  font-weight: 700;
}

.architecture-note p {
  margin: 5px 0 0;
  font-size: 13px;
  line-height: 1.55;
}

.architecture-note code {
  font-size: 12px;
}

@media (max-width: 760px) {
  .overview-strip {
    grid-template-columns: repeat(2, 1fr);
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .overview-strip {
    grid-template-columns: 1fr;
  }

  .section-heading > span {
    display: none;
  }
}
</style>
