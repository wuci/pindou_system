import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: {
          title: '工作台',
          icon: 'Odometer',
          permission: 'dashboard:view'
        }
      },
      {
        path: 'tables',
        name: 'TableManagement',
        component: () => import('@/views/TableManagement/index.vue'),
        meta: {
          title: '桌台管理',
          icon: 'Grid',
          permission: 'table:view'
        }
      },
      {
        path: 'orders',
        name: 'OrderManagement',
        component: () => import('@/views/OrderManagement/index.vue'),
        meta: {
          title: '订单管理',
          icon: 'Document',
          permission: 'order:view'
        }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/Report/index.vue'),
        meta: {
          title: '数据统计',
          icon: 'DataAnalysis',
          permission: 'statistics:view'
        }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          permission: 'user:view'
        }
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('@/views/RoleManagement/index.vue'),
        meta: {
          title: '角色管理',
          icon: 'Lock',
          permission: 'role:manage'
        }
      },
      {
        path: 'logs',
        name: 'OperationLog',
        component: () => import('@/views/OperationLog/index.vue'),
        meta: {
          title: '操作日志',
          icon: 'Document',
          permission: 'log:view'
        }
      },
      {
        path: 'members',
        name: 'MemberManagement',
        component: () => import('@/views/MemberManagement/index.vue'),
        meta: {
          title: '会员管理',
          icon: 'User',
          permission: 'member:view'
        }
      },
      {
        path: 'member-levels',
        name: 'MemberLevelManagement',
        component: () => import('@/views/MemberLevelManagement/index.vue'),
        meta: {
          title: '会员等级',
          icon: 'Star',
          permission: 'member:level'
        }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings/index.vue'),
        meta: {
          title: '系统设置',
          icon: 'Tools',
          permission: 'system:view'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound/index.vue'),
    meta: {
      title: '页面不存在'
    }
  }
]

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * 检查是否有指定模块下的任意权限
 * @param permissions 用户权限列表
 * @param permission 路由要求的权限
 * @returns 是否有权限
 */
const checkModulePermission = (permissions: string[], permission: string): boolean => {
  // 超级管理员
  if (permissions.includes('*')) {
    return true
  }

  // 先检查精确匹配
  if (permissions.includes(permission)) {
    return true
  }

  // 如果要求的权限是 xxx:view 格式，检查是否有该模块下的任意权限
  // 例如：table:view -> 检查是否有 table: 开头的任意权限
  if (permission.endsWith(':view')) {
    const prefix = permission.substring(0, permission.lastIndexOf(':') + 1)
    return permissions.some(p => p.startsWith(prefix))
  }

  return false
}

/**
 * 获取用户有权访问的第一个路由路径
 * @param permissions 用户权限列表
 * @returns 有权访问的路由路径，如果没有则返回空字符串
 */
const getFirstAccessibleRoute = (permissions: string[]): string => {
  // 超级管理员直接返回工作台
  if (permissions.includes('*')) {
    return '/dashboard'
  }

  // 定义路由优先级顺序
  const routeOrder: Array<{ path: string; permission: string }> = [
    { path: '/dashboard', permission: 'dashboard:view' },
    { path: '/tables', permission: 'table:view' },
    { path: '/orders', permission: 'order:view' },
    { path: '/reports', permission: 'statistics:view' },
    { path: '/users', permission: 'user:view' },
    { path: '/roles', permission: 'role:view' },
    { path: '/logs', permission: 'log:view' },
    { path: '/members', permission: 'member:view' },
    { path: '/member-levels', permission: 'member:level' },
    { path: '/settings', permission: 'system:view' }
  ]

  // 按优先级查找第一个有权访问的路由
  for (const route of routeOrder) {
    if (checkModulePermission(permissions, route.permission)) {
      return route.path
    }
  }

  return ''
}

// 防止无限跳转的标志
let isRedirecting = false

/**
 * 路由守卫
 */
router.beforeEach(async (to, from, next) => {
  try {
    // 如果正在重定向，直接放行，避免无限循环
    if (isRedirecting) {
      next()
      return
    }

    const userStore = useUserStore()

    // 设置页面标题
    const appTitle = import.meta.env.VITE_APP_TITLE || '拼豆计时系统'
    document.title = to.meta.title
      ? `${to.meta.title} - ${appTitle}`
      : appTitle

    // 1. 登录页处理 - 直接放行
    if (to.path === '/login') {
      next()
      return
    }

    // 2. 未登录处理 - 直接跳转登录页
    if (to.meta.requiresAuth !== false && !userStore.isLogin) {
      isRedirecting = true
      next({ path: '/login', query: { redirect: to.fullPath } })
      setTimeout(() => { isRedirecting = false }, 100)
      return
    }

    // 3. 已登录但权限为空 - 视为数据不完整，清除登录状态
    if (to.meta.requiresAuth !== false && userStore.isLogin && (!userStore.permissions || userStore.permissions.length === 0)) {
      userStore.clearAuth()
      isRedirecting = true
      next({ path: '/login' })
      setTimeout(() => { isRedirecting = false }, 100)
      return
    }

    // 4. 根路径重定向处理
    if (to.path === '/') {
      if (userStore.isLogin) {
        const firstRoute = getFirstAccessibleRoute(userStore.permissions)
        if (firstRoute) {
          isRedirecting = true
          next({ path: firstRoute, replace: true })
          setTimeout(() => { isRedirecting = false }, 100)
          return
        } else {
          // 没有任何权限，清除登录状态
          userStore.clearAuth()
          isRedirecting = true
          next({ path: '/login' })
          setTimeout(() => { isRedirecting = false }, 100)
          return
        }
      } else {
        isRedirecting = true
        next({ path: '/login' })
        setTimeout(() => { isRedirecting = false }, 100)
        return
      }
    }

    // 5. 检查路由权限
    if (to.meta.requiresAuth !== false && to.meta.permission) {
      const requiredPermission = to.meta.permission as string
      if (!checkModulePermission(userStore.permissions, requiredPermission)) {
        // 无权限时，查找第一个有权访问的页面
        const firstRoute = getFirstAccessibleRoute(userStore.permissions)
        if (firstRoute) {
          ElMessage.warning('您没有访问该页面的权限')
          isRedirecting = true
          next({ path: firstRoute })
          setTimeout(() => { isRedirecting = false }, 100)
          return
        } else {
          // 没有任何权限，清除登录状态
          ElMessage.warning('您的账号没有任何访问权限，请联系管理员')
          userStore.clearAuth()
          isRedirecting = true
          next({ path: '/login' })
          setTimeout(() => { isRedirecting = false }, 100)
          return
        }
      }
    }

    next()
  } catch (error) {
    // 发生错误时跳转到登录页
    isRedirecting = true
    next({ path: '/login' })
    setTimeout(() => { isRedirecting = false }, 100)
  }
})

export default router
