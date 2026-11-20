import type { UserRole } from '~/types/api'

/** Ключ единственной сохраненной сессии личного кабинета. */
export const AUTH_SESSION_STORAGE_KEY = 'nosql-sem5-lab1-auth'

export interface StoredAuthUser {
  userId: number
  /** Логин используется как бизнес-идентификатор клиента. */
  login: string
  /** Отображаемое имя приходит в displayName backend-ответа. */
  username: string
  role: UserRole
  loggedInAt: string
}

export interface StoredAuthSession {
  user: StoredAuthUser
  password: string
}

/** Проверяет всю структуру, чтобы поврежденная запись не считалась сессией. */
const isStoredAuthSession = (value: unknown): value is StoredAuthSession => {
  if (!value || typeof value !== 'object') return false
  const candidate = value as Partial<StoredAuthSession>
  const storedUser = candidate.user as Partial<StoredAuthUser> | undefined

  return Boolean(storedUser)
    && typeof storedUser?.userId === 'number'
    && typeof storedUser.login === 'string'
    && typeof storedUser.username === 'string'
    && (storedUser.role === 'ROLE_USER' || storedUser.role === 'ROLE_ADMIN')
    && typeof storedUser.loggedInAt === 'string'
    && typeof candidate.password === 'string'
}

/** Сессия ограничена вкладкой, потому что временная схема хранит пароль. */
export const readStoredAuthSession = (): StoredAuthSession | null => {
  if (!import.meta.client) return null

  try {
    const value: unknown = JSON.parse(sessionStorage.getItem(AUTH_SESSION_STORAGE_KEY) ?? 'null')
    return isStoredAuthSession(value) ? value : null
  }
  catch {
    return null
  }
}

export const saveStoredAuthSession = (session: StoredAuthSession): void => {
  if (import.meta.client) {
    sessionStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session))
  }
}

export const clearStoredAuthSession = (): void => {
  if (import.meta.client) sessionStorage.removeItem(AUTH_SESSION_STORAGE_KEY)
}

/**
 * Middleware проверяет наличие сессии, но не переносит ее в Nuxt state до
 * гидратации. Иначе статический HTML без localStorage отличался бы от клиента.
 */
export const hasStoredAuthSession = (): boolean => {
  return readStoredAuthSession() !== null
}

/**
 * Заголовки повторяют SimpleAuthFilter backend. Чтение вынесено из useAuth,
 * чтобы цепочка useAuth -> useApi -> HTTP-клиент не стала циклической.
 */
export const readAuthHeaders = (): Record<string, string> => {
  const session = readStoredAuthSession()
  if (!session) return {}

  return {
    'X-Username': session.user.login,
    'X-Password': session.password,
  }
}
