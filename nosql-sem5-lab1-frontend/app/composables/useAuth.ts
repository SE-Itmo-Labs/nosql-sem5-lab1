import type { AuthResponse, RegisterRequest } from '~/types/api'
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

  /** Вход и регистрация создают одну и ту же сессию из ответа сервера. */
  const establishSession = (response: AuthResponse, enteredPassword: string) => {
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

  const login = async (login: string, enteredPassword: string) => {
    const response = await api.auth.login({ username: login, password: enteredPassword })
    return establishSession(response, enteredPassword)
  }

  const register = async (request: RegisterRequest) => {
    const response = await api.auth.register(request)
    return establishSession(response, request.password)
  }

  const logout = () => {
    user.value = null
    clearStoredAuthSession()
  }

  return { user, restore, login, register, logout }
}
