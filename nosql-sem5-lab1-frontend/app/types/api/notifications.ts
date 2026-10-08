import type { IsoDateTime, PageQuery } from './common'

/** Состояние уведомления, которое показывается в личном кабинете. */
export type NotificationStatus = 'SENT' | 'READ'

/** Уведомление в том виде, в котором его получает frontend. */
export interface Notification {
  id: number
  userId: string
  title: string
  text: string
  categoryId: number
  status: NotificationStatus
  createdAt: IsoDateTime
}

/** Тело запроса на создание и отправку уведомления. */
export interface CreateNotificationRequest {
  userId: string
  title: string
  text: string
  categoryId: number
}

/** ID, получатель и дата создания не меняются после отправки. */
export interface UpdateNotificationRequest {
  title?: string
  text?: string
  categoryId?: number
  status?: NotificationStatus
}

/** Фильтры списка уведомлений; все поля необязательны. */
export interface NotificationQuery extends PageQuery {
  userId?: string
  categoryId?: number
  status?: NotificationStatus
  q?: string
}

/** Агрегация для информационных карточек на главной странице. */
export interface NotificationStats {
  total: number
  sent: number
  read: number
  byCategory: Record<string, number>
}
