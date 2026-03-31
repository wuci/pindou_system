import { http } from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

/**
 * 操作日志信息
 */
export interface LogInfo {
  id: string
  module: string
  operation: string
  description: string
  username: string
  method: string
  params: string
  result: string
  duration: number
  ip: string
  status: number
  errorMsg: string
  createdAt: number
}

/**
 * 日志查询参数
 */
export interface LogQueryParams extends PageParams {
  module?: string
  operation?: string
  username?: string
  startTime?: number
  endTime?: number
  status?: number
}

/**
 * 获取操作日志
 */
export const getLogs = (params: LogQueryParams) => {
  return http.get<PageResult<LogInfo>>('/logs', { params })
}

/**
 * 导出日志
 */
export const exportLogs = (params: Omit<LogQueryParams, 'page' | 'pageSize'>) => {
  return http.get<LogInfo[]>('/logs/export', { params })
}
