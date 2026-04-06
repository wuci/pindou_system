<template>
  <div class="layout-container">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <div class="logo-wrapper">
            <svg class="logo" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
              <!-- 水面波纹（海洋） -->
              <path d="M8 48 Q16 44 24 48 Q32 52 40 48 Q48 44 56 48" fill="none" stroke="url(#waterGradient)" stroke-width="2" opacity="0.4"/>
              <path d="M12 54 Q20 50 28 54 Q36 58 44 54 Q52 50 60 54" fill="none" stroke="url(#waterGradient)" stroke-width="1.5" opacity="0.3"/>
              <path d="M16 58 Q24 55 32 58 Q40 61 48 58" fill="none" stroke="url(#waterGradient)" stroke-width="1" opacity="0.2"/>

              <!-- 岛屿主体 -->
              <path d="M32 50 C22 50 18 40 18 32 C18 22 26 16 32 16 C38 16 46 22 46 32 C46 40 42 50 32 50Z"
                    fill="url(#islandGradient)"/>

              <!-- 岛屿高光（沙滩感） -->
              <path d="M32 20 C36 20 40 22 42 26" stroke="rgba(255,255,255,0.4)" stroke-width="2" fill="none" stroke-linecap="round"/>

              <!-- 岛屿上的小光点（温柔集结） -->
              <circle cx="28" cy="32" r="2.5" fill="#fff" opacity="0.9"/>
              <circle cx="36" cy="30" r="3" fill="#fff" opacity="0.95"/>
              <circle cx="32" cy="38" r="2" fill="#fff" opacity="0.85"/>
              <circle cx="26" cy="26" r="1.5" fill="#fff" opacity="0.8"/>
              <circle cx="38" cy="36" r="1.8" fill="#fff" opacity="0.85"/>

              <!-- 连接光（渡船） -->
              <line x1="28" y1="32" x2="36" y2="30" stroke="#fff" stroke-width="0.8" opacity="0.5"/>
              <line x1="28" y1="32" x2="32" y2="38" stroke="#fff" stroke-width="0.8" opacity="0.4"/>
              <line x1="36" y1="30" x2="32" y2="38" stroke="#fff" stroke-width="0.8" opacity="0.4"/>

              <defs>
                <linearGradient id="waterGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#c5e3ff"/>
                  <stop offset="50%" stop-color="#d4a5ff"/>
                  <stop offset="100%" stop-color="#ffb6d9"/>
                </linearGradient>
                <linearGradient id="islandGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#e8c4e8"/>
                  <stop offset="50%" stop-color="#d4a5ff"/>
                  <stop offset="100%" stop-color="#a78bdf"/>
                </linearGradient>
              </defs>
            </svg>
            <h2>豆屿温柔集</h2>
          </div>
        </div>
        <div class="header-right">
          <!-- 提醒铃铛 -->
          <div class="remind-bell" @click="toggleRemindPanel">
            <el-badge :value="remindCount" :hidden="remindCount === 0" :max="99">
              <el-icon :size="20" :class="{ 'has-remind': remindCount > 0, 'shake': hasNewRemind }">
                <Bell />
              </el-icon>
            </el-badge>
          </div>

          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span>{{ userStore.userInfo?.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边菜单 -->
        <el-aside width="200px" class="layout-aside">
          <el-menu
            :default-active="activeMenu"
            router
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409eff"
          >
            <el-menu-item index="/dashboard" v-if="permissions.canViewDashboard">
              <el-icon><Odometer /></el-icon>
              <span>工作台</span>
            </el-menu-item>
            <el-menu-item index="/tables" v-if="permissions.canViewTables">
              <el-icon><Grid /></el-icon>
              <span>桌台管理</span>
            </el-menu-item>
            <el-menu-item index="/orders" v-if="permissions.canViewOrders">
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="/reports" v-if="permissions.canViewStatistics">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
            <el-menu-item index="/users" v-if="permissions.canViewUsers">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/roles" v-if="permissions.canViewRoles">
              <el-icon><Lock /></el-icon>
              <span>角色管理</span>
            </el-menu-item>
            <el-menu-item index="/logs" v-if="permissions.canViewLogs">
              <el-icon><Notebook /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
            <el-menu-item index="/members" v-if="permissions.canViewMembers">
              <el-icon><User /></el-icon>
              <span>会员管理</span>
            </el-menu-item>
            <el-menu-item index="/member-levels" v-if="permissions.canViewMemberLevels">
              <el-icon><Star /></el-icon>
              <span>会员等级</span>
            </el-menu-item>
            <el-menu-item index="/settings" v-if="permissions.canViewSettings">
              <el-icon><Tools /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="layout-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>

    <!-- 提醒面板 -->
    <RemindPanel
      ref="remindPanelRef"
      :sound-enabled="true"
      @update:count="handleRemindCountUpdate"
      @remind="handleRemind"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'
import { ElMessageBox } from 'element-plus'
import { Bell, User, SwitchButton, Odometer, Grid, Document, DataAnalysis, Lock, Notebook, Star, Tools } from '@element-plus/icons-vue'
import RemindPanel from '@/components/RemindPanel.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const webSocketStore = useWebSocketStore()

const activeMenu = computed(() => route.path)
const remindPanelRef = ref<InstanceType<typeof RemindPanel>>()
const remindCount = ref(0)
const hasNewRemind = ref(false)

/**
 * 检查是否有指定模块下的任意权限
 * @param prefix 权限前缀，如 'table:'、'member:'
 */
const hasModulePermission = (prefix: string): boolean => {
  if (!userStore.permissions || userStore.permissions.length === 0) {
    return false
  }
  // 超级管理员
  if (userStore.permissions.includes('*')) {
    return true
  }
  // 检查是否有任意以该前缀开头的权限
  return userStore.permissions.some(p => p.startsWith(prefix))
}

// 使用 computed 缓存权限判断，避免频繁重新计算
const permissions = computed(() => ({
  canViewDashboard: userStore.hasPermission('dashboard:view'),
  // 桌台管理：有任意 table: 开头的权限即可显示
  canViewTables: hasModulePermission('table:'),
  canViewOrders: userStore.hasPermission('order:view'),
  canViewStatistics: userStore.hasPermission('statistics:view'),
  canViewUsers: userStore.hasPermission('user:view'),
  canViewRoles: userStore.hasPermission('role:view'),
  canViewLogs: userStore.hasPermission('log:view'),
  // 会员管理：有任意 member: 开头的权限即可显示
  canViewMembers: hasModulePermission('member:'),
  // 会员等级：有任意 member:level: 开头的权限即可显示
  canViewMemberLevels: hasModulePermission('member:level:'),
  // 系统设置：有任意 system: 开头的权限即可显示
  canViewSettings: hasModulePermission('system:')
}))

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      webSocketStore.disconnect()
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}

// 切换提醒面板
const toggleRemindPanel = () => {
  remindPanelRef.value?.toggle()
  hasNewRemind.value = false
}

// 处理提醒数量更新
const handleRemindCountUpdate = (count: number) => {
  remindCount.value = count
  if (count > 0) {
    hasNewRemind.value = true
    setTimeout(() => {
      hasNewRemind.value = false
    }, 2000)
  }
}

// 处理新提醒
const handleRemind = () => {
  hasNewRemind.value = true
  setTimeout(() => {
    hasNewRemind.value = false
  }, 2000)
}

// 初始化 WebSocket
onMounted(() => {
  if (userStore.userInfo?.id && !webSocketStore.isInitialized) {
    webSocketStore.init(userStore.userInfo.id)
  }
})
</script>

<style scoped lang="scss">
.layout-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #ffeef8 0%, #f3e7ff 50%, #e8f4ff 100%);

  :deep(.el-container) {
    height: 100%;
  }
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 16px rgba(212, 165, 255, 0.15);
  padding: 0 24px;
  height: 64px;
  border-bottom: 1px solid rgba(212, 165, 255, 0.2);

  .header-left {
    .logo-wrapper {
      display: flex;
      align-items: center;
      gap: 12px;

      .logo {
        width: 36px;
        height: 36px;
        animation: logoFloat 3s ease-in-out infinite;
      }

      h2 {
        margin: 0;
        font-size: 22px;
        font-weight: 600;
        background: linear-gradient(135deg, #d4a5ff 0%, #ffb6d9 50%, #c5e3ff 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        letter-spacing: 1px;
      }
    }
  }

  @keyframes logoFloat {
    0%, 100% {
      transform: translateY(0);
    }
    50% {
      transform: translateY(-3px);
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .remind-bell {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      cursor: pointer;
      border-radius: 12px;
      transition: all 0.3s ease;
      position: relative;

      &:hover {
        background: linear-gradient(135deg, rgba(212, 165, 255, 0.15), rgba(255, 182, 217, 0.15));

        .el-icon {
          color: #d4a5ff;
        }
      }

      .el-icon {
        color: #999;
        transition: color 0.3s ease;
      }

      .el-icon.has-remind {
        color: #ffb6d9;
      }

      .el-icon.shake {
        animation: gentleShake 0.6s ease-in-out;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 10px 16px;
      border-radius: 12px;
      transition: all 0.3s ease;
      color: #666;

      &:hover {
        background: linear-gradient(135deg, rgba(212, 165, 255, 0.15), rgba(255, 182, 217, 0.15));
        color: #d4a5ff;
      }
    }
  }
}

@keyframes gentleShake {
  0%, 100% {
    transform: rotate(0deg) scale(1);
  }
  25% {
    transform: rotate(-8deg) scale(1.1);
  }
  50% {
    transform: rotate(8deg) scale(1.1);
  }
  75% {
    transform: rotate(-8deg) scale(1.1);
  }
}

.layout-aside {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(243, 231, 255, 0.9) 100%);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(212, 165, 255, 0.2);
  overflow-x: hidden;
  overflow-y: auto;

  :deep(.el-menu) {
    background: transparent;
    border: none;

    .el-menu-item {
      color: #666;
      transition: all 0.3s ease;
      margin: 4px 12px;
      border-radius: 12px;

      &:hover {
        background: linear-gradient(135deg, rgba(212, 165, 255, 0.15), rgba(255, 182, 217, 0.15)) !important;
        color: #d4a5ff !important;
      }

      &.is-active {
        background: linear-gradient(135deg, #d4a5ff, #ffb6d9) !important;
        color: #fff !important;
        box-shadow: 0 4px 12px rgba(212, 165, 255, 0.3);
      }

      .el-icon {
        color: inherit;
      }
    }
  }
}

.layout-main {
  background: transparent;
  padding: 24px;
  overflow-y: auto;
  height: 100%;
  box-sizing: border-box;
}
</style>
