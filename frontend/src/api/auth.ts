import { http } from '@/utils/request'

/**
 * 登录接口
 */
export const login = (data: {
  username: string
  password: string
  remember?: boolean
}) => {
  return http.post<{
    token: string
    userInfo: {
      id: string
      username: string
      nickname: string
      roleId: string
      roleName: string
      permissions: string[]
      avatar: string | null
    }
  }>('/auth/login', data)
}

/**
 * 登出接口
 */
export const logout = () => {
  return http.post('/auth/logout')
}

/**
 * 验证Token
 */
export const checkToken = () => {
  return http.get('/auth/check')
}

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
  return http.get<{
    id: string
    username: string
    nickname: string
    roleId: string
    roleName: string
    permissions: string[]
    avatar: string | null
  }>('/auth/userinfo')
}
