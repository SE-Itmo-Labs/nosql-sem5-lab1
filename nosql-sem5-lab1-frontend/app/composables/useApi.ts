import { API_ENDPOINTS } from '~/constants/apiEndpoints'

/**
 * Минимальный HTTP-вход в backend.
 *
 * На текущем этапе здесь оставлена только техническая проверка `/hello`.
 * Доменные методы будут добавлены через интерфейс ApiClient одновременно с
 * HTTP- и mock-реализациями, чтобы временные заглушки не протекали в страницы.
 */
export const useApi = () => {
  const { apiBase } = useRuntimeConfig().public

  return {
    hello: () => $fetch<string>(`${apiBase}${API_ENDPOINTS.system.hello}`),
  }
}
