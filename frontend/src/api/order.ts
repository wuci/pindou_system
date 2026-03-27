import { http } from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

/**
 * 订单信息
 */
export interface OrderInfo {
  id: string
  tableId: number
  tableName: string
  startTime: number
  endTime: number | null
  duration: number
  pauseDuration: number
  presetDuration: number | null
  status: 'active' | 'completed'
  amount: number
  operatorName: string
  paidAt: number | null
  createdAt: number
}

/**
 * 订单查询参数
 */
export interface OrderQueryParams extends PageParams {
  startTime?: number
  endTime?: number
  tableId?: number
}

/**
 * 订单详情
 */
export interface OrderDetail {
  id: string
  tableId: number
  tableName: string
  startTime: number
  endTime: number | null
  duration: number
  pauseDuration: number
  presetDuration: number | null
  status: 'active' | 'completed'
  amount: number
  amountDetail: {
    normalDuration: number
    normalAmount: number
    overtimeDuration: number
    overtimeAmount: number
    totalAmount: number
  }
  operatorName: string
  paidAt: number | null
  createdAt: number
}

/**
 * 获取当前订单
 */
export const getActiveOrders = () => {
  return http.get<OrderInfo[]>('/orders/active')
}

/**
 * 获取历史订单
 */
export const getHistoryOrders = (params: OrderQueryParams) => {
  return http.get<PageResult<OrderInfo>>('/orders/history', { params })
}

/**
 * 获取订单详情
 */
export const getOrderDetail = (id: string) => {
  return http.get<OrderDetail>(`/orders/${id}`)
}

/**
 * 导出订单
 */
export const exportOrders = (params: OrderQueryParams) => {
  return http.get<Blob>('/orders/export', {
    params,
    responseType: 'blob'
  })
}
