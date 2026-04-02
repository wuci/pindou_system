import { http } from '@/utils/request'
import type { PageResult } from '@/types'

/**
 * 会员信息
 */
export interface MemberInfo {
  id: number
  name: string
  phone: string
  address: string | null
  totalAmount: number
  balance: number
  levelId: number
  levelName: string
  discountRate: number
  createdAt: number
  updatedAt: number
}

/**
 * 充值请求参数
 */
export interface RechargeParams {
  memberId: number
  amount: number
  paymentMethod: string
  remark?: string
}

/**
 * 充值记录
 */
export interface RechargeRecord {
  id: number
  memberId: number
  memberName: string
  memberPhone: string
  amount: number
  balanceBefore: number
  balanceAfter: number
  paymentMethod: string
  paymentMethodName: string
  remark: string
  operatorId: string
  operatorName: string
  createdAt: number
}

/**
 * 消费记录
 */
export interface ConsumptionRecord {
  id: number
  memberId: number
  memberName: string
  memberPhone: string
  orderId: number
  amount: number
  balanceBefore: number
  balanceAfter: number
  remark: string
  createdAt: number
}

/**
 * 会员查询参数
 */
export interface MemberQueryParams {
  page: number
  pageSize: number
  keyword?: string
}

/**
 * 创建会员参数
 */
export interface CreateMemberParams {
  name: string
  phone: string
  address?: string
}

/**
 * 更新会员参数
 */
export interface UpdateMemberParams {
  name: string
  address?: string
}

/**
 * 获取会员列表（分页）
 */
export const getMemberList = (params: MemberQueryParams) => {
  return http.get<PageResult<MemberInfo>>('/members', { params })
}

/**
 * 搜索会员（用于下拉选择）
 */
export const searchMembers = (keyword: string) => {
  return http.get<MemberInfo[]>('/members/search', { params: { keyword } })
}

/**
 * 获取会员详情
 */
export const getMemberDetail = (id: number) => {
  return http.get<MemberInfo>(`/members/${id}`)
}

/**
 * 创建会员
 */
export const createMember = (data: CreateMemberParams) => {
  return http.post<{ id: number }>('/members', data)
}

/**
 * 更新会员
 */
export const updateMember = (id: number, data: UpdateMemberParams) => {
  return http.put(`/members/${id}`, data)
}

/**
 * 删除会员
 */
export const deleteMember = (id: number) => {
  return http.delete(`/members/${id}`)
}

/**
 * 计算会员折扣
 */
export const calculateDiscount = (memberId: number, originalAmount: number) => {
  return http.post('/members/calculate-discount', { memberId, originalAmount })
}

/**
 * 会员充值
 */
export const recharge = (memberId: number, data: Omit<RechargeParams, 'memberId'>) => {
  return http.post<number>(`/members/${memberId}/recharge`, data)
}

/**
 * 获取会员充值记录
 */
export const getRechargeRecords = (memberId: number, page: number = 1, pageSize: number = 20) => {
  return http.get<PageResult<RechargeRecord>>(`/members/${memberId}/recharge-records`, {
    params: { page, pageSize }
  })
}

/**
 * 获取会员消费记录
 */
export const getConsumptionRecords = (memberId: number, page: number = 1, pageSize: number = 20) => {
  return http.get<PageResult<ConsumptionRecord>>(`/members/${memberId}/consumption-records`, {
    params: { page, pageSize }
  })
}
