import { useState } from '#imports'
import {
  clearStoredAuthSession,
  readStoredAuthSession,
  saveStoredAuthSession,
  type StoredAuthUser,
} from '~/services/auth/authSession'

export type AuthUser = StoredAuthUser

export const useAuth = () => {
  const user = useState<AuthUser | null>('auth-user', () => null)
  const api = useApi()

  /** Восстановление после mount не создает расхождение со статическим HTML. */
  const restore = () => {
    if (!import.meta.client || user.value) return

    const stored = readStoredAuthSession()
    if (stored) {
      user.value = stored.user
    }
  }

  const login = async (login: string, enteredPassword: string) => {
    const response = await api.auth.login({ username: login, password: enteredPassword })

    const authUser: AuthUser = {
      userId: response.userId,
      login: response.username,
      username: response.displayName,
      role: response.role,
      loggedInAt: new Date().toISOString(),
    }

    user.value = authUser
    saveStoredAuthSession({ user: authUser, password: enteredPassword })

    return authUser
  }

  const logout = () => {
    user.value = null
    clearStoredAuthSession()
  }

  return { user, restore, login, logout }
}
