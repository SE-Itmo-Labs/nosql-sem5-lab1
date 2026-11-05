<script setup lang="ts">
import { ApiClientError } from '~/services/api/ApiClientError'
import { validateRegistration } from '~/utils/registrationValidation'

const { register } = useAuth()
const router = useRouter()
const form = reactive({ username: '', displayName: '', password: '', confirmPassword: '' })
const loading = ref(false)
const error = ref('')

const onSubmit = async () => {
  if (loading.value) return

  // Пробелы в пароле сохраняем: при входе он должен совпасть с введённым.
  const request = {
    username: form.username.trim(),
    displayName: form.displayName.trim(),
    password: form.password,
  }
  error.value = validateRegistration(request)
  if (error.value) return
  if (form.password !== form.confirmPassword) {
    error.value = 'Пароли не совпадают'
    return
  }

  loading.value = true
  try {
    await register(request)
    await router.push('/movies')
  }
  catch (cause) {
    error.value = cause instanceof ApiClientError && cause.status === 409
      ? 'Этот логин уже занят. Выберите другой.'
      : cause instanceof ApiClientError && cause.status === 400
        ? 'Проверьте данные: каждое поле должно содержать от 5 до 40 символов.'
        : 'Не удалось зарегистрироваться. Попробуйте ещё раз.'
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <form class="registration-form" :aria-busy="loading" @submit.prevent="onSubmit">
    <p id="registration-hint" class="registration-hint">Имя, логин и пароль — от 5 до 40 символов.</p>

    <label class="field">
      <span class="field-label">Ваше имя</span>
      <input
        v-model="form.displayName"
        class="field-input"
        type="text"
        name="displayName"
        autocomplete="nickname"
        placeholder="Как к вам обращаться"
        minlength="5"
        maxlength="40"
        aria-describedby="registration-hint"
        :disabled="loading"
        required
      >
    </label>

    <label class="field">
      <span class="field-label">Логин</span>
      <input
        v-model="form.username"
        class="field-input"
        type="text"
        name="username"
        autocomplete="username"
        autocapitalize="none"
        spellcheck="false"
        placeholder="Придумайте логин"
        minlength="5"
        maxlength="40"
        aria-describedby="registration-hint"
        :disabled="loading"
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
        autocomplete="new-password"
        placeholder="Придумайте пароль"
        minlength="5"
        maxlength="40"
        aria-describedby="registration-hint"
        :disabled="loading"
        required
      >
    </label>

    <label class="field">
      <span class="field-label">Повторите пароль</span>
      <input
        v-model="form.confirmPassword"
        class="field-input"
        type="password"
        name="confirmPassword"
        autocomplete="new-password"
        placeholder="Введите пароль ещё раз"
        minlength="5"
        maxlength="40"
        :disabled="loading"
        required
      >
    </label>

    <p v-if="error" class="form-error" role="alert">{{ error }}</p>

    <button class="btn btn-primary btn-block btn-lg" type="submit" :disabled="loading">
      {{ loading ? 'Создаём аккаунт…' : 'Зарегистрироваться' }}
    </button>
  </form>
</template>

<style scoped>
.registration-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.registration-hint {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 13px;
}
</style>
