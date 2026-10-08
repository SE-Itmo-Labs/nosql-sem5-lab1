
export const useApi = () => {
  const { apiBase } = useRuntimeConfig().public
  const { authHeaders } = useAuth()

  return {
    hello: () => $fetch(`${apiBase}/api/v1/hello`, {
      headers: authHeaders(),
    }),
  }
}
