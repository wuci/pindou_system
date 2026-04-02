<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper" @click="navigateToReports">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">今日营收</div>
              <div class="stat-value">¥{{ todayRevenue.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper" @click="navigateToTables">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">使用中桌台</div>
              <div class="stat-value">{{ usingTableCount }} / {{ totalTableCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper" @click="navigateToOrders">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">今日订单</div>
              <div class="stat-value">{{ todayOrderCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">翻台率</div>
              <div class="stat-value">{{ turnoverRate }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>欢迎来到拼豆店计时管理系统</span>
          </template>
          <div class="welcome-content">
            <h3>👋 欢迎使用拼豆店计时管理系统</h3>
            <p>系统正在初始化中，请稍候...</p>
            <p>Day 1 项目初始化已完成 ✅</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTableList } from '@/api/table'
import { getActiveOrders } from '@/api/order'
import type { TableInfo } from '@/api/table'

const router = useRouter()

// 统计数据
const todayRevenue = ref(0)
const usingTableCount = ref(0)
const totalTableCount = ref(0)
const todayOrderCount = ref(0)
const turnoverRate = ref(0)

// 跳转到数据统计
const navigateToReports = () => {
  router.push('/reports')
}

// 跳转到桌台管理
const navigateToTables = () => {
  router.push('/tables')
}

// 跳转到订单管理
const navigateToOrders = () => {
  router.push('/orders')
}

// 加载工作台数据
const loadDashboardData = async () => {
  try {
    // 获取所有桌台
    const tables = await getTableList()

    // 桌台总数（实际存在的桌台数量）
    totalTableCount.value = tables.length

    // 计算使用中的桌台数
    usingTableCount.value = tables.filter((t: TableInfo) => t.status === 'using').length

    // 获取当天已完成的订单
    const todayStart = new Date()
    todayStart.setHours(0, 0, 0, 0)
    const todayStartTime = todayStart.getTime()

    const ordersResult = await getActiveOrders({ page: 1, pageSize: 1000 })
    const todayOrders = ordersResult.list || []

    // 计算今日营收
    todayRevenue.value = todayOrders.reduce((sum: number, order: any) => {
      if (order.createdAt >= todayStartTime) {
        return sum + (order.amount || 0)
      }
      return sum
    }, 0)

    // 今日订单数
    todayOrderCount.value = todayOrders.length

    // 计算翻台率
    if (totalTableCount.value > 0) {
      turnoverRate.value = Math.round((todayOrderCount.value / totalTableCount.value) * 100)
    }
  } catch (error) {
    console.error('加载工作台数据失败:', error)
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card-wrapper {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
    }

    &:active {
      transform: translateY(-2px);
    }
  }

  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 24px;
    }

    .stat-content {
      flex: 1;

      .stat-title {
        font-size: 14px;
        color: #999;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #333;
      }
    }
  }

  .welcome-content {
    text-align: center;
    padding: 40px 0;

    h3 {
      margin-bottom: 16px;
      font-size: 20px;
      color: #333;
    }

    p {
      margin: 8px 0;
      font-size: 14px;
      color: #666;
    }
  }
}
</style>
