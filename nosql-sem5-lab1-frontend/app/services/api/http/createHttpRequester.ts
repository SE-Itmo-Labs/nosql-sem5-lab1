import { normalizeHttpError } from '../ApiClientError'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
type QueryParameters = Record<string, string | number | boolean | undefined>

interface RequestOptions {
  body?: object
  query?: object
}

export interface HttpRequester {
  get<T>(path: string, query?: object): Promise<T>
  post<T>(path: string, body?: object): Promise<T>
  put<T>(path: string, body?: object): Promise<T>
  patch<T>(path: string, body?: object): Promise<T>
  delete(path: string): Promise<void>
}

interface HttpRequesterOptions {
  baseUrl: string
  getAccessToken: () => string | null
}

/**
 * Низкоуровневый HTTP-слой отвечает только за адрес, Bearer-токен и ошибки.
 * Знание предметных маршрутов остается в createHttpApiClient.
 */
export const createHttpRequester = ({
  baseUrl,
  getAccessToken,
}: HttpRequesterOptions): HttpRequester => {
  const normalizedBaseUrl = baseUrl.replace(/\/$/, '')

  const request = async <T>(
    method: HttpMethod,
    path: string,
    options: RequestOptions = {},
  ): Promise<T> => {
    const token = getAccessToken()
    const headers: Record<string, string> = {
      Accept: 'application/json',
    }

    if (token) headers.Authorization = `Bearer ${token}`

    try {
      return await $fetch<T>(`${normalizedBaseUrl}${path}`, {
        method,
        body: options.body,
        query: options.query as QueryParameters | undefined,
        headers,
      })
    }
    catch (error) {
      throw normalizeHttpError(error, path)
    }
  }

  return {
    get: <T>(path: string, query?: object) => request<T>('GET', path, { query }),
    post: <T>(path: string, body?: object) => request<T>('POST', path, { body }),
    put: <T>(path: string, body?: object) => request<T>('PUT', path, { body }),
    patch: <T>(path: string, body?: object) => request<T>('PATCH', path, { body }),
    delete: async (path: string) => {
      await request<unknown>('DELETE', path)
    },
  }
}
