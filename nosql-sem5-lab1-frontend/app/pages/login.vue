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
  <section class="login-card">
    <!-- Заголовок остается нейтральным и подходит mock- и real-режиму. -->
    <header class="login-heading">
      <p class="login-eyebrow">С возвращением</p>
      <h2>Вход в кабинет</h2>
      <p>Используйте учётные данные клиента кинотеатра.</p>
    </header>

    <p v-if="isMockMode" class="demo-hint">
      <strong>Демо-доступ</strong>
      <span>client01 / client123</span>
    </p>

    <form class="login-form" @submit.prevent="onSubmit">
      <label class="field">
        <span class="field-label">Логин</span>
        <input
          v-model="form.login"
          type="text"
          name="login"
          autocomplete="username"
          placeholder="например, client01"
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

.login-heading p:last-child {
  margin-top: 8px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.demo-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 0 0 22px;
  padding: 11px 13px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-soft);
  color: #9a3412;
  font-size: 12px;
}

.demo-hint span {
  font-family: "Cascadia Code", Consolas, monospace;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

@media (max-width: 480px) {
  .login-card {
    padding: 28px 22px;
  }
}
</style>
