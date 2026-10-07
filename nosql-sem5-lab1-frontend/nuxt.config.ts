// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },

  css: ['~/assets/css/main.css'],

  nitro: {
    prerender: {
      routes: ['/']
    }
  },
  app: {
    baseURL: '/nosql-sem5-lab1/',
  },

  ssr: true,

  modules: [
    '@nuxt/eslint',
    '@nuxt/icon',
    '@nuxt/image',
    '@nuxt/scripts'
  ],

  runtimeConfig: {
    public: {
      // URL используется только real-клиентом; mock не выполняет сетевых запросов.
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:16767',
      // Backend пока неполный, поэтому безопасный режим разработки — явный mock.
      apiMode: process.env.NUXT_PUBLIC_API_MODE || 'mock'
    }
  }
})
