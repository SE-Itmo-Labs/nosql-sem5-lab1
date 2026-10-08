import { useState } from '#imports'

export interface AuthUser {
  userId: number
  login: string
  username: string
  role: 'ROLE_USER' | 'ROLE_ADMIN'
  loggedInAt: string
}

interface LoginResponse {
  userId: number
  username: string
  displayName: string
  role: 'ROLE_USER' | 'ROLE_ADMIN'
}

interface StoredAuth {
  user: AuthUser
  password: string
}

const STORAGE_KEY = 'nosql-sem5-lab1-auth'

export const useAuth = () => {
  const user = useState<AuthUser | null>('auth-user', () => null)
  const password = useState<string | null>('auth-password', () => null)
  const { apiBase } = useRuntimeConfig().public

  const restore = () => {
    if (!import.meta.client || user.value) return

    try {
      const raw = sessionStorage.getItem(STORAGE_KEY)
      if (!raw) return

      const stored: StoredAuth = JSON.parse(raw)
      user.value = stored.user
      password.value = stored.password
    }
    catch {
      sessionStorage.removeItem(STORAGE_KEY)
    }
  }

  const login = async (login: string, enteredPassword: string) => {
    const response = await $fetch<LoginResponse>(`${apiBase}/api/v1/auth/login`, {
      method: 'POST',
      body: {
        username: login,
        password: enteredPassword,
      },
    })

    const authUser: AuthUser = {
      userId: response.userId,
      login: response.username,
      username: response.displayName,
      role: response.role,
      loggedInAt: new Date().toISOString(),
    }

    user.value = authUser
    password.value = enteredPassword

    if (import.meta.client) {
      const stored: StoredAuth = { user: authUser, password: enteredPassword }
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(stored))
    }

    return authUser
  }

  const authHeaders = () => {
    if (!user.value || !password.value) return {}

    return {
      'X-Username': user.value.login,
      'X-Password': password.value,
    }
  }

  const logout = () => {
    user.value = null
    password.value = null
    if (import.meta.client) sessionStorage.removeItem(STORAGE_KEY)
  }

  return { user, restore, login, logout, authHeaders }
}
