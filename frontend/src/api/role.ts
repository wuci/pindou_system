import { http } from '@/utils/request'
import type { PageResult, PageParams } from '@/types'
import type { PermissionResponse } from './permission'

// 重新导出权限类型，方便使用
export type { PermissionResponse }

/**
 * 角色信息
 */
export interface RoleInfo {
  id: string
  name: string
  code: string
  permissions: string[]
  sort: number
  status: number
  isBuiltIn: number
  description: string
  createdAt: number
  updatedAt: number
}

/**
 * 角色查询参数
 */
export interface RoleQueryParams extends PageParams {
  name?: string
  code?: string
  status?: number
}

/**
 * 创建角色参数
 */
export interface CreateRoleParams {
  name: string
  code: string
  permissions?: string[]
  sort?: number
  status?: number
  description?: string
}

/**
 * 更新角色参数
 */
export interface UpdateRoleParams {
  id: string
  name?: string
  permissions?: string[]
  sort?: number
  status?: number
  description?: string
}

/**
 * 获取角色列表（分页）
 */
export const getRoleList = (params: RoleQueryParams) => {
  return http.get<PageResult<RoleInfo>>('/roles', { params })
}

/**
 * 获取所有角色（不分页）
 */
export const getAllRoles = () => {
  return http.get<RoleInfo[]>('/roles/all')
}

/**
 * 获取角色详情
 */
export const getRoleDetail = (id: string) => {
  return http.get<RoleInfo>(`/roles/${id}`)
}

/**
 * 创建角色
 */
export const createRole = (data: CreateRoleParams) => {
  return http.post<string>('/roles', data)
}

/**
 * 更新角色
 */
export const updateRole = (data: UpdateRoleParams) => {
  return http.put<boolean>('/roles', data)
}

/**
 * 删除角色
 */
export const deleteRole = (id: string) => {
  return http.delete<boolean>(`/roles/${id}`)
}

/**
 * 获取角色权限列表
 */
export const getRolePermissions = (id: string) => {
  return http.get<string[]>(`/roles/${id}/permissions`)
}
