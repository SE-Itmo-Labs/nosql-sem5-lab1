import { hasStoredAuthSession } from '~/services/auth/authSession'

/**
 * Защищает страницы личного кабинета.
 * На сервере редирект не выполняется, потому что статическая генерация не имеет
 * доступа к localStorage. На клиенте middleware только проверяет хранилище:
 * восстановление Vue-state до гидратации создало бы несовпадение HTML.
 */
export default defineNuxtRouteMiddleware(() => {
  if (import.meta.server) return

  if (!hasStoredAuthSession()) return navigateTo('/login')
})
