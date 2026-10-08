/** Учетные данные для входа через POST /api/v1/auth/login. */
export interface LoginRequest {
  username: string
  password: string
}

/** Данные для создания учетной записи клиента кинотеатра. */
export interface RegisterRequest extends LoginRequest {
  email: string
}

/**
 * Результат входа или регистрации.
 * Поля повторяют целевой AuthResponse на backend.
 */
export interface AuthResponse {
  token: string
  type: 'Bearer'
  username: string
  email: string | null
}
