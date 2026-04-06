import { defineStore } from 'pinia'
import type { UserInfo } from '@/types'
import storage from '@/utils/storage'
import { login as authLogin, logout, getUserInfo } from '@/api/auth'

interface UserState {
  token: string | null
  userInfo: UserInfo | null
  permissions: string[]
}

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', {
  state: (): UserState => {
    // 读取存储的数据
    const token = storage.get<string>('token')
    const userInfo = storage.get<UserInfo>('userInfo')

    // 数据验证：如果有token但没有userInfo，说明数据不完整，清除所有数据
    if (token && !userInfo) {
      storage.remove('token')
      storage.remove('userInfo')
      return {
        token: null,
        userInfo: null,
        permissions: []
      }
    }

    // 如果有userInfo，恢复permissions
    const permissions = userInfo?.permissions || []

    return {
      token: token || null,
      userInfo: userInfo || null,
      permissions
    }
  },

  getters: {
    /**
     * 是否已登录（需要同时有token和userInfo）
     */
    isLogin: (state) => {
      return !!(state.token && state.userInfo)
    },

    /**
     * 是否有权限
     */
    hasPermission: (state) => (permission: string | string[]) => {
      // 未登录视为无权限
      if (!state.token || !state.userInfo) {
        return false
      }

      // 如果没有权限数据，默认无权限
      if (!state.permissions || state.permissions.length === 0) {
        return false
      }

      // 超级管理员
      if (state.permissions.includes('*')) {
        return true
      }

      const perms = Array.isArray(permission) ? permission : [permission]
      return perms.some(p => state.permissions.includes(p))
    },

    /**
     * 用户角色
     */
    role: (state) => state.userInfo?.roleName || '',

    /**
     * 是否是超级管理员
     */
    isSuperAdmin: (state) => state.userInfo?.roleId === 'role_super_admin',

    /**
     * 是否是管理员（超管或店长）
     */
    isAdmin: (state) =>
      ['role_super_admin', 'role_manager'].includes(state.userInfo?.roleId || '')
  },

  actions: {
    /**
     * 设置Token
     */
    setToken(token: string) {
      this.token = token
      storage.set('token', token)
    },

    /**
     * 设置用户信息
     */
    setUserInfo(userInfo: UserInfo) {
      this.userInfo = userInfo
      this.permissions = userInfo.permissions || []
      storage.set('userInfo', userInfo)
    },

    /**
     * 登录
     */
    async login(username: string, password: string) {
      const response = await authLogin({ username, password })

      // 保存token和用户信息
      this.setToken(response.token)
      this.setUserInfo(response.userInfo)

      return response
    },

    /**
     * 登出
     */
    async logout() {
      try {
        // 调用登出接口
        await logout()
      } catch (error) {
        // 忽略错误，继续清除本地数据
      } finally {
        this.token = null
        this.userInfo = null
        this.permissions = []
        storage.remove('token')
        storage.remove('userInfo')
      }
    },

    /**
     * 获取用户信息
     */
    async getUserInfo() {
      // Token不存在，无需获取
      if (!this.token) {
        return
      }

      // 调用获取用户信息接口
      const userInfo = await getUserInfo()
      this.setUserInfo(userInfo)
    },

    /**
     * 清除认证信息（不调用后端接口）
     */
    clearAuth() {
      this.token = null
      this.userInfo = null
      this.permissions = []
      storage.remove('token')
      storage.remove('userInfo')
    }
  }
})
