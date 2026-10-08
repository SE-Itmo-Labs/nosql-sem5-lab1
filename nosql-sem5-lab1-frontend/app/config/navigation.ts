/** Описание одного раздела личного кабинета. */
export interface NavigationItem {
  label: string
  shortLabel: string
  description: string
  to: string
  icon: string
  accent: 'orange' | 'blue' | 'green' | 'violet' | 'slate'
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
  },
  {
    label: 'Уведомления',
    shortLabel: 'Уведомления',
    description: 'Отправка и история уведомлений клиента кинотеатра.',
    to: '/notifications',
    icon: '✦',
    accent: 'orange',
  },
  {
    label: 'Категории',
    shortLabel: 'Категории',
    description: 'Справочник и наглядная демонстрация cache-aside.',
    to: '/categories',
    icon: '▦',
    accent: 'blue',
  },
  {
    label: 'Временная бронь',
    shortLabel: 'TTL-блокировка',
    description: 'Временная блокировка места с автоматическим истечением TTL.',
    to: '/blocks',
    icon: '◷',
    accent: 'green',
  },
  {
    label: 'Распределённый lock',
    shortLabel: 'Lock',
    description: 'Атомарный захват ресурса и безопасное освобождение по токену.',
    to: '/locks',
    icon: '◇',
    accent: 'violet',
  },
  {
    label: 'Согласованность',
    shortLabel: 'Primary / replica',
    description: 'Сравнение eventual-чтения и ожидания подтверждения реплики.',
    to: '/consistency',
    icon: '⇄',
    accent: 'slate',
  },
] as const

/** Возвращает конфигурацию страницы и сразу обнаруживает ошибочный URL. */
export const getNavigationItem = (path: string): NavigationItem => {
  const item = APP_NAVIGATION.find(candidate => candidate.to === path)
  if (!item) throw new Error(`Раздел навигации не найден: ${path}`)
  return item
}
