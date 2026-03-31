import { http } from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

/**
 * 今日统计数据
 */
export interface DailyStats {
  todayRevenue: number
  activeTableCount: number
  todayOrderCount: number
  turnoverRate: number
  todayDuration: number
}

/**
 * 营收趋势项
 */
export interface TrendItem {
  date: string
  revenue: number
  orderCount: number
}

/**
 * 获取今日统计
 */
export const getDailyStats = () => {
  return http.get<DailyStats>('/reports/daily')
}

/**
 * 获取营收趋势
 */
export const getRevenueTrend = (days: number = 7) => {
  return http.get<TrendItem[]>('/reports/trend', {
    params: { days }
  })
}
