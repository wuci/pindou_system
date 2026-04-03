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
    redirect: '/dashboard',
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
    redirect: '/dashboard'
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
 * 路由守卫
 */
router.beforeEach((to, from, next) => {
  try {
    console.log('路由守卫:', to.path, 'from:', from.path, 'isLogin:', useUserStore().isLogin)

    const userStore = useUserStore()

    // 设置页面标题
    const appTitle = import.meta.env.VITE_APP_TITLE || '拼豆计时系统'
    document.title = to.meta.title
      ? `${to.meta.title} - ${appTitle}`
      : appTitle

    // 如果已经是登录页，直接放行
    if (to.path === '/login') {
      next()
      return
    }

    // 检查是否需要登录
    if (to.meta.requiresAuth !== false) {
      if (!userStore.isLogin) {
        console.log('未登录，重定向到登录页')
        // 未登录跳转登录页
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
        return
      }

      // 检查权限（只有已登录用户才检查权限）
      if (to.meta.permission) {
        const requiredPermission = to.meta.permission as string
        if (!checkModulePermission(userStore.permissions, requiredPermission)) {
          console.log('无权限，重定向到首页。需要权限:', requiredPermission, '用户权限:', userStore.permissions)
          // 已登录用户无权限时，提示并跳转到首页
          ElMessage.warning('您没有访问该页面的权限')
          next({ path: '/dashboard' })
          return
        }
      }
    }

    console.log('路由守卫通过，继续导航')
    next()
  } catch (error) {
    console.error('路由守卫错误:', error)
    // 发生错误时跳转到登录页
    next({ path: '/login' })
  }
})

export default router
