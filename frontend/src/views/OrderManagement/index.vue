<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <el-button
            type="primary"
            size="small"
            @click="handleExport"
            :loading="exporting"
          >
            导出订单
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 当前订单 -->
        <el-tab-pane label="当前订单" name="active">
          <div v-if="activeLoading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="activeOrders.length === 0" class="empty-container">
            <el-empty description="暂无进行中的订单" />
          </div>
          <el-table
            v-else
            :data="activeOrders"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="tableName" label="桌台名称" width="120" />
            <el-table-column label="开始时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column label="使用时长" width="120">
              <template #default="{ row }">
                {{ formatDuration(row.duration) }}
              </template>
            </el-table-column>
            <el-table-column label="预设时长" width="120">
              <template #default="{ row }">
                {{ row.presetDuration ? formatDuration(row.presetDuration) : '不设时长' }}
              </template>
            </el-table-column>
            <el-table-column label="当前费用" width="120">
              <template #default="{ row }">
                <span class="amount">¥{{ formatMoney(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作员" width="100" />
            <el-table-column label="状态" width="100">
              <template #default>
                <el-tag type="success" size="small">进行中</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="handleViewDetail(row.id)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 历史订单 -->
        <el-tab-pane label="历史订单" name="history">
          <!-- 筛选条件 -->
          <div class="filter-container">
            <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery">
              <el-form-item label="日期范围">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="x"
                  @change="handleDateChange"
                />
              </el-form-item>
              <el-form-item label="桌台">
                <el-select
                  v-model="queryForm.tableId"
                  placeholder="全部桌台"
                  clearable
                  style="width: 120px"
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
                  style="width: 150px"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleQuery">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
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
            style="width: 100%"
          >
            <el-table-column prop="tableName" label="桌台名称" width="120" />
            <el-table-column label="开始时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column label="结束时间" width="180">
              <template #default="{ row }">
                {{ row.endTime ? formatDateTime(row.endTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="使用时长" width="120">
              <template #default="{ row }">
                {{ formatDuration(row.duration) }}
              </template>
            </el-table-column>
            <el-table-column label="总金额" width="120">
              <template #default="{ row }">
                <span class="amount">¥{{ formatMoney(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作员" width="100" />
            <el-table-column label="支付时间" width="180">
              <template #default="{ row }">
                {{ row.paidAt ? formatDateTime(row.paidAt) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="handleViewDetail(row.id)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container" v-if="total > 0">
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
import OrderDetailDrawer from '@/components/OrderDetailDrawer.vue'

// 当前订单
const activeTab = ref('active')
const activeLoading = ref(false)
const activeOrders = ref<OrderInfo[]>([])

// 历史订单
const historyLoading = ref(false)
const historyOrders = ref<OrderInfo[]>([])
const total = ref(0)
const dateRange = ref<number[]>([])

const queryForm = reactive({
  page: 1,
  pageSize: 10,
  status: 'completed',
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

onMounted(() => {
  loadActiveOrders()
  loadHistoryOrders()
})

const handleTabChange = (tabName: string) => {
  if (tabName === 'active' && activeOrders.value.length === 0) {
    loadActiveOrders()
  } else if (tabName === 'history' && historyOrders.value.length === 0) {
    loadHistoryOrders()
  }
}

const loadActiveOrders = async () => {
  activeLoading.value = true
  try {
    activeOrders.value = await getActiveOrders()
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
    const result = await getHistoryOrders(queryForm)
    historyOrders.value = result.records
    total.value = result.total
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
  queryForm.page = page
  loadHistoryOrders()
}

const handleSizeChange = (size: number) => {
  queryForm.pageSize = size
  queryForm.page = 1
  loadHistoryOrders()
}

const handleViewDetail = (orderId: string) => {
  selectedOrderId.value = orderId
  detailDrawerVisible.value = true
}

const handleExport = async () => {
  exporting.value = true
  try {
    const data = await exportOrders({
      status: 'completed',
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
</script>

<style scoped>
.order-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px 0;
}

.filter-container {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.amount {
  font-weight: 600;
  color: #409eff;
}
</style>
