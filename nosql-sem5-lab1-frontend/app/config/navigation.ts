import type { UserRole } from '~/types/api'

/** Описание одного раздела личного кабинета. */
export interface NavigationItem {
  label: string
  to: string
  icon: string
  audience: 'all' | 'admin'
}

/** Разделы бокового меню и роли, которым они доступны. */
export const APP_NAVIGATION: readonly NavigationItem[] = [
  {
    label: 'Афиша',
    to: '/movies',
    icon: '◉',
    audience: 'all',
  },
  {
    label: 'Уведомления',
    to: '/notifications',
    icon: '✦',
    audience: 'all',
  },
  {
    label: 'Мои заказы',
    to: '/orders',
    icon: '▤',
    audience: 'all',
  },
  {
    label: 'Категории',
    to: '/categories',
    icon: '▦',
    audience: 'admin',
  },
  {
    label: 'Аналитика уведомлений',
    to: '/notification-analytics',
    icon: '▥',
    audience: 'admin',
  },
  {
    label: 'Согласованность',
    to: '/consistency',
    icon: '⇄',
    audience: 'admin',
  },
] as const

export const getNavigationForRole = (role?: UserRole): readonly NavigationItem[] => (
  APP_NAVIGATION.filter(item => item.audience === 'all' || role === 'ROLE_ADMIN')
)
