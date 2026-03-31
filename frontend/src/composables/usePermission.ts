import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * 权限检查 Hook
 */
export function usePermission() {
  const userStore = useUserStore()

  /**
   * 是否拥有指定权限（满足任意一个即可）
   */
  const hasPermission = (permission: string | string[]): boolean => {
    return userStore.hasPermission(permission)
  }

  /**
   * 是否拥有所有指定权限
   */
  const hasAllPermissions = (permissions: string[]): boolean => {
    if (!userStore.isLogin) {
      return false
    }

    if (!userStore.permissions || userStore.permissions.length === 0) {
      return false
    }

    // 超级管理员
    if (userStore.permissions.includes('*')) {
      return true
    }

    return permissions.every(p => userStore.permissions.includes(p))
  }

  /**
   * 是否拥有任意一个指定权限
   */
  const hasAnyPermission = (permissions: string[]): boolean => {
    return hasPermission(permissions)
  }

  /**
   * 当前用户权限列表
   */
  const permissions = computed(() => userStore.permissions)

  /**
   * 是否是超级管理员
   */
  const isSuperAdmin = computed(() => userStore.isSuperAdmin)

  /**
   * 是否是管理员（超管或店长）
   */
  const isAdmin = computed(() => userStore.isAdmin)

  /**
   * 用户角色
   */
  const role = computed(() => userStore.role)

  return {
    hasPermission,
    hasAllPermissions,
    hasAnyPermission,
    permissions,
    isSuperAdmin,
    isAdmin,
    role
  }
}
