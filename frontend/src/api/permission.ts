import { http } from '@/utils/request'

/**
 * 权限响应
 */
export interface PermissionResponse {
  id: string
  parentId: string
  permissionKey: string
  permissionName: string
  permissionType: string
  icon?: string
  path?: string
  sort: number
  status: number
  isBuiltIn: number
  description?: string
  createdAt: number
  updatedAt: number
  children?: PermissionResponse[]
}

/**
 * 创建权限请求
 */
export interface CreatePermissionParams {
  parentId: string
  permissionKey: string
  permissionName: string
  permissionType: string
  icon?: string
  path?: string
  sort: number
  status?: number
  description?: string
}

/**
 * 更新权限请求
 */
export interface UpdatePermissionParams {
  id: string
  permissionName: string
  icon?: string
  path?: string
  sort: number
  status?: number
  description?: string
}

/**
 * 获取权限树
 */
export const getPermissionTree = () => {
  return http.get<PermissionResponse[]>('/permissions/tree')
}

/**
 * 获取所有权限列表
 */
export const getAllPermissions = () => {
  return http.get<PermissionResponse[]>('/permissions/all')
}

/**
 * 获取权限详情
 */
export const getPermissionDetail = (id: string) => {
  return http.get<PermissionResponse>(`/permissions/${id}`)
}

/**
 * 创建权限
 */
export const createPermission = (data: CreatePermissionParams) => {
  return http.post<string>('/permissions', data)
}

/**
 * 更新权限
 */
export const updatePermission = (data: UpdatePermissionParams) => {
  return http.put<boolean>('/permissions', data)
}

/**
 * 删除权限
 */
export const deletePermission = (id: string) => {
  return http.delete<boolean>(`/permissions/${id}`)
}
