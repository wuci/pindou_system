import { http } from '@/utils/request'

/**
 * 单条计费规则
 */
export interface BillingRuleItem {
  hours: number | null  // 时长（小时），null表示不限时
  price: number  // 价格（元）
  unlimited: boolean  // 是否不限时
}

/**
 * 渠道计费规则
 */
export interface ChannelBillingRule {
  channel: 'store' | 'meituan' | 'dianping'  // 渠道代码
  channelName: string  // 渠道名称
  rules: BillingRuleItem[]  // 计费规则列表
}

/**
 * 计费规则配置
 */
export interface BillingRule {
  channels: ChannelBillingRule[]
}

/**
 * 提醒配置
 */
export interface RemindConfig {
  threshold: number
  soundEnabled: 0 | 1
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
 * 获取所有配置
 */
export const getAllConfigs = () => {
  return http.get<Record<string, string>>('/config')
}

/**
 * 获取系统配置
 */
export const getConfig = () => {
  return http.get<SystemConfig>('/config')
}

/**
 * 获取计费规则配置
 */
export const getBillingRuleConfig = () => {
  return http.get<BillingRule>('/config/billing')
}

/**
 * 更新计费规则
 */
export const updateBillingRule = (data: BillingRule) => {
  return http.put<boolean>('/config/billing', data)
}

/**
 * 获取提醒配置
 */
export const getRemindConfig = () => {
  return http.get<string>('/config/remind')
}

/**
 * 更新提醒配置
 */
export const updateRemindConfig = (data: RemindConfig) => {
  return http.put<boolean>('/config/remind', data)
}

/**
 * 获取桌台数量配置
 */
export const getTableCountConfig = () => {
  return http.get<number>('/config/table-count')
}

/**
 * 更新桌台数量配置
 */
export const updateTableCountConfig = (count: number) => {
  return http.put<boolean>('/config/table-count', null, {
    params: { count }
  })
}
