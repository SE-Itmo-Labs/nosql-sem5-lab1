import { useState } from '#imports'

export interface AuthUser {
  /** Логин пользователя (он же — идентификатор клиента в БД/Redis) */
  login: string
  /** Отображаемое имя (пока = логин, позже можно брать из ответа API) */
  username: string
  /** Момент входа (ISO), для отчёта */
  loggedInAt: string
}

const STORAGE_KEY = 'nosql-sem5-lab1-auth'

/**
 * Клиентская авторизация.
 *
 * СЕЙЧАС: демо-режим — валидируем поля и сохраняем сессию в localStorage,
 * потому что на беке ещё нет эндпоинта аутентификации.
 *
 * ПОТОМ: в login() заменить тело на вызов
 *   POST {apiBase}/auth/login  {  login, password  }
 * и брать пользователя + токен из ответа сервера.
 */
export const useAuth = () => {
  const user = useState<AuthUser | null>('auth-user', () => null)

  /** Восстановить сессию из localStorage (клиент). */
  const restore = () => {
    if (import.meta.client && !user.value) {
      try {
        const raw = localStorage.getItem(STORAGE_KEY)
        if (raw) user.value = JSON.parse(raw)
      }
      catch {
        // битые данные — просто игнорируем
      }
    }
  }

  /** Вход: сейчас демо, позже — реальный вызов API. */
  const login = (login: string, _password: string) => {
    // TODO: заменить на POST {apiBase}/auth/login и обработать ошибку 401
    const authUser: AuthUser = {
      login,
      username: login,
      loggedInAt: new Date().toISOString(),
    }
    user.value = authUser
    if (import.meta.client) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(authUser))
    }
    return authUser
  }

  /** Выход: очистить состояние и localStorage. */
  const logout = () => {
    user.value = null
    if (import.meta.client) {
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  return { user, restore, login, logout }
}