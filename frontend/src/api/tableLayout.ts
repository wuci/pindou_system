import { http } from '@/utils/request'

/**
 * 桌台布局项
 */
export interface TableLayoutItem {
  id: number
  x: number
  y: number
  width: number
  height: number
}

/**
 * 获取布局配置
 */
export const getLayoutConfig = (categoryId: number) => {
  return http.get<{ categoryId: number; config: string }>(`/table-layout-config/${categoryId}`)
}

/**
 * 保存布局配置
 */
export const saveLayoutConfig = (categoryId: number, config: TableLayoutItem[]) => {
  return http.post('/table-layout-config', {
    categoryId,
    config: JSON.stringify(config)
  })
}
