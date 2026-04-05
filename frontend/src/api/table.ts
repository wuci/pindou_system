import { http } from '@/utils/request'

/**
 * 支付方式枚举
 */
export type PaymentMethod = 'offline' | 'online' | 'balance' | 'combined'

/**
 * 支付方式选项
 */
export interface PaymentMethodOption {
  value: PaymentMethod
  label: string
  description?: string
}

/**
 * 非会员支付方式
 */
export const NON_MEMBER_PAYMENT_METHODS: PaymentMethodOption[] = [
  { value: 'offline', label: '线下支付', description: '店内现场支付' },
  { value: 'online', label: '线上支付', description: '平台订单支付' }
]

/**
 * 会员支付方式
 */
export const MEMBER_PAYMENT_METHODS: PaymentMethodOption[] = [
  { value: 'offline', label: '线下支付', description: '店内现场支付' },
  { value: 'online', label: '线上支付', description: '平台订单支付' },
  { value: 'balance', label: '会员余额', description: '使用会员余额支付' },
  { value: 'combined', label: '组合支付', description: '余额+线下组合支付' }
]

/**
 * 支付方式显示文本映射
 */
export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  offline: '线下支付',
  online: '线上支付',
  balance: '会员余额',
  combined: '组合支付'
}

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
  pauseAccumulated?: number  // 累计暂停时长（秒）
  lastPauseTime?: number | null  // 最后一次暂停时间（毫秒时间戳）
  presetDuration: number | null
  amount: number
  originalAmount?: number  // 原价（折扣前）
  reminded: number
  remindIgnored: number
  createdAt: number | null
  endTime: number | null
  memberId?: number  // 会员ID
  memberName?: string  // 会员姓名
  memberDiscountRate?: number  // 会员折扣率
  memberBalance?: number  // 会员余额
  channel?: string  // 订单渠道
  // 预定相关字段
  reservationStatus?: 'none' | 'reserved'  // 预定状态：none-未预定，reserved-已预定
  reservationEndTime?: number | null  // 预定截止时间（毫秒时间戳）
  reservationName?: string | null  // 预订人姓名
  reservationPhone?: string | null  // 预订人手机号
  // 支付方式相关字段
  paymentMethod?: PaymentMethod  // 支付方式
  balanceAmount?: number  // 余额支付金额
  otherPaymentAmount?: number  // 其他方式支付金额
}

/**
 * 开始计时参数
 */
export interface StartTableParams {
  presetDuration: number | null
  channel?: string  // 订餐渠道
  memberId?: number  // 会员ID
  remark?: string
  paymentMethod?: PaymentMethod  // 支付方式
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
  balance: number  // 会员余额
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
  paymentMethod?: PaymentMethod  // 支付方式
  balanceAmount?: number  // 余额支付金额
  otherPaymentAmount?: number  // 其他方式支付金额
}

/**
 * 获取桌台列表
 */
export const getTableList = (status?: string, categoryId?: number, name?: string) => {
  return http.get<TableInfo[]>('/tables', {
    params: { status, categoryId, name }
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
  paymentMethod?: PaymentMethod  // 支付方式
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
  memberId?: number  // 会员ID
  paymentMethod?: PaymentMethod  // 支付方式
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

/**
 * 预定参数
 */
export interface ReservationParams {
  reservationEndTime: number  // 预定截止时间（毫秒时间戳）
  reservationName: string  // 预订人姓名
  reservationPhone: string  // 预订人手机号
}

/**
 * 创建桌台预定
 */
export const createReservation = (id: number, data: ReservationParams) => {
  return http.post(`/tables/${id}/reservation`, data)
}

/**
 * 取消桌台预定
 */
export const cancelReservation = (id: number) => {
  return http.delete(`/tables/${id}/reservation`)
}
