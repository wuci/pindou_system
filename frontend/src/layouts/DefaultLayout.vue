<template>
  <div class="layout-container">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <h2>拼豆店计时管理系统</h2>
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
            <el-menu-item index="/dashboard">
              <el-icon><Dashboard /></el-icon>
              <span>工作台</span>
            </el-menu-item>
            <el-menu-item index="/tables">
              <el-icon><Grid /></el-icon>
              <span>桌台管理</span>
            </el-menu-item>
            <el-menu-item index="/orders">
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="/reports">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
            <el-menu-item index="/users" v-if="hasPermission('user:view')">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/roles" v-if="hasPermission('role:manage')">
              <el-icon><Lock /></el-icon>
              <span>角色管理</span>
            </el-menu-item>
            <el-menu-item index="/logs" v-if="hasPermission('log:view')">
              <el-icon><Tickets /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
            <el-menu-item index="/settings" v-if="hasPermission('system:config')">
              <el-icon><Setting /></el-icon>
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
import { Bell } from '@element-plus/icons-vue'
import RemindPanel from '@/components/RemindPanel.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const webSocketStore = useWebSocketStore()

const activeMenu = computed(() => route.path)
const remindPanelRef = ref<InstanceType<typeof RemindPanel>>()
const remindCount = ref(0)
const hasNewRemind = ref(false)

const hasPermission = (permission: string) => {
  return userStore.hasPermission(permission)
}

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
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
  height: 60px;

  .header-left {
    h2 {
      margin: 0;
      font-size: 20px;
      color: #333;
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
      width: 36px;
      height: 36px;
      cursor: pointer;
      border-radius: 6px;
      transition: all 0.3s ease;
      position: relative;

      &:hover {
        background-color: #f5f7fa;

        .el-icon {
          color: #409eff;
        }
      }

      .el-icon {
        color: #606266;
        transition: color 0.3s ease;
      }

      .el-icon.has-remind {
        color: #f56c6c;
      }

      .el-icon.shake {
        animation: shake 0.5s ease-in-out;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 4px;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }
    }
  }
}

@keyframes shake {
  0%, 100% {
    transform: rotate(0deg);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: rotate(-10deg);
  }
  20%, 40%, 60%, 80% {
    transform: rotate(10deg);
  }
}

.layout-aside {
  background-color: #304156;
  overflow-x: hidden;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
