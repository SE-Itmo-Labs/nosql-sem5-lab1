export const formatScreeningDate = (value: string): string => new Intl.DateTimeFormat('ru-RU', {
  day: 'numeric',
  month: 'long',
  weekday: 'short',
}).format(new Date(value))

export const formatScreeningTime = (value: string): string => new Intl.DateTimeFormat('ru-RU', {
  hour: '2-digit',
  minute: '2-digit',
}).format(new Date(value))

export const formatPrice = (value: number): string => new Intl.NumberFormat('ru-RU', {
  style: 'currency',
  currency: 'RUB',
  maximumFractionDigits: 0,
}).format(value)
