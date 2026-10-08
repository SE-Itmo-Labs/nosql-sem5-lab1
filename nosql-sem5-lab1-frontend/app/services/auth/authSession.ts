/** Ключ единственной сохраненной сессии личного кабинета. */
export const AUTH_SESSION_STORAGE_KEY = 'nosql-sem5-lab1-auth'

/**
 * Middleware проверяет наличие сессии, но не переносит ее в Nuxt state до
 * гидратации. Иначе статический HTML без localStorage отличался бы от клиента.
 */
export const hasStoredAuthSession = (): boolean => {
  if (!import.meta.client) return false

  try {
    const session = JSON.parse(localStorage.getItem(AUTH_SESSION_STORAGE_KEY) ?? '{}') as {
      username?: unknown
      token?: unknown
      type?: unknown
      loggedInAt?: unknown
    }

    return typeof session.username === 'string'
      && typeof session.token === 'string'
      && session.type === 'Bearer'
      && typeof session.loggedInAt === 'string'
  }
  catch {
    return false
  }
}

/**
 * HTTP-клиенту нужен только access token, поэтому чтение сессии вынесено из
 * useAuth. Это предотвращает циклическую зависимость useAuth -> useApi -> useAuth.
 */
export const readAccessToken = (): string | null => {
  if (!import.meta.client) return null

  try {
    const session = JSON.parse(localStorage.getItem(AUTH_SESSION_STORAGE_KEY) ?? '{}') as {
      token?: unknown
    }

    return typeof session.token === 'string' ? session.token : null
  }
  catch {
    return null
  }
}
