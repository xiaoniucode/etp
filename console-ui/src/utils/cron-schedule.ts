import { $t } from '@/locales'

export type CronScheduleType =
  | 'daily'
  | 'everyNDays'
  | 'hourly'
  | 'everyNHours'
  | 'everyNMinutes'
  | 'weekly'
  | 'monthly'
  | 'everyNSeconds'
  | 'custom'

export interface CronSchedule {
  type: CronScheduleType
  minute?: number
  hour?: number
  dayOfMonth?: number
  dayOfWeek?: number
  interval?: number
  customExpression?: string
}

const WEEKDAY_I18N_KEYS = ['mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun'] as const

export function getCronScheduleTypeOptions(): { label: string; value: CronScheduleType }[] {
  return [
    { label: $t('orbien.cron.type.daily'), value: 'daily' },
    { label: $t('orbien.cron.type.everyNDays'), value: 'everyNDays' },
    { label: $t('orbien.cron.type.hourly'), value: 'hourly' },
    { label: $t('orbien.cron.type.everyNHours'), value: 'everyNHours' },
    { label: $t('orbien.cron.type.everyNMinutes'), value: 'everyNMinutes' },
    { label: $t('orbien.cron.type.weekly'), value: 'weekly' },
    { label: $t('orbien.cron.type.monthly'), value: 'monthly' },
    { label: $t('orbien.cron.type.everyNSeconds'), value: 'everyNSeconds' },
    { label: $t('orbien.cron.type.custom'), value: 'custom' }
  ]
}

export function getWeekdayOptions(): { label: string; value: number }[] {
  return WEEKDAY_I18N_KEYS.map((key, index) => ({
    label: $t(`orbien.cron.weekday.${key}`),
    value: index + 1
  }))
}

const WEEKDAY_TO_CRON = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']

function pad(value?: number): string {
  return String(value ?? 0).padStart(2, '0')
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value))
}

function uiDayToCron(dayOfWeek: number): string {
  return WEEKDAY_TO_CRON[clamp(dayOfWeek, 1, 7) - 1]
}

function cronDayToUi(dayOfWeek: string): number {
  const named: Record<string, number> = {
    MON: 1,
    TUE: 2,
    WED: 3,
    THU: 4,
    FRI: 5,
    SAT: 6,
    SUN: 7
  }
  const upper = dayOfWeek.toUpperCase()
  if (named[upper]) {
    return named[upper]
  }
  const numeric = Number(dayOfWeek)
  if (numeric === 0 || numeric === 7) {
    return 7
  }
  if (numeric >= 1 && numeric <= 6) {
    return numeric
  }
  return 1
}

function parsePositiveInt(value: string, fallback = 0): number {
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) ? parsed : fallback
}

function getWeekdayLabel(dayOfWeek?: number): string {
  const index = clamp(dayOfWeek ?? 1, 1, 7) - 1
  return $t(`orbien.cron.weekday.${WEEKDAY_I18N_KEYS[index]}`)
}

export function createDefaultSchedule(type: CronScheduleType): CronSchedule {
  switch (type) {
    case 'everyNDays':
      return { type, hour: 1, minute: 30, interval: 1 }
    case 'hourly':
      return { type, minute: 0 }
    case 'everyNHours':
      return { type, minute: 0, interval: 1 }
    case 'everyNMinutes':
      return { type, interval: 5 }
    case 'weekly':
      return { type, dayOfWeek: 1, hour: 1, minute: 30 }
    case 'monthly':
      return { type, dayOfMonth: 1, hour: 1, minute: 30 }
    case 'everyNSeconds':
      return { type, interval: 30 }
    case 'custom':
      return { type, customExpression: '0 0 1 * * ?' }
    case 'daily':
    default:
      return { type: 'daily', hour: 1, minute: 30 }
  }
}

export function buildCronExpression(schedule: CronSchedule): string {
  switch (schedule.type) {
    case 'daily':
      return `0 ${clamp(schedule.minute ?? 0, 0, 59)} ${clamp(schedule.hour ?? 0, 0, 23)} * * ?`
    case 'everyNDays':
      return `0 ${clamp(schedule.minute ?? 0, 0, 59)} ${clamp(schedule.hour ?? 0, 0, 23)} 1/${clamp(schedule.interval ?? 1, 1, 31)} * ?`
    case 'hourly':
      return `0 ${clamp(schedule.minute ?? 0, 0, 59)} * * * ?`
    case 'everyNHours':
      return `0 ${clamp(schedule.minute ?? 0, 0, 59)} */${clamp(schedule.interval ?? 1, 1, 23)} * * ?`
    case 'everyNMinutes':
      return `0 */${clamp(schedule.interval ?? 1, 1, 59)} * * * ?`
    case 'weekly':
      return `0 ${clamp(schedule.minute ?? 0, 0, 59)} ${clamp(schedule.hour ?? 0, 0, 23)} ? * ${uiDayToCron(schedule.dayOfWeek ?? 1)}`
    case 'monthly':
      return `0 ${clamp(schedule.minute ?? 0, 0, 59)} ${clamp(schedule.hour ?? 0, 0, 23)} ${clamp(schedule.dayOfMonth ?? 1, 1, 31)} * ?`
    case 'everyNSeconds':
      return `*/${clamp(schedule.interval ?? 1, 1, 59)} * * * * ?`
    case 'custom':
      return schedule.customExpression?.trim() || '0 0 1 * * ?'
    default:
      return '0 0 1 * * ?'
  }
}

