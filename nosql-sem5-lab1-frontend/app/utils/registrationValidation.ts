import type { RegisterRequest } from '~/types/api'

/** Ограничения RegisterRequest backend одинаковы для формы и mock API. */
export const validateRegistration = (request: RegisterRequest): string => {
  const fields = [
    ['Логин', request.username],
    ['Имя', request.displayName],
    ['Пароль', request.password],
  ] as const

  for (const [label, value] of fields) {
    if (!value.trim()) return `${label}: заполните поле`
    if (value.length < 5 || value.length > 40) {
      return `${label}: нужно от 5 до 40 символов`
    }
  }

  return ''
}
