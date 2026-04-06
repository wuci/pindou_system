<template>
  <div class="report-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon revenue">
              <!-- 温柔营收图标 -->
              <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="24" cy="32" rx="16" ry="10" fill="rgba(255,255,255,0.3)" transform="rotate(-10, 24, 32)"/>
                <circle cx="20" cy="30" r="2.5" fill="#fff" opacity="0.9"/>
                <circle cx="28" cy="34" r="3" fill="#fff" opacity="0.95"/>
                <circle cx="24" cy="26" r="2" fill="#fff" opacity="0.85"/>
                <line x1="20" y1="30" x2="28" y2="34" stroke="#fff" stroke-width="1" opacity="0.3"/>
                <line x1="20" y1="30" x2="24" y2="26" stroke="#fff" stroke-width="1" opacity="0.25"/>
              </svg>
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
              <!-- 温柔订单图标 -->
              <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                <circle cx="24" cy="32" r="12" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.2"/>
                <circle cx="18" cy="28" r="2.5" fill="#fff" opacity="0.9"/>
                <circle cx="30" cy="30" r="3" fill="#fff" opacity="0.95"/>
                <circle cx="24" cy="36" r="2" fill="#fff" opacity="0.85"/>
                <circle cx="32" cy="26" r="1.8" fill="#fff" opacity="0.88"/>
                <circle cx="20" cy="34" r="1.5" fill="#fff" opacity="0.82"/>
                <line x1="18" y1="28" x2="30" y2="30" stroke="#fff" stroke-width="0.6" opacity="0.3"/>
                <line x1="18" y1="28" x2="24" y2="36" stroke="#fff" stroke-width="0.6" opacity="0.25"/>
                <line x1="30" y1="30" x2="32" y2="26" stroke="#fff" stroke-width="0.6" opacity="0.25"/>
              </svg>
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
              <!-- 温柔桌台图标 -->
              <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="16" cy="34" rx="10" ry="7" fill="rgba(255,255,255,0.4)" transform="rotate(-8, 16, 34)"/>
                <ellipse cx="32" cy="34" rx="10" ry="7" fill="rgba(255,255,255,0.5)" transform="rotate(8, 32, 34)"/>
                <circle cx="16" cy="32" r="2" fill="#fff" opacity="0.9"/>
                <circle cx="32" cy="32" r="2.5" fill="#fff" opacity="0.95"/>
                <circle cx="24" cy="28" r="1.5" fill="#fff" opacity="0.85"/>
                <path d="M22 40 Q26 38 30 40" stroke="#fff" stroke-width="1" opacity="0.3" fill="none"/>
              </svg>
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
              <!-- 温柔翻台率图标 -->
              <svg viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                <circle cx="24" cy="32" r="10" fill="none" stroke="rgba(255,255,255,0.4)" stroke-width="1.5"/>
                <circle cx="24" cy="32" r="5" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.5"/>
                <!-- 流动的光点 -->
                <circle cx="24" cy="22" r="2" fill="#fff" opacity="0.95">
                  <animate attributeName="cy" values="22;42;22" dur="3s" repeatCount="indefinite"/>
                </circle>
                <circle cx="14" cy="36" r="1.5" fill="#fff" opacity="0.85">
                  <animate attributeName="cx" values="14;34;14" dur="4s" repeatCount="indefinite"/>
                  <animate attributeName="opacity" values="0.85;0.3;0.85" dur="4s" repeatCount="indefinite"/>
                </circle>
                <circle cx="34" cy="36" r="1.5" fill="#fff" opacity="0.85">
                  <animate attributeName="cx" values="34;14;34" dur="4s" repeatCount="indefinite"/>
                  <animate attributeName="opacity" values="0.85;0.3;0.85" dur="4s" repeatCount="indefinite"/>
                </circle>
                <circle cx="24" cy="32" r="2.5" fill="#fff" opacity="0.98"/>
              </svg>
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
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(212, 165, 255, 0.2);
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
  background: linear-gradient(135deg, #ffb6d9 0%, #d4a5ff 100%);
  box-shadow: 0 4px 12px rgba(212, 165, 255, 0.3);
}

.stat-icon.orders {
  background: linear-gradient(135deg, #c5e3ff 0%, #9ac0ff 100%);
  box-shadow: 0 4px 12px rgba(154, 192, 255, 0.3);
}

.stat-icon.tables {
  background: linear-gradient(135deg, #98d4bb 0%, #7dd3b0 100%);
  box-shadow: 0 4px 12px rgba(152, 216, 187, 0.3);
}

.stat-icon.turnover {
  background: linear-gradient(135deg, #ffd993 0%, #ffb86c 100%);
  box-shadow: 0 4px 12px rgba(255, 217, 147, 0.3);
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
  background: linear-gradient(135deg, #d4a5ff 0%, #ffb6d9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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