export function parseCronExpression(cron: string): CronSchedule {
  const expression = cron?.trim()
  if (!expression) {
    return createDefaultSchedule('daily')
  }

  const parts = expression.split(/\s+/)
  if (parts.length !== 6) {
    return { type: 'custom', customExpression: expression }
  }

  const [sec, min, hour, dom, month, dow] = parts

  if (sec.startsWith('*/') && min === '*' && hour === '*' && dom === '*' && month === '*' && dow === '?') {
    return { type: 'everyNSeconds', interval: parsePositiveInt(sec.slice(2), 1) }
  }

  if (sec === '0' && min.startsWith('*/') && hour === '*' && dom === '*' && month === '*' && dow === '?') {
    return { type: 'everyNMinutes', interval: parsePositiveInt(min.slice(2), 1) }
  }

  if (sec === '0' && !min.includes('/') && hour.startsWith('*/') && dom === '*' && month === '*' && dow === '?') {
    return {
      type: 'everyNHours',
      minute: parsePositiveInt(min, 0),
      interval: parsePositiveInt(hour.slice(2), 1)
    }
  }

  if (sec === '0' && !min.includes('/') && hour === '*' && dom === '*' && month === '*' && dow === '?') {
    return { type: 'hourly', minute: parsePositiveInt(min, 0) }
  }

  if (sec === '0' && dom === '?' && month === '*' && dow !== '?' && dow !== '*') {
    return {
      type: 'weekly',
      minute: parsePositiveInt(min, 0),
      hour: parsePositiveInt(hour, 0),
      dayOfWeek: cronDayToUi(dow)
    }
  }

  if (sec === '0' && month === '*' && dow === '?' && dom !== '*' && !dom.includes('/')) {
    return {
      type: 'monthly',
      minute: parsePositiveInt(min, 0),
      hour: parsePositiveInt(hour, 0),
      dayOfMonth: parsePositiveInt(dom, 1)
    }
  }

  if (sec === '0' && dom.startsWith('1/') && month === '*' && dow === '?') {
    return {
      type: 'everyNDays',
      minute: parsePositiveInt(min, 0),
      hour: parsePositiveInt(hour, 0),
      interval: parsePositiveInt(dom.slice(2), 1)
    }
  }

  if (
    sec === '0' &&
    !min.includes('/') &&
    !hour.includes('/') &&
    dom === '*' &&
    month === '*' &&
    dow === '?'
  ) {
    return {
      type: 'daily',
      minute: parsePositiveInt(min, 0),
      hour: parsePositiveInt(hour, 0)
    }
  }

  return { type: 'custom', customExpression: expression }
}

export function describeCronSchedule(schedule: CronSchedule): string {
  switch (schedule.type) {
    case 'daily':
      return $t('orbien.cron.preview.daily', {
        hour: pad(schedule.hour),
        minute: pad(schedule.minute)
      })
    case 'everyNDays':
      return $t('orbien.cron.preview.everyNDays', {
        interval: schedule.interval ?? 1,
        hour: pad(schedule.hour),
        minute: pad(schedule.minute)
      })
    case 'hourly':
      return $t('orbien.cron.preview.hourly', { minute: schedule.minute ?? 0 })
    case 'everyNHours':
      return $t('orbien.cron.preview.everyNHours', {
        interval: schedule.interval ?? 1,
        minute: schedule.minute ?? 0
      })
    case 'everyNMinutes':
      return $t('orbien.cron.preview.everyNMinutes', { interval: schedule.interval ?? 1 })
    case 'weekly':
      return $t('orbien.cron.preview.weekly', {
        weekday: getWeekdayLabel(schedule.dayOfWeek),
        hour: pad(schedule.hour),
        minute: pad(schedule.minute)
      })
    case 'monthly':
      return $t('orbien.cron.preview.monthly', {
        day: schedule.dayOfMonth ?? 1,
        hour: pad(schedule.hour),
        minute: pad(schedule.minute)
      })
    case 'everyNSeconds':
      return $t('orbien.cron.preview.everyNSeconds', { interval: schedule.interval ?? 1 })
    case 'custom':
      return schedule.customExpression || $t('orbien.cron.preview.custom')
    default:
      return $t('orbien.cron.preview.notConfigured')
  }
}

export function describeCronExpression(cron: string): string {
  return describeCronSchedule(parseCronExpression(cron))
}
