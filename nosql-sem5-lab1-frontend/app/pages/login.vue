<script setup lang="ts">
defineOptions({ name: 'UserLoginPage' })

definePageMeta({
  layout: 'auth',
  middleware: 'guest',
  title: 'Вход',
})

const { login } = useAuth()
const router = useRouter()
const isMockMode = useRuntimeConfig().public.apiMode === 'mock'

const form = reactive({
  login: '',
  password: '',
})
const error = ref('')
const loading = ref(false)

/** Валидация пустых полей выполняется до обращения к mock или backend. */
const onSubmit = async () => {
  error.value = ''

  if (!form.login.trim() || !form.password) {
    error.value = 'Заполни логин и пароль'
    return
  }

  loading.value = true
  try {
    await login(form.login.trim(), form.password)
    await router.push('/')
  }
  catch (cause) {
    error.value = cause instanceof Error ? cause.message : 'Не удалось войти'
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
      <p class="login-hint">Тестовый вход: client01 / client123</p>

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

      <button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading">
        {{ loading ? 'Проверяем данные…' : 'Войти' }}
      </button>
    </form>
  </section>
</template>

<style scoped>
.login-card {
  width: 100%;
  max-width: 430px;
  padding: 36px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-md);
}

.login-heading {
  margin-bottom: 24px;
}

.login-eyebrow,
.login-heading h2,
.login-heading p {
  margin: 0;
}

.login-eyebrow {
  color: var(--color-primary-hover);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.login-heading h2 {
  margin-top: 8px;
  font-size: 28px;
  letter-spacing: -0.02em;
}

.login-subtitle {
  margin: 8px 0 6px;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.login-hint {
  margin: 0 0 28px;
  text-align: center;
  color: var(--color-text-muted);
  font-size: 13px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
</style>
