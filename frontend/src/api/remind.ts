import { http } from '@/utils/request'

/**
 * 提醒信息
 */
export interface RemindInfo {
  tableId: number
  tableName: string
  remindType: string
  remindTypeDesc: string
  startTime: number
  presetDuration: number | null
  usedDuration: number
  remainingDuration: number
  overtimeDuration: number
}

/**
 * 获取提醒列表
 */
export const getReminders = () => {
  return http.get<RemindInfo[]>('/remind')
}

/**
 * 忽略提醒
 */
export const ignoreRemind = (tableId: number) => {
  return http.post<boolean>(`/remind/ignore/${tableId}`)
}
