import { hasStoredAuthSession } from '~/services/auth/authSession'

/** Авторизованному пользователю форма входа больше не нужна. */
export default defineNuxtRouteMiddleware(() => {
  if (import.meta.server) return

  if (hasStoredAuthSession()) return navigateTo('/')
})
