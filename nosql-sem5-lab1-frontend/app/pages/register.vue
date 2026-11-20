<script setup lang="ts">
import AuthCard from '~/components/auth/AuthCard.vue'
import RegistrationForm from '~/components/auth/RegistrationForm.vue'
defineOptions({ name: 'UserRegistrationPage' })

definePageMeta({
  layout: 'auth',
  middleware: 'guest',
  title: 'Регистрация',
})
</script>

<template>
  <AuthCard
    eyebrow="Добро пожаловать"
    title="Регистрация"
    description="Создайте аккаунт, чтобы бронировать места и сохранять билеты."
  >
    <RegistrationForm />
    <template #footer>
      Уже есть аккаунт? <NuxtLink to="/login">Войти</NuxtLink>
    </template>
  </AuthCard>
</template>
