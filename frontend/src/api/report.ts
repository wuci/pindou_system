import { http } from '@/utils/request'

/**
 * 今日统计
 */
export interface DailyReport {
  revenue: number
  orderCount: number
  usingTableCount: number
  totalTableCount: number
  turnoverRate: number
}

/**
 * 营收趋势数据
 */
export interface TrendReport {
  dates: string[]
  revenues: number[]
  orderCounts: number[]
}

/**
 * 获取今日统计
 */
export const getDailyReport = () => {
  return http.get<DailyReport>('/reports/daily')
}

/**
 * 获取营收趋势
 */
export const getTrendReport = (days: number = 7) => {
  return http.get<TrendReport>('/reports/trend', {
    params: { days }
  })
}
