<template>
  <div class="order-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">订单管理</span>
          <el-button
            type="primary"
            size="default"
            @click="handleExport"
            :loading="exporting"
          >
            <el-icon><Download /></el-icon>
            <span style="margin-left: 6px">导出订单</span>
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="order-tabs">
        <!-- 当前订单 -->
        <el-tab-pane label="当前订单" name="active">
          <div v-if="activeLoading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="activeOrders.length === 0" class="empty-container">
            <el-empty description="暂无当前订单" />
          </div>
          <el-table
            v-else
            :data="activeOrders"
            stripe
            class="order-table"
            :row-style="{ height: '64px' }"
            :cell-style="{ fontSize: '14px' }"
            :header-cell-style="{ fontSize: '14px', fontWeight: '600', background: '#f5f7fa' }"
          >
            <el-table-column label="订单编号" width="180">
              <template #default="{ row }">
                <span class="order-no">{{ row.orderNo || row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tableName" label="桌台" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="info" size="large" effect="plain">{{ row.tableName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="套餐类型" width="150" align="center">
              <template #default="{ row }">
                <el-tag :type="getPackageTypeColor(row)" size="large" effect="plain">
                  {{ getPackageTypeName(row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="渠道" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="getChannelTagType(row.channel)" size="small" effect="plain">
                  {{ getChannelDisplayName(row.channel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="开始时间" width="170">
              <template #default="{ row }">
                <span class="time-text">{{ formatDateTime(row.startTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="使用时长" width="140" align="center">
              <template #default="{ row }">
                <div class="duration-cell">
                  <div class="duration-main">{{ formatDuration(row.duration) }}</div>
                  <div v-if="row.pauseDuration > 0" class="pause-text">
                    暂停{{ formatDuration(row.pauseDuration) }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="费用" width="140" align="right">
              <template #default="{ row }">
                <div class="amount-cell">
                  <div v-if="row.originalAmount && row.originalAmount > row.amount" class="amount-with-original">
                    <span class="original-amount">¥{{ formatMoney(row.originalAmount) }}</span>
                    <span class="final-amount">¥{{ formatMoney(row.amount) }}</span>
                  </div>
                  <div v-else class="amount-simple">
                    <span class="final-amount">¥{{ formatMoney(row.amount) }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="会员" width="120" align="center">
              <template #default="{ row }">
                <div v-if="row.memberName" class="member-cell">
                  <div class="member-name">{{ row.memberName }}</div>
                  <el-tag v-if="row.memberDiscountRate" type="success" size="small" effect="plain">
                    {{ (row.memberDiscountRate * 10).toFixed(1) }}折
                  </el-tag>
                </div>
                <span v-else class="non-member">非会员</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'active'" type="success" size="large" effect="plain">进行中</el-tag>
                <el-tag v-else-if="row.status === 'completed'" type="info" size="large" effect="plain">已完成</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="default"
                  link
                  @click="handleViewDetail(row.id)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="activeQueryForm.page"
              v-model:page-size="activeQueryForm.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="activeTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleActiveSizeChange"
              @current-change="handleActivePageChange"
            />
          </div>
        </el-tab-pane>

        <!-- 历史订单 -->
        <el-tab-pane label="历史订单" name="history">
          <!-- 筛选条件 -->
          <div class="filter-container">
            <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" class="filter-form">
              <el-form-item label="订单状态">
                <el-select
                  v-model="queryForm.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 140px"
                >
                  <el-option label="全部" value="" />
                  <el-option label="已完成" value="completed" />
                  <el-option label="已作废" value="cancelled" />
                  <el-option label="进行中" value="active" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期范围">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="x"
                  @change="handleDateChange"
                  style="width: 280px"
                />
              </el-form-item>
              <el-form-item label="桌台">
                <el-select
                  v-model="queryForm.tableId"
                  placeholder="全部桌台"
                  clearable
                  style="width: 140px"
                >
                  <el-option
                    v-for="i in 50"
                    :key="i"
                    :label="`桌台${i}`"
                    :value="i"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="关键词">
                <el-input
                  v-model="queryForm.keyword"
                  placeholder="桌台名称/操作员"
                  clearable
                  style="width: 180px"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleQuery">
                  <el-icon><Search /></el-icon>
                  <span style="margin-left: 4px">查询</span>
                </el-button>
                <el-button @click="handleReset">
                  <el-icon><RefreshLeft /></el-icon>
                  <span style="margin-left: 4px">重置</span>
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 订单列表 -->
          <div v-if="historyLoading" class="loading-container">
            <el-skeleton :rows="8" animated />
          </div>
          <div v-else-if="historyOrders.length === 0" class="empty-container">
            <el-empty description="暂无历史订单" />
          </div>
          <el-table
            v-else
            :data="historyOrders"
            stripe
            class="order-table"
            :row-style="{ height: '64px' }"
            :cell-style="{ fontSize: '14px' }"
            :header-cell-style="{ fontSize: '14px', fontWeight: '600', background: '#f5f7fa' }"
          >
            <el-table-column label="订单编号" width="180">
              <template #default="{ row }">
                <span class="order-no">{{ row.orderNo || row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tableName" label="桌台" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="info" size="large" effect="plain">{{ row.tableName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="套餐类型" width="150" align="center">
              <template #default="{ row }">
                <el-tag :type="getPackageTypeColor(row)" size="large" effect="plain">
                  {{ getPackageTypeName(row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="渠道" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="getChannelTagType(row.channel)" size="small" effect="plain">
                  {{ getChannelDisplayName(row.channel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="200">
              <template #default="{ row }">
                <div class="time-range-cell">
                  <div>{{ formatDateTime(row.startTime) }}</div>
                  <div v-if="row.endTime" class="end-time">至 {{ formatDateTime(row.endTime) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="使用时长" width="140" align="center">
              <template #default="{ row }">
                <div class="duration-cell">
                  <div class="duration-main">{{ formatDuration(row.duration) }}</div>
                  <div v-if="row.pauseDuration > 0" class="pause-text">
                    暂停{{ formatDuration(row.pauseDuration) }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="费用" width="140" align="right">
              <template #default="{ row }">
                <div class="amount-cell">
                  <div v-if="row.originalAmount && row.originalAmount > row.amount" class="amount-with-original">
                    <span class="original-amount">¥{{ formatMoney(row.originalAmount) }}</span>
                    <span class="final-amount">¥{{ formatMoney(row.amount) }}</span>
                  </div>
                  <div v-else class="amount-simple">
                    <span class="final-amount">¥{{ formatMoney(row.amount) }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="会员" width="120" align="center">
              <template #default="{ row }">
                <div v-if="row.memberName" class="member-cell">
                  <div class="member-name">{{ row.memberName }}</div>
                  <el-tag v-if="row.memberDiscountRate" type="success" size="small" effect="plain">
                    {{ (row.memberDiscountRate * 10).toFixed(1) }}折
                  </el-tag>
                </div>
                <span v-else class="non-member">非会员</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'active'" type="success" size="large" effect="plain">进行中</el-tag>
                <el-tag v-else-if="row.status === 'completed'" type="info" size="large" effect="plain">已完成</el-tag>
                <el-tag v-else-if="row.status === 'cancelled'" type="danger" size="large" effect="plain">已作废</el-tag>
                <el-tag v-else type="warning" size="large" effect="plain">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作员" width="110" align="center" />
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="default"
                  link
                  @click="handleViewDetail(row.id)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="queryForm.page"
              v-model:page-size="queryForm.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 订单详情抽屉 -->
    <order-detail-drawer
      v-model="detailDrawerVisible"
      :order-id="selectedOrderId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getActiveOrders, getHistoryOrders, exportOrders } from '@/api/order'
import type { OrderInfo } from '@/api/order'
import { ElMessage } from 'element-plus'
import { Download, Search, RefreshLeft } from '@element-plus/icons-vue'
import OrderDetailDrawer from '@/components/OrderDetailDrawer.vue'
import { useChannelTranslation } from '@/composables/useChannelTranslation'

// 渠道翻译
const { loadBillingRules, getChannels, getChannelName } = useChannelTranslation()

// 当前订单
const activeTab = ref('active')
const activeLoading = ref(false)
const activeOrders = ref<OrderInfo[]>([])
const activeTotal = ref(0)
const activeQueryForm = reactive({
  page: 1,
  pageSize: 10
})

// 历史订单
const historyLoading = ref(false)
const historyOrders = ref<OrderInfo[]>([])
const total = ref(0)
const dateRange = ref<number[]>([])

const queryForm = reactive({
  page: 1,
  pageSize: 10,
  status: '',
  tableId: undefined as number | undefined,
  keyword: '',
  startTime: undefined as number | undefined,
  endTime: undefined as number | undefined
})

// 详情抽屉
const detailDrawerVisible = ref(false)
const selectedOrderId = ref('')

// 导出
const exporting = ref(false)

onMounted(async () => {
  await loadBillingRules()
  loadActiveOrders()
  loadHistoryOrders()
})

const handleTabChange = (tabName: string | number) => {
  const name = String(tabName)
  if (name === 'active') {
    if (activeOrders.value.length === 0) {
      activeQueryForm.page = 1
      loadActiveOrders()
    }
  } else if (name === 'history') {
    if (historyOrders.value.length === 0) {
      queryForm.page = 1
      loadHistoryOrders()
    }
  }
}

const loadActiveOrders = async () => {
  activeLoading.value = true
  try {
    console.log('加载当前订单，参数：', activeQueryForm)
    const result = await getActiveOrders(activeQueryForm)
    console.log('当前订单返回结果：', result)
    activeOrders.value = result.list || []
    activeTotal.value = result.total || 0
  } catch (error) {
    ElMessage.error('加载当前订单失败')
    console.error(error)
  } finally {
    activeLoading.value = false
  }
}

const loadHistoryOrders = async () => {
  historyLoading.value = true
  try {
    console.log('加载历史订单，参数：', queryForm)
    const result = await getHistoryOrders(queryForm)
    console.log('历史订单返回结果：', result)
    historyOrders.value = result.list || []
    total.value = result.total || 0
  } catch (error) {
    ElMessage.error('加载历史订单失败')
    console.error(error)
  } finally {
    historyLoading.value = false
  }
}

const handleQuery = () => {
  queryForm.page = 1
  loadHistoryOrders()
}

const handleReset = () => {
  dateRange.value = []
  queryForm.tableId = undefined
  queryForm.keyword = ''
  queryForm.startTime = undefined
  queryForm.endTime = undefined
  queryForm.page = 1
  loadHistoryOrders()
}

const handleDateChange = (values: number[]) => {
  if (values && values.length === 2) {
    queryForm.startTime = values[0]
    queryForm.endTime = values[1]
  } else {
    queryForm.startTime = undefined
    queryForm.endTime = undefined
  }
}

const handlePageChange = (page: number) => {
  console.log('历史订单页码变化：', page)
  queryForm.page = page
  loadHistoryOrders()
}

const handleSizeChange = (size: number) => {
  console.log('历史订单每页大小变化：', size)
  queryForm.pageSize = size
  queryForm.page = 1
  loadHistoryOrders()
}

const handleActivePageChange = (page: number) => {
  console.log('当前订单页码变化：', page)
  activeQueryForm.page = page
  loadActiveOrders()
}

const handleActiveSizeChange = (size: number) => {
  console.log('当前订单每页大小变化：', size)
  activeQueryForm.pageSize = size
  activeQueryForm.page = 1
  loadActiveOrders()
}

const handleViewDetail = (orderId: string) => {
  selectedOrderId.value = orderId
  detailDrawerVisible.value = true
}

const handleExport = async () => {
  exporting.value = true
  try {
    const data = await exportOrders({
      status: queryForm.status,
      tableId: queryForm.tableId,
      startTime: queryForm.startTime,
      endTime: queryForm.endTime,
      keyword: queryForm.keyword
    })

    // 创建下载链接
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `订单数据_${new Date().toLocaleDateString()}.json`
    link.click()
    window.URL.revokeObjectURL(url)

    ElMessage.success('订单导出成功')
  } catch (error) {
    ElMessage.error('导出订单失败')
    console.error(error)
  } finally {
    exporting.value = false
  }
}

const formatDateTime = (timestamp: number) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatDuration = (seconds: number) => {
  if (!seconds) return '0秒'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  const parts = []
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分`)
  if (secs > 0 || parts.length === 0) parts.push(`${secs}秒`)

  return parts.join('')
}

const formatMoney = (amount: number) => {
  return amount.toFixed(2)
}

/**
 * 获取套餐类型名称
 */
const getPackageTypeName = (order: OrderInfo): string => {
  if (!order.presetDuration) {
    return '不限时'
  }

  const presetMinutes = Math.round(order.presetDuration / 60)

  // 在计费规则中查找匹配的规则
  const channels = getChannels.value
  for (const channel of channels) {
    if (channel.channel === order.channel) {
      for (const rule of channel.rules) {
        if (rule.unlimited) {
          if (!order.presetDuration) {
            return '不限时'
          }
        } else if (rule.minutes === presetMinutes) {
          // 找到匹配的规则，使用时长作为套餐名称
          return formatRuleTime(rule)
        }
      }
    }
  }

  // 没有找到匹配的规则，使用默认格式
  return formatDefaultPackageType(presetMinutes)
}

/**
 * 格式化规则时间
 */
const formatRuleTime = (rule: any): string => {
  if (rule.unlimited) {
    return '不限时'
  }
  const totalMinutes = rule.minutes || 0
  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60

  if (hours > 0 && minutes > 0) {
    return `${hours}小时${minutes}分`
  } else if (hours > 0) {
    return `${hours}小时套餐`
  } else {
    return `${minutes}分钟套餐`
  }
}

/**
 * 格式化默认套餐类型
 */
const formatDefaultPackageType = (minutes: number): string => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60

  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分`
  } else if (hours > 0) {
    return `${hours}小时套餐`
  } else {
    return `${mins}分钟套餐`
  }
}

/**
 * 获取套餐类型颜色
 */
const getPackageTypeColor = (order: OrderInfo): 'success' | 'warning' | 'danger' | 'info' => {
  if (!order.presetDuration) {
    return 'danger'
  }

  const presetMinutes = Math.round(order.presetDuration / 60)

  // 根据时长返回不同的颜色
  if (presetMinutes >= 240) {
    return 'danger' // 4小时以上 - 红色
  } else if (presetMinutes >= 120) {
    return 'warning' // 2-4小时 - 橙色
  } else if (presetMinutes >= 60) {
    return 'success' // 1-2小时 - 绿色
  } else {
    return 'info' // 1小时以下 - 灰色
  }
}

/**
 * 获取渠道显示名称
 */
const getChannelDisplayName = (channel: string): string => {
  return getChannelName(channel || 'store')
}

/**
 * 获取渠道标签颜色
 */
const getChannelTagType = (channel: string): 'success' | 'warning' | 'danger' | 'info' | 'primary' => {
  const typeMap: Record<string, 'success' | 'warning' | 'danger' | 'info' | 'primary'> = {
    store: 'primary',
    meituan: 'warning',
    dianping: 'danger'
  }
  return typeMap[channel] || 'info'
}
</script>

<style scoped>
.order-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 40px);
}

.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.order-tabs {
  padding: 0 4px;
}

.order-table {
  border-radius: 4px;
  overflow: hidden;
}

.order-table :deep(.el-table__row) {
  transition: background-color 0.3s;
}

.order-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa !important;
}

.loading-container {
  padding: 40px;
}

.empty-container {
  padding: 60px 0;
}

.filter-container {
  margin-bottom: 20px;
  padding: 20px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-form {
  margin-bottom: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}

.filter-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}

/* 订单编号 */
.order-no {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 时间文本 */
.time-text {
  font-size: 14px;
  color: #606266;
}

.time-range-cell {
  font-size: 13px;
  line-height: 1.6;
}

.time-range-cell .end-time {
  color: #909399;
  margin-top: 2px;
}

/* 时长单元格 */
.duration-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.duration-main {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.pause-text {
  font-size: 12px;
  color: #e6a23c;
  font-weight: 500;
}

/* 金额单元格 */
.amount-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.amount-with-original {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.original-amount {
  font-size: 12px;
  color: #909399;
  text-decoration: line-through;
  font-weight: 400;
}

.final-amount {
  font-size: 16px;
  font-weight: 700;
  color: #409eff;
}

.amount-simple .final-amount {
  font-size: 16px;
}

/* 会员单元格 */
.member-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.non-member {
  font-size: 13px;
  color: #c0c4cc;
}

/* 标签样式优化 */
:deep(.el-tag--large) {
  padding: 6px 14px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 4px;
}

:deep(.el-tag--plain) {
  background-color: #ffffff;
}

/* 按钮样式优化 */
:deep(.el-button--default) {
  padding: 8px 16px;
  font-size: 14px;
  border-radius: 4px;
  font-weight: 500;
}

:deep(.el-button--primary.is-link) {
  font-size: 14px;
  font-weight: 500;
  padding: 8px 12px;
}

/* 分页样式优化 */
:deep(.el-pagination) {
  font-weight: 500;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 4px;
}

:deep(.el-pagination .el-pager li.is-active) {
  background-color: #409eff;
  color: #ffffff;
}

/* 输入框和选择器样式优化 */
:deep(.el-input__wrapper) {
  border-radius: 4px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 4px;
}

:deep(.el-date-editor) {
  border-radius: 4px;
}
</style>
