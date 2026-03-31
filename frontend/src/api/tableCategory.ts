import request from '@/utils/request'

/**
 * 桌台分类响应类型
 */
export interface TableCategoryResponse {
  id: number
  name: string
  icon?: string
  sortOrder?: number
  tableCount: number
  usingCount: number
  createdAt?: string
  remark?: string
}

/**
 * 桌台分类请求类型
 */
export interface TableCategoryRequest {
  id?: number
  name: string
  icon?: string
  sortOrder?: number
  remark?: string
}

/**
 * 获取所有分类
 */
export function getCategories() {
  return request.get<TableCategoryResponse[]>('/table-category/list')
}

/**
 * 创建分类
 */
export function createCategory(data: TableCategoryRequest) {
  return request.post<number>('/table-category/create', data)
}

/**
 * 更新分类
 */
export function updateCategory(data: TableCategoryRequest) {
  return request.post<void>('/table-category/update', data)
}

/**
 * 删除分类
 */
export function deleteCategory(id: number) {
  return request.delete<void>(`/table-category/delete/${id}`)
}

/**
 * 获取分类详情
 */
export function getCategory(id: number) {
  return request.get<TableCategoryResponse>(`/table-category/${id}`)
}
