import { http } from '@/utils/request'

/**
 * 规则分类枚举
 */
export enum RuleCategory {
  PACKAGES = 'packages',  // 套餐规则
  SERVICES = 'services',  // 增值服务
  SAFETY = 'safety',      // 安全须知
  OTHER = 'other'         // 其他规定
}

/**
 * 规则类型枚举
 */
export enum RuleType {
  TABLE = 'table',        // 表格数据
  LIST = 'list',          // 列表
  WARNING = 'warning',    // 警告框
  SPECIAL = 'special'     // 特色服务
}

/**
 * 店铺规则响应
 */
export interface StoreRule {
  id: string
  category: string
  title: string | null
  content: string
  ruleType: string | null
  sortOrder: number
  isEnabled: boolean
  createdAt: number
  updatedAt: number | null
}

/**
 * 店铺规则请求
 */
export interface StoreRuleRequest {
  id?: string
  category: string
  title?: string
  content: string
  ruleType?: string
  sortOrder: number
  isEnabled?: boolean
}

/**
 * 套餐表格数据项
 */
export interface PackageItem {
  name: string
  price: string
  content: string
  suitable: string
  duration: string
}

/**
 * 服务表格数据项
 */
export interface ServiceItem {
  name: string
  price: string
  description: string
}

/**
 * 根据分类获取规则列表
 */
export const getRulesByCategory = (category?: string) => {
  return http.get<StoreRule[]>('/store-rules', {
    params: category ? { category } : undefined
  })
}

/**
 * 根据ID获取规则
 */
export const getRuleById = (id: string) => {
  return http.get<StoreRule>(`/store-rules/${id}`)
}

/**
 * 创建规则
 */
export const createRule = (data: StoreRuleRequest) => {
  return http.post<StoreRule>('/store-rules', data)
}

/**
 * 更新规则
 */
export const updateRule = (data: StoreRuleRequest) => {
  return http.put<StoreRule>('/store-rules', data)
}

/**
 * 删除规则
 */
export const deleteRule = (id: string) => {
  return http.delete<boolean>(`/store-rules/${id}`)
}

/**
 * 切换规则启用状态
 */
export const toggleRuleEnabled = (id: string) => {
  return http.put<boolean>(`/store-rules/${id}/toggle`)
}
