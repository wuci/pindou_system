<template>
  <div class="report-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日营收</div>
              <div class="stat-value">¥{{ formatMoney(dailyStats.todayRevenue) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon orders">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日订单</div>
              <div class="stat-value">{{ dailyStats.todayOrderCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon tables">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">使用中桌台</div>
              <div class="stat-value">{{ dailyStats.activeTableCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon turnover">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">翻台率</div>
              <div class="stat-value">{{ (dailyStats.turnoverRate * 100).toFixed(1) }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 营收趋势图表 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="chart-header">
          <span>营收趋势</span>
          <el-radio-group v-model="trendDays" @change="handleTrendDaysChange">
            <el-radio-button :label="7">近7天</el-radio-button>
            <el-radio-button :label="14">近14天</el-radio-button>
            <el-radio-button :label="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Money, Document, OfficeBuilding, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDailyStats, getRevenueTrend, type DailyStats, type TrendItem } from '@/api/report'

// 数据
const dailyStats = ref<DailyStats>({
  todayRevenue: 0,
  activeTableCount: 0,
  todayOrderCount: 0,
  turnoverRate: 0,
  todayDuration: 0
})

const trendData = ref<TrendItem[]>([])
const trendDays = ref(7)
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 加载今日统计
const loadDailyStats = async () => {
  try {
    const data = await getDailyStats()
    dailyStats.value = data
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error(error)
  }
}

// 加载营收趋势
const loadRevenueTrend = async () => {
  try {
    const data = await getRevenueTrend(trendDays.value)
    trendData.value = data
    renderChart()
  } catch (error) {
    ElMessage.error('加载营收趋势失败')
    console.error(error)
  }
}

// 切换趋势天数
const handleTrendDaysChange = () => {
  loadRevenueTrend()
}

// 渲染图表
const renderChart = () => {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const dates = trendData.value.map(item => item.date)
  const revenues = trendData.value.map(item => item.revenue)
  const orderCounts = trendData.value.map(item => item.orderCount)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['营收', '订单数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '营收（元）',
        position: 'left'
      },
      {
        type: 'value',
        name: '订单数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '营收',
        type: 'line',
        smooth: true,
        data: revenues,
        yAxisIndex: 0,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        itemStyle: {
          color: '#409eff'
        }
      },
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: orderCounts,
        yAxisIndex: 1,
        itemStyle: {
          color: '#67c23a'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

// 格式化金额
const formatMoney = (amount: number) => {
  return amount.toFixed(2)
}

// 定时刷新
let refreshTimer: number | null = null

const startRefresh = () => {
  refreshTimer = window.setInterval(() => {
    loadDailyStats()
  }, 30000) // 30秒刷新一次
}

const stopRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 窗口大小变化时重新渲染图表
const handleResize = () => {
  chartInstance?.resize()
}

// 生命周期
onMounted(() => {
  loadDailyStats()
  loadRevenueTrend()
  startRefresh()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  stopRefresh()
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.report-page {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon i {
  font-size: 28px;
  color: #fff;
}

.stat-icon.revenue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.orders {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.tables {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.turnover {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 400px;
}
</style>
