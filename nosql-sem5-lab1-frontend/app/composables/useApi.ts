
export const useApi = () => {
  const { apiBase } = useRuntimeConfig().public

  return {
    hello: () => $fetch(`${apiBase}/hello`)
  }
}