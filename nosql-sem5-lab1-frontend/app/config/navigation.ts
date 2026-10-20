import type { UserRole } from '~/types/api'

/** Описание одного раздела личного кабинета. */
export interface NavigationItem {
  label: string
  shortLabel: string
  description: string
  to: string
  icon: string
  accent: 'orange' | 'blue' | 'green' | 'violet' | 'slate'
  audience: 'all' | 'admin'
}

/**
 * Единый список используется sidebar и главной страницей.
 * Название, URL и описание раздела поэтому не расходятся между компонентами.
 */
export const APP_NAVIGATION: readonly NavigationItem[] = [
  {
    label: 'Обзор',
    shortLabel: 'Главная',
    description: 'Состояние лабораторных сценариев и быстрые переходы.',
    to: '/',
    icon: '⌂',
    accent: 'slate',
    audience: 'all',
  },
  {
    label: 'Афиша',
    shortLabel: 'Афиша',
    description: 'Фильмы, будущие сеансы и выбор мест в зале.',
    to: '/movies',
    icon: '◉',
    accent: 'orange',
    audience: 'all',
  },
  {
    label: 'Уведомления',
    shortLabel: 'Уведомления',
    description: 'Отправка и история уведомлений клиента кинотеатра.',
    to: '/notifications',
    icon: '✦',
    accent: 'orange',
    audience: 'all',
  },
  {
    label: 'Категории',
    shortLabel: 'Категории',
    description: 'Справочник и наглядная демонстрация cache-aside.',
    to: '/categories',
    icon: '▦',
    accent: 'blue',
    audience: 'admin',
  },
  {
    label: 'Временная бронь',
    shortLabel: 'TTL-блокировка',
    description: 'Временная блокировка места с автоматическим истечением TTL.',
    to: '/blocks',
    icon: '◷',
    accent: 'green',
    audience: 'all',
  },
  {
    label: 'Распределённый lock',
    shortLabel: 'Lock',
    description: 'Конкуренция запросов за критическую секцию Redisson RLock.',
    to: '/locks',
    icon: '◇',
    accent: 'violet',
    audience: 'admin',
  },
  {
    label: 'Согласованность',
    shortLabel: 'Primary / replica',
    description: 'Сравнение eventual-чтения и ожидания подтверждения реплики.',
    to: '/consistency',
    icon: '⇄',
    accent: 'slate',
    audience: 'admin',
  },
] as const

export const getNavigationForRole = (role?: UserRole): readonly NavigationItem[] => (
  APP_NAVIGATION.filter(item => item.audience === 'all' || role === 'ROLE_ADMIN')
)

/** Возвращает конфигурацию страницы и сразу обнаруживает ошибочный URL. */
export const getNavigationItem = (path: string): NavigationItem => {
  const item = APP_NAVIGATION.find(candidate => candidate.to === path)
  if (!item) throw new Error(`Раздел навигации не найден: ${path}`)
  return item
}
