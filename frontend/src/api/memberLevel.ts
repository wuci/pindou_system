import { http } from '@/utils/request'

/**
 * 会员等级信息
 */
export interface MemberLevelInfo {
  id: number
  name: string
  minAmount: number
  maxAmount: number | null
  discountRate: number
  sort: number
  createdAt: number
  updatedAt: number
}

/**
 * 创建会员等级参数
 */
export interface CreateMemberLevelParams {
  name: string
  minAmount: number
  maxAmount: number | null
  discountRate: number
  sort: number
}

/**
 * 更新会员等级参数
 */
export interface UpdateMemberLevelParams {
  name?: string
  minAmount?: number
  maxAmount?: number | null
  discountRate?: number
  sort?: number
}

/**
 * 获取所有会员等级
 */
export const getMemberLevelList = () => {
  return http.get<MemberLevelInfo[]>('/member-levels')
}

/**
 * 获取会员等级详情
 */
export const getMemberLevelDetail = (id: number) => {
  return http.get<MemberLevelInfo>(`/member-levels/${id}`)
}

/**
 * 创建会员等级
 */
export const createMemberLevel = (data: CreateMemberLevelParams) => {
  return http.post<{ id: number }>('/member-levels', data)
}

/**
 * 更新会员等级
 */
export const updateMemberLevel = (id: number, data: UpdateMemberLevelParams) => {
  return http.put(`/member-levels/${id}`, data)
}

/**
 * 删除会员等级
 */
export const deleteMemberLevel = (id: number) => {
  return http.delete(`/member-levels/${id}`)
}

/**
 * 初始化默认会员等级
 */
export const initDefaultMemberLevels = () => {
  return http.post('/member-levels/init')
}
