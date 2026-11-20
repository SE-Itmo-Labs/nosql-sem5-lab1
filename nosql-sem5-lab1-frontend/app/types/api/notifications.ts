import type { IsoDateTime, PageQuery, PageResponse } from './common'

/** Состояние уведомления, которое показывается в личном кабинете. */
export type NotificationStatus = 'SENT' | 'READ'
export type NotificationActionType = 'CREATED' | 'READ' | 'RULE_APPLIED'
export type NotificationSort = 'UPDATED_AT_DESC' | 'UPDATED_AT_ASC' | 'CREATED_AT_DESC' | 'CREATED_AT_ASC'

/** Уведомление в том виде, в котором его получает frontend. */
export interface Notification {
  id: number
  userId: string
  title: string
  text: string
  categoryId: number
  status: NotificationStatus
  createdAt: IsoDateTime
  updatedAt?: IsoDateTime | null
  processingRuleId?: number | null
  tags?: string[] | null
  sourceEventId?: string | null
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

export interface NotificationSearchQuery extends Omit<NotificationQuery, 'userId' | 'sort'> {
  processingRuleId?: number
  updatedFrom?: IsoDateTime
  updatedTo?: IsoDateTime
  tag?: string
  actionType?: NotificationActionType
  actorId?: string
  sort?: NotificationSort
}

/** Страница в формате маршрута /notifications/search. */
export interface NotificationSearchPage extends Omit<PageResponse<Notification>, 'content' | 'last' | 'empty'> {
  items: Notification[]
}

/** Агрегация для информационных карточек на главной странице. */
export interface NotificationStats {
  total: number
  sent: number
  read: number
  byCategory: Record<string, number>
}

export interface NotificationReadSummary {
  totalCount: number
  readCount: number
  averageMs: number | null
}

export interface NotificationReadAnalytics {
  summary: NotificationReadSummary
  byDay: (NotificationReadSummary & { day: string })[]
}
