import { http } from '@/utils/request'
import type { PageResult } from '@/types'

/**
 * 桌台信息
 */
export interface TableInfo {
  id: number
  name: string
  categoryId: number | null
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
 * 账单信息
 */
export interface BillInfo {
  orderId: string
  tableId: number
  tableName: string
  startTime: number
  endTime: number | null
  duration: number
  pauseDuration: number
  actualDuration: number
  presetDuration: number | null
  operatorName: string
  status: string
  amountDetail?: {
    normalAmount: number
    overtimeAmount: number
    totalAmount: number
  }
}

/**
 * 获取桌台列表
 */
export const getTableList = (status?: string, categoryId?: number) => {
  return http.get<TableInfo[]>('/tables', {
    params: { status, categoryId }
  })
}

/**
 * 配置桌台数量
 */
export const configTableCount = (tableCount: number, categoryId: number) => {
  return http.put('/tables/config', { tableCount, categoryId })
}

/**
 * 更新桌台信息
 */
export const updateTable = (id: number, data: { name?: string; categoryId?: number }) => {
  return http.put(`/tables/${id}`, data)
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
 * 获取桌台账单
 */
export const getTableBill = (id: number) => {
  return http.get<BillInfo>(`/tables/${id}/bill`)
}

/**
 * 忽略提醒
 */
export const ignoreRemind = (id: number) => {
  return http.post(`/tables/${id}/ignore-remind`)
}

/**
 * 删除桌台
 */
export const deleteTable = (id: number) => {
  return http.delete(`/tables/${id}`)
}

/**
 * 批量删除桌台
 */
export const batchDeleteTables = (tableIds: number[]) => {
  return http.delete('/tables/batch', { data: { tableIds } })
}
