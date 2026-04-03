import { http } from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

/**
 * 订单信息
 */
export interface OrderInfo {
  id: string
  orderNo: string  // 订单编号
  tableId: number
  tableName: string
  startTime: number
  endTime: number | null
  duration: number
  pauseDuration: number
  presetDuration: number | null
  channel: string  // 订餐渠道
  status: 'active' | 'completed'
  amount: number
  originalAmount?: number  // 原价（折扣前）
  discountAmount?: number  // 折扣金额
  operatorName: string
  paidAt: number | null
  createdAt: number
  memberId?: number | null  // 会员ID
  memberName?: string  // 会员姓名（用于显示）
  memberLevelName?: string  // 会员等级名称
  memberDiscountRate?: number  // 会员折扣率
  paymentMethod?: string  // 支付方式
  balanceAmount?: number  // 余额支付金额
  otherPaymentAmount?: number  // 其他方式支付金额
}

/**
 * 订单查询参数
 */
export interface OrderQueryParams extends PageParams {
  status?: string
  startTime?: number
  endTime?: number
  tableId?: number
  keyword?: string
}

/**
 * 时间线记录
 */
export interface TimeLineItem {
  time: number
  action: string
  description: string
  operator: string
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
  paidAt: number | null
  duration: number
  pauseDuration: number
  actualDuration: number
  presetDuration: number | null
  channel?: string  // 订单渠道
  status: 'active' | 'completed'
  amount: number
  originalAmount?: number  // 原价（折扣前）
  discountAmount?: number  // 折扣金额
  normalAmount: number
  overtimeAmount: number
  operatorId: string
  operatorName: string
  createdAt: number
  updatedAt: number
  memberId?: number | null  // 会员ID
  memberName?: string  // 会员姓名
  memberLevelName?: string  // 会员等级名称
  memberDiscountRate?: number  // 会员折扣率
  paymentMethod?: string  // 支付方式
  balanceAmount?: number  // 余额支付金额
  otherPaymentAmount?: number  // 其他方式支付金额
  timeLine: TimeLineItem[]
}

/**
 * 获取当前订单（分页）
 */
export const getActiveOrders = (params?: PageParams) => {
  return http.get<PageResult<OrderInfo>>('/orders/active', { params })
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
