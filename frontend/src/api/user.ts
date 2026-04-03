import { http } from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

/**
 * 用户信息
 */
export interface UserInfo {
  id: string
  username: string
  nickname: string
  roleId: string
  roleName: string
  status: number
  lastLoginAt: number
  createdAt: number
}

/**
 * 用户查询参数
 */
export interface UserQueryParams extends PageParams {
  username?: string
  nickname?: string
  roleId?: string
  status?: number
}

/**
 * 创建用户
 */
export interface CreateUserParams {
  username: string
  password: string
  nickname: string
  roleId: string
  status: number
}

/**
 * 更新用户
 */
export interface UpdateUserParams {
  nickname?: string
  roleId?: string
  status?: number
}

/**
 * 获取用户列表
 */
export const getUserList = (params: UserQueryParams) => {
  return http.get<PageResult<UserInfo>>('/users', { params })
}

/**
 * 获取所有用户列表（不分页）
 */
export const getAllUsers = () => {
  return http.get<UserInfo[]>('/users/all')
}

/**
 * 新增用户
 */
export const createUser = (data: CreateUserParams) => {
  return http.post('/users', data)
}

/**
 * 编辑用户
 */
export const updateUser = (id: string, data: UpdateUserParams) => {
  return http.put(`/users/${id}`, data)
}

/**
 * 删除用户
 */
export const deleteUser = (id: string) => {
  return http.delete(`/users/${id}`)
}

/**
 * 重置密码
 */
export const resetPassword = (id: string, password: string) => {
  return http.put(`/users/${id}/reset-password`, { password })
}

/**
 * 修改密码参数
 */
export interface ChangePasswordParams {
  userId: string
  newPassword: string
}

/**
 * 修改密码
 */
export const changePassword = (data: ChangePasswordParams) => {
  return http.put(`/users/${data.userId}/reset-password`, { password: data.newPassword })
}
