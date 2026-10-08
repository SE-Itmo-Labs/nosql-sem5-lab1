// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },

  // Стили разделены по назначению, чтобы базовые правила не превращались в монолит.
  css: [
    '~/assets/css/tokens.css',
    '~/assets/css/base.css',
    '~/assets/css/buttons.css',
    '~/assets/css/forms.css',
    '~/assets/css/surfaces.css'
  ],

  nitro: {
    prerender: {
      // GitHub Pages не имеет runtime-сервера, поэтому каждый маршрут генерируется заранее.
      routes: [
        '/',
        '/login',
        '/notifications',
        '/categories',
        '/blocks',
        '/locks',
        '/consistency'
      ]
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
