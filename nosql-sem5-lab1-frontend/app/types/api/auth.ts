/** Учетные данные для входа через POST /api/v1/auth/login. */
export interface LoginRequest {
  username: string
  password: string
}

/** Роли дословно повторяют enum Role из Spring Boot. */
export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN'

/**
 * Фактический AuthResponse backend. JWT сейчас не используется: защищенные
 * запросы передают исходные credentials в X-Username и X-Password.
 */
export interface AuthResponse {
  userId: number
  username: string
  displayName: string
  role: UserRole
}
