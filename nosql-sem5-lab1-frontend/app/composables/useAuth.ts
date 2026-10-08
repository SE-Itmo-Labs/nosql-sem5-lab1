import { useState } from '#imports'
import { AUTH_SESSION_STORAGE_KEY } from '~/services/auth/authSession'

export interface AuthUser {
  /** Логин одновременно является идентификатором клиента в учебном приложении. */
  username: string
  /** Email может отсутствовать у старой учетной записи backend. */
  email: string | null
  /** Access token передается HTTP-клиентом в заголовке Authorization. */
  token: string
  /** Тип токена зафиксирован API-контрактом. */
  type: 'Bearer'
  /** Момент входа (ISO), для отчёта */
  loggedInAt: string
}

/** Не позволяет восстановить устаревшую или поврежденную запись localStorage. */
const isAuthUser = (value: unknown): value is AuthUser => {
  if (!value || typeof value !== 'object') return false
  const candidate = value as Partial<AuthUser>

  return typeof candidate.username === 'string'
    && (typeof candidate.email === 'string' || candidate.email === null)
    && typeof candidate.token === 'string'
    && candidate.type === 'Bearer'
    && typeof candidate.loggedInAt === 'string'
}

/**
 * Клиентская авторизация.
 *
 * useApi сам выбирает mock или real, поэтому логика сессии не содержит
 * временных ветвлений и останется прежней после подключения backend.
 */
export const useAuth = () => {
  const user = useState<AuthUser | null>('auth-user', () => null)
  const api = useApi()

  /** Восстановить сессию из localStorage (клиент). */
  const restore = () => {
    if (import.meta.client && !user.value) {
      try {
        const raw = localStorage.getItem(AUTH_SESSION_STORAGE_KEY)
        const stored = raw ? JSON.parse(raw) : null

        if (isAuthUser(stored)) {
          user.value = stored
        }
        else if (raw) {
          localStorage.removeItem(AUTH_SESSION_STORAGE_KEY)
        }
      }
      catch {
        // Поврежденная сессия не должна ломать загрузку страницы.
        localStorage.removeItem(AUTH_SESSION_STORAGE_KEY)
      }
    }
  }

  /** Выполнить вход через активную реализацию ApiClient и сохранить сессию. */
  const login = async (username: string, password: string): Promise<AuthUser> => {
    const response = await api.auth.login({ username, password })
    const authUser: AuthUser = {
      username: response.username,
      email: response.email,
      token: response.token,
      type: response.type,
      loggedInAt: new Date().toISOString(),
    }
    user.value = authUser

    if (import.meta.client) {
      localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(authUser))
    }

    return authUser
  }

  /** Выход: очистить состояние и localStorage. */
  const logout = () => {
    user.value = null
    if (import.meta.client) {
      localStorage.removeItem(AUTH_SESSION_STORAGE_KEY)
    }
  }

  return { user, restore, login, logout }
}
