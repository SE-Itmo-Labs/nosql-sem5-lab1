import type { NotificationReadAnalytics, NotificationReadSummary } from '~/types/api'
import type { MockNotification } from './database'

interface ReadCounts {
  totalCount: number
  readCount: number
  totalMs: number
}

const emptyCounts = (): ReadCounts => ({ totalCount: 0, readCount: 0, totalMs: 0 })

const summarize = ({ totalCount, readCount, totalMs }: ReadCounts): NotificationReadSummary => ({
  totalCount,
  readCount,
  averageMs: readCount ? totalMs / readCount : null,
})

export const readAnalytics = (notifications: MockNotification[], days: number): NotificationReadAnalytics => {
  const end = new Date()
  end.setUTCHours(24, 0, 0, 0)
  const start = new Date(end)
  start.setUTCDate(start.getUTCDate() - days)
  const daysWithNotifications = new Map<string, ReadCounts>()
  const totals = emptyCounts()

  for (const notification of notifications) {
    const createdAt = new Date(notification.createdAt)
    if (!(createdAt >= start && createdAt < end)) continue

    const firstRead = (notification.actionLog ?? []).reduce((earliest, action) => {
      const at = action.at ? Date.parse(action.at) : NaN
      return action.type === 'READ' && at >= +createdAt ? Math.min(earliest, at) : earliest
    }, Infinity)
    const day = createdAt.toISOString().slice(0, 10)
    const counts = daysWithNotifications.get(day) ?? emptyCounts()
    for (const group of [counts, totals]) {
      group.totalCount += 1
      if (Number.isFinite(firstRead)) {
        group.readCount += 1
        group.totalMs += firstRead - +createdAt
      }
    }
    daysWithNotifications.set(day, counts)
  }

  return {
    summary: summarize(totals),
    byDay: [...daysWithNotifications.entries()]
      .sort(([left], [right]) => right.localeCompare(left))
      .map(([day, counts]) => ({ day, ...summarize(counts) })),
  }
}
