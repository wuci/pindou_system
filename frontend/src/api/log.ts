import { http } from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

/**
 * 操作日志信息
 */
export interface LogInfo {
  id: string
  userId: string
  username: string
  action: string
  actionName: string
  content: string
  targetType: string | null
  targetId: string | null
  ip: string
  executeTime: number | null
  createdAt: number
}

/**
 * 日志查询参数
 */
export interface LogQueryParams extends PageParams {
  userId?: string
  action?: string
  startTime?: number
  endTime?: number
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
export const exportLogs = (params: LogQueryParams) => {
  return http.get<Blob>('/logs/export', {
    params,
    responseType: 'blob'
  })
}
