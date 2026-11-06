import { readStoredAuthSession } from '~/services/auth/authSession'

/** Prevents client accounts from opening administrative laboratory pages. */
export default defineNuxtRouteMiddleware(() => {
  if (import.meta.server) return

  const session = readStoredAuthSession()
  if (!session) return navigateTo('/login')
  if (session.user.role !== 'ROLE_ADMIN') return navigateTo('/')
})
