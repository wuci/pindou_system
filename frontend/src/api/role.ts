import { http } from '@/utils/request'

/**
 * 角色信息
 */
export interface RoleInfo {
  id: string
  name: string
  permissions: string[]
  isSystem: boolean
  description: string
}

/**
 * 创建角色
 */
export interface CreateRoleParams {
  name: string
  permissions: string[]
  description?: string
}

/**
 * 更新角色
 */
export interface UpdateRoleParams {
  name?: string
  permissions?: string[]
  description?: string
}

/**
 * 获取角色列表
 */
export const getRoleList = () => {
  return http.get<RoleInfo[]>('/roles')
}

/**
 * 新增角色
 */
export const createRole = (data: CreateRoleParams) => {
  return http.post('/roles', data)
}

/**
 * 编辑角色
 */
export const updateRole = (id: string, data: UpdateRoleParams) => {
  return http.put(`/roles/${id}`, data)
}

/**
 * 删除角色
 */
export const deleteRole = (id: string) => {
  return http.delete(`/roles/${id}`)
}
