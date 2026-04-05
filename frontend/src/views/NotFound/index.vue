<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-message">页面不存在</div>
      <div class="error-description">抱歉，您访问的页面不存在或已被删除</div>
      <el-button type="primary" @click="goBack">返回上一页</el-button>
      <el-button @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

/**
 * 返回首页（第一个有权访问的页面）
 */
const goHome = () => {
  if (userStore.isLogin) {
    // 找到第一个有权访问的路由
    const routes = [
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

    for (const route of routes) {
      const hasPermission = userStore.permissions.includes('*') ||
        userStore.permissions.includes(route.permission) ||
        (route.permission.endsWith(':view') && userStore.permissions.some((p: string) => p.startsWith(route.permission.slice(0, -4) + ':')))

      if (hasPermission) {
        router.push(route.path)
        return
      }
    }
  }
  router.push('/login')
}
</script>

<style scoped lang="scss">
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background-color: #f5f5f5;
}

.not-found-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
  margin-bottom: 20px;
}

.error-message {
  font-size: 24px;
  color: #333;
  margin-bottom: 12px;
}

.error-description {
  font-size: 14px;
  color: #999;
  margin-bottom: 30px;
}

.el-button {
  margin: 0 8px;
}
</style>
