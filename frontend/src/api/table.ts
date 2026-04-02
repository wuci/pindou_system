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
  originalAmount?: number  // 原价（折扣前）
  reminded: number
  remindIgnored: number
  createdAt: number | null
  endTime: number | null
  memberName?: string  // 会员姓名
  memberDiscountRate?: number  // 会员折扣率
}

/**
 * 开始计时参数
 */
export interface StartTableParams {
  presetDuration: number | null
  channel?: string  // 订餐渠道
  memberId?: number  // 会员ID
  remark?: string
}

/**
 * 暂停计时参数
 */
export interface PauseTableParams {
  remark?: string
}

/**
 * 会员信息（账单用）
 */
export interface BillMemberInfo {
  id: number
  name: string
  levelName: string
  discountRate: number
  discountAmount: number
  finalAmount: number
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
  originalAmount?: number  // 原价（折扣前）
  amountDetail?: {
    normalAmount: number
    overtimeAmount: number
    totalAmount: number
  }
  member?: BillMemberInfo  // 会员信息
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
 * 结账参数
 */
export interface EndTableParams {
  memberId?: number | null
}

/**
 * 结束结账
 */
export const endTable = (id: number, data?: EndTableParams) => {
  return http.post(`/tables/${id}/end`, data || {})
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

/**
 * 续费参数
 */
export interface ExtendTableParams {
  additionalDuration: number  // 额外时长（秒）
  channel?: string  // 订餐渠道
}

/**
 * 续费时长规则
 */
export interface ExtendRule {
  unlimited: boolean
  minutes?: number
  price?: number
}

/**
 * 续费时长
 */
export const extendTable = (id: number, data: ExtendTableParams) => {
  return http.post(`/tables/${id}/extend`, data)
}
