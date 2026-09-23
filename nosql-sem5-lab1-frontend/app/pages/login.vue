<script setup lang="ts">
const { user, restore, login } = useAuth()
restore()

const router = useRouter()

const form = reactive({
  login: '',
  password: '',
})

const error = ref('')
const loading = ref(false)

// Уже вошли — сразу на главную
onMounted(() => {
  if (user.value) {
    router.push('/')
  }
})

async function onSubmit() {
  error.value = ''

  if (!form.login.trim() || !form.password) {
    error.value = 'Заполни логин и пароль'
    return
  }

  loading.value = true
  try {
    // Сейчас — демо-вход (без backend). Позже заменим на POST /auth/login через useApi().
    login(form.login.trim(), form.password)
    await router.push('/')
  }
  catch (e) {
    error.value = e instanceof Error ? e.message : 'Не удалось войти'
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">Кинотеатр</h1>
      <p class="login-subtitle">Личный кабинет клиента</p>

      <form class="login-form" @submit.prevent="onSubmit">
        <label class="field">
          <span class="field-label">Логин</span>
          <input
            v-model="form.login"
            type="text"
            name="login"
            autocomplete="username"
            placeholder="например, ivanov01"
            class="field-input"
          >
        </label>

        <label class="field">
          <span class="field-label">Пароль</span>
          <input
            v-model="form.password"
            type="password"
            name="password"
            autocomplete="current-password"
            placeholder="••••••••"
            class="field-input"
          >
        </label>

        <p v-if="error" class="form-error" role="alert">
          {{ error }}
        </p>

        <button
          type="submit"
          class="btn btn-primary btn-block btn-lg"
          :disabled="loading"
        >
          {{ loading ? 'Входим…' : 'Войти' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(160deg, var(--color-bg-dark) 0%, var(--color-bg-dark-soft) 100%);
}

.login-card {
  width: 100%;
  max-width: 380px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 40px 32px;
  box-shadow: var(--shadow-lg);
}

.login-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  color: var(--color-text);
}

.login-subtitle {
  margin: 8px 0 32px;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
</style>