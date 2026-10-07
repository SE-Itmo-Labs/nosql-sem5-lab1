/** Ключ единственной сохраненной сессии личного кабинета. */
export const AUTH_SESSION_STORAGE_KEY = 'nosql-sem5-lab1-auth'

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
