/**
 * Защищает страницы личного кабинета.
 * На сервере редирект не выполняется, потому что статическая генерация не имеет
 * доступа к localStorage; окончательная проверка происходит после гидратации.
 */
export default defineNuxtRouteMiddleware(() => {
  if (import.meta.server) return

  const { user, restore } = useAuth()
  restore()

  if (!user.value) return navigateTo('/login')
})
