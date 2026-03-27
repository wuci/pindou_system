import { http } from '@/utils/request'

/**
 * 计费规则
 */
export interface BillingRule {
  type: 'hour' | 'minute'
  pricePerHour: number
  pricePerMinute: number
  overtimeRate: number
}

/**
 * 提醒配置
 */
export interface RemindConfig {
  threshold: number
  soundEnabled: boolean
  repeatInterval: number
  expiringCloseTime: number
  timeoutCloseTime: number
}

/**
 * 系统配置
 */
export interface SystemConfig {
  tableCount: number
  billingRule: BillingRule
  remindConfig: RemindConfig
  sessionTimeout: number
}

/**
 * 获取系统配置
 */
export const getConfig = () => {
  return http.get<SystemConfig>('/config')
}

/**
 * 更新计费规则
 */
export const updateBillingRule = (data: BillingRule) => {
  return http.put('/config/billing', data)
}

/**
 * 更新提醒配置
 */
export const updateRemindConfig = (data: RemindConfig) => {
  return http.put('/config/remind', data)
}
