<script setup lang="ts">
defineOptions({ name: 'AuthLayout' })
</script>

<template>
  <div class="auth-layout">
    <!-- Декоративная область объясняет назначение приложения до авторизации. -->
    <section class="auth-intro" aria-label="Кинотеатр">
      <NuxtLink class="auth-brand" to="/login">
        <span class="auth-brand-mark" aria-hidden="true">К</span>
        <span>Кинотеатр</span>
      </NuxtLink>

      <div class="auth-copy">
        <p class="auth-eyebrow">Ваш следующий поход в кино</p>
        <h1>Выберите фильм. Забронируйте лучшие места.</h1>
        <p>
          Афиша, удобный выбор сеанса и мест, билеты и кинобар.
          Все ваши заказы и сообщения кинотеатра — в личном кабинете.
        </p>
      </div>

      <p class="auth-footnote">Билеты, заказы и уведомления в одном месте</p>
    </section>

    <!-- Форма остается самостоятельным slot и не знает о декоративной части. -->
    <main class="auth-content">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.auth-layout {
  display: grid;
  min-height: 100vh;
  grid-template-columns: minmax(360px, 0.95fr) minmax(420px, 1.05fr);
  background: var(--color-surface);
}

.auth-intro {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  padding: 40px clamp(32px, 6vw, 88px);
  background:
    linear-gradient(145deg, rgb(17 24 39 / 92%), rgb(31 41 55 / 96%)),
    radial-gradient(circle at 20% 20%, var(--color-primary), transparent 45%);
  color: var(--color-text-on-dark);
}

.auth-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  width: fit-content;
  color: var(--color-text-on-dark);
  font-size: 14px;
  font-weight: 750;
  text-decoration: none;
}

.auth-brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 11px;
  background: var(--color-primary);
  font-size: 18px;
}

.auth-copy {
  max-width: 620px;
  margin: auto 0;
}

.auth-eyebrow {
  margin: 0 0 18px;
  color: #fdba74;
  font-size: 12px;
  font-weight: 750;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.auth-copy h1 {
  margin: 0;
  font-size: clamp(34px, 4.4vw, 62px);
  line-height: 1.05;
  letter-spacing: -0.035em;
}

.auth-copy p:last-child {
  max-width: 570px;
  margin: 24px 0 0;
  color: var(--color-text-on-dark-muted);
  font-size: 16px;
  line-height: 1.7;
}

.auth-footnote {
  margin: 0;
  color: #98a2b3;
  font-size: 12px;
}

.auth-content {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 40px 24px;
  background: var(--color-bg-page);
}

@media (max-width: 900px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }

  .auth-intro {
    display: none;
  }
}
</style>
