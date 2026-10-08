/** Авторизованному пользователю форма входа больше не нужна. */
export default defineNuxtRouteMiddleware(() => {
  if (import.meta.server) return

  const { user, restore } = useAuth()
  restore()

  if (user.value) return navigateTo('/')
})
