/**
 * Публичная точка входа для API-типов.
 * Внутренние файлы разделены по предметным областям, а остальной frontend
 * импортирует их из `~/types/api`, не завязываясь на структуру каталогов.
 */
export type * from './auth'
export type * from './blocks'
export type * from './categories'
export type * from './client'
export type * from './common'
export type * from './consistency'
export type * from './notifications'
