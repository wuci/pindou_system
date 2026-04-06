import { http } from '@/utils/request'

/**
 * 折扣信息
 */
export interface DiscountInfo {
  id: string
  name: string
  type: number
  typeName: string
  discountRate: number
  minAmount: number | null
  maxDiscount: number | null
  memberLevelId: number | null
  memberLevelName: string | null
  startTime: number | null
  endTime: number | null
  status: number
  sort: number
  description: string | null
  createdAt: number
  updatedAt: number
}

/**
 * 折扣查询参数
 */
export interface DiscountQueryParams {
  page?: number
  pageSize?: number
  name?: string
  type?: number
  status?: number
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/**
 * 创建折扣参数
 */
export interface CreateDiscountParams {
  name: string
  type: number
  discountRate: number
  minAmount?: number
  maxDiscount?: number
  memberLevelId?: number
  startTime?: number
  endTime?: number
  status: number
  sort?: number
  description?: string
}

/**
 * 更新折扣参数
 */
export interface UpdateDiscountParams {
  name?: string
  type?: number
  discountRate?: number
  minAmount?: number
  maxDiscount?: number
  memberLevelId?: number
  startTime?: number
  endTime?: number
  status?: number
  sort?: number
  description?: string
}

/**
 * 计算折扣参数
 */
export interface CalculateDiscountParams {
  amount: number
  memberLevelId?: number | null
}

/**
 * 计算折扣结果
 */
export interface CalculateDiscountResult {
  originalAmount: number
  discountRate: number
  discountAmount: number
  finalAmount: number
  appliedDiscountName: string
}

/**
 * 获取折扣列表（分页）
 */
export const getDiscountList = (params: DiscountQueryParams) => {
  return http.get<PageResult<DiscountInfo>>('/discounts', { params })
}

/**
 * 获取所有启用的折扣
 */
export const getActiveDiscounts = () => {
  return http.get<DiscountInfo[]>('/discounts/active')
}

/**
 * 创建折扣
 */
export const createDiscount = (data: CreateDiscountParams) => {
  return http.post('/discounts', data)
}

/**
 * 更新折扣
 */
export const updateDiscount = (id: string, data: UpdateDiscountParams) => {
  return http.put(`/discounts/${id}`, data)
}

/**
 * 删除折扣
 */
export const deleteDiscount = (id: string) => {
  return http.delete(`/discounts/${id}`)
}

/**
 * 更新折扣状态
 */
export const updateDiscountStatus = (id: string, status: number) => {
  return http.put(`/discounts/${id}/status`, null, { params: { status } })
}

/**
 * 计算订单折扣
 */
export const calculateDiscount = (data: CalculateDiscountParams) => {
  return http.post<CalculateDiscountResult>('/discounts/calculate', data)
}

/**
 * 根据折扣ID计算折扣
 */
export const calculateDiscountById = (discountId: string, amount: number, memberId?: number) => {
  return http.post<CalculateDiscountResult>('/discounts/calculate-by-id', {
    discountId,
    amount,
    memberId
  })
}
