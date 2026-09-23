<script setup lang="ts">
const { user, restore, logout } = useAuth()
restore()

const router = useRouter()

// Нет сессии — на страницу входа
onMounted(() => {
  if (!user.value) {
    router.push('/login')
  }
})

function onLogout() {
  logout()
  router.push('/login')
}
</script>

<template>
  <div class="page">
    <header class="page-header">
      <div>
        <h1 class="page-header-title">Личный кабинет клиента</h1>
        <p v-if="user" class="page-header-subtitle">
          Вы вошли как <strong>{{ user.username }}</strong>
          <span class="page-header-muted">(вход {{ new Date(user.loggedInAt).toLocaleString() }})</span>
        </p>
      </div>
      <button class="btn btn-ghost" type="button" @click="onLogout">
        Выйти
      </button>
    </header>

    <main class="page-main">
      <section class="card">
        <h2 class="card-title">Добро пожаловать!</h2>
        <p class="card-text">
          Это личный кабинет клиента кинотеатра. Здесь появятся разделы
          лабораторной №1: уведомления, категории, бронирование мест и демонстрация
          блокировок.
        </p>
      </section>

      <!-- <section class="grid">
        <NuxtLink class="tile" to="/notifications">
          <span class="tile-title">Уведомления</span>
          <span class="tile-sub">список и отправка</span>
        </NuxtLink>
        <NuxtLink class="tile" to="/categories">
          <span class="tile-title">Категории</span>
          <span class="tile-sub">справочник (кэш)</span>
        </NuxtLink>
        <NuxtLink class="tile" to="/blocks">
          <span class="tile-title">Бронь мест</span>
          <span class="tile-sub">TTL-блокировка</span>
        </NuxtLink>
        <NuxtLink class="tile" to="/locks">
          <span class="tile-title">Блокировки</span>
          <span class="tile-sub">атомарный механизм</span>
        </NuxtLink>
      </section> -->

      <p class="hint">
        Разделы-заглушки. Наполнятся, когда на беке появятся соответствующие REST-эндпоинты.
      </p>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 32px;
  background: var(--color-bg-dark);
  color: var(--color-text-on-dark);
}

.page-header-title {
  margin: 0;
  font-size: 22px;
}

.page-header-subtitle {
  margin: 6px 0 0;
  color: var(--color-text-on-dark-muted);
  font-size: 14px;
}

.page-header-muted {
  color: var(--color-text-muted);
  font-size: 13px;
}

.page-main {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px;
}
</style>