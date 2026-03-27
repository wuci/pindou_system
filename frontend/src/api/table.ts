import { http } from '@/utils/request'
import type { PageResult } from '@/types'

/**
 * 桌台信息
 */
export interface TableInfo {
  id: number
  name: string
  status: 'idle' | 'using' | 'paused'
  currentOrderId: string | null
  startTime: number | null
  duration: number
  pauseDuration: number
  presetDuration: number | null
  amount: number
  reminded: number
  remindIgnored: number
  createdAt: number | null
  endTime: number | null
}

/**
 * 开始计时参数
 */
export interface StartTableParams {
  presetDuration: number | null
  remark?: string
}

/**
 * 暂停计时参数
 */
export interface PauseTableParams {
  remark?: string
}

/**
 * 获取桌台列表
 */
export const getTableList = (status?: string) => {
  return http.get<TableInfo[]>('/tables', {
    params: { status }
  })
}

/**
 * 配置桌台数量
 */
export const configTableCount = (tableCount: number) => {
  return http.put('/tables/config', { tableCount })
}

/**
 * 开始计时
 */
export const startTable = (id: number, data: StartTableParams) => {
  return http.post(`/tables/${id}/start`, data)
}

/**
 * 暂停计时
 */
export const pauseTable = (id: number, data: PauseTableParams) => {
  return http.post(`/tables/${id}/pause`, data)
}

/**
 * 恢复计时
 */
export const resumeTable = (id: number) => {
  return http.post(`/tables/${id}/resume`)
}

/**
 * 结束结账
 */
export const endTable = (id: number) => {
  return http.post(`/tables/${id}/end`)
}

/**
 * 忽略提醒
 */
export const ignoreRemind = (id: number) => {
  return http.post(`/tables/${id}/ignore-remind`)
}
