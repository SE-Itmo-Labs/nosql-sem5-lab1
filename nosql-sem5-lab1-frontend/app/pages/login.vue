<script setup lang="ts">
import AuthCard from '~/components/auth/AuthCard.vue'
defineOptions({ name: 'UserLoginPage' })

definePageMeta({
  layout: 'auth',
  middleware: 'guest',
  title: 'Вход',
})

const { login } = useAuth()
const router = useRouter()
const form = reactive({ login: '', password: '' })
const error = ref('')
const loading = ref(false)

/** Валидация пустых полей выполняется до обращения к mock или backend. */
const onSubmit = async () => {
  error.value = ''

  if (!form.login.trim() || !form.password) {
    error.value = 'Заполните логин и пароль'
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
  <AuthCard
    eyebrow="С возвращением"
    title="Вход в кабинет"
    description="Используйте учётные данные клиента кинотеатра."
  >
    <form class="login-form" @submit.prevent="onSubmit">
      <label class="field">
        <span class="field-label">Логин</span>
        <input
          v-model="form.login"
          class="field-input"
          type="text"
          name="login"
          autocomplete="username"
          placeholder="например, client01"
          required
        >
      </label>

      <label class="field">
        <span class="field-label">Пароль</span>
        <input
          v-model="form.password"
          class="field-input"
          type="password"
          name="password"
          autocomplete="current-password"
          placeholder="••••••••"
          required
        >
      </label>

      <p v-if="error" class="form-error" role="alert">{{ error }}</p>

      <button class="btn btn-primary btn-block btn-lg" type="submit" :disabled="loading">
        {{ loading ? 'Проверяем данные…' : 'Войти' }}
      </button>
    </form>
    <template #footer>
      Нет аккаунта? <NuxtLink to="/register">Зарегистрироваться</NuxtLink>
    </template>
  </AuthCard>
</template>

<style scoped>
.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
</style>
