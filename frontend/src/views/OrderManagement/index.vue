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
            <el-empty description="暂无当前订单" />
          </div>
          <el-table
            v-else
            :data="activeOrders"
            stripe
            style="width: 100%"
          >
            <el-table-column label="订单编号" width="160">
              <template #default="{ row }">
                <span class="order-no">{{ row.orderNo || row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tableName" label="桌台" width="80" />
            <el-table-column label="渠道" width="90">
              <template #default="{ row }">
                <el-tag :type="getChannelType(row.channel)" size="small">{{ getChannelName(row.channel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="开始时间" width="140">
              <template #default="{ row }">
                {{ formatDateTime(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column label="时长" width="90">
              <template #default="{ row }">
                <div>{{ formatDuration(row.duration) }}</div>
                <div v-if="row.pauseDuration > 0" class="pause-text">
                  暂停{{ formatDuration(row.pauseDuration) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="费用明细" width="120">
              <template #default="{ row }">
                <div v-if="row.originalAmount && row.originalAmount > row.amount">
                  <div style="font-size: 11px; color: #909399;">原价 ¥{{ formatMoney(row.originalAmount) }}</div>
                  <div style="font-size: 13px; color: #409eff; font-weight: 600;">实付 ¥{{ formatMoney(row.amount) }}</div>
                </div>
                <div v-else>
                  <span style="font-size: 13px; font-weight: 600; color: #409eff;">¥{{ formatMoney(row.amount) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="会员信息" width="100">
              <template #default="{ row }">
                <div v-if="row.memberName" style="font-size: 12px;">
                  <div style="color: #303133; font-weight: 500;">{{ row.memberName }}</div>
                  <div v-if="row.memberDiscountRate" style="color: #67c23a;">{{ (row.memberDiscountRate * 10).toFixed(1) }}折</div>
                </div>
                <span v-else style="color: #909399; font-size: 12px;">非会员</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'active'" type="success" size="small">进行中</el-tag>
                <el-tag v-else-if="row.status === 'completed'" type="info" size="small">已完成</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
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
            <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery">
              <el-form-item label="订单状态">
                <el-select
                  v-model="queryForm.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 120px"
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
            <el-table-column label="订单编号" width="160">
              <template #default="{ row }">
                <span class="order-no">{{ row.orderNo || row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tableName" label="桌台" width="80" />
            <el-table-column label="渠道" width="90">
              <template #default="{ row }">
                <el-tag :type="getChannelType(row.channel)" size="small">{{ getChannelName(row.channel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="180">
              <template #default="{ row }">
                <div style="font-size: 12px;">
                  <div>{{ formatDateTime(row.startTime) }}</div>
                  <div v-if="row.endTime" style="color: #909399;">至 {{ formatDateTime(row.endTime) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="时长" width="90">
              <template #default="{ row }">
                <div>{{ formatDuration(row.duration) }}</div>
                <div v-if="row.pauseDuration > 0" class="pause-text">
                  暂停{{ formatDuration(row.pauseDuration) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="费用明细" width="120">
              <template #default="{ row }">
                <div v-if="row.originalAmount && row.originalAmount > row.amount">
                  <div style="font-size: 11px; color: #909399;">原价 ¥{{ formatMoney(row.originalAmount) }}</div>
                  <div style="font-size: 13px; color: #409eff; font-weight: 600;">实付 ¥{{ formatMoney(row.amount) }}</div>
                </div>
                <div v-else>
                  <span style="font-size: 13px; font-weight: 600; color: #409eff;">¥{{ formatMoney(row.amount) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="会员信息" width="100">
              <template #default="{ row }">
                <div v-if="row.memberName" style="font-size: 12px;">
                  <div style="color: #303133; font-weight: 500;">{{ row.memberName }}</div>
                  <div v-if="row.memberDiscountRate" style="color: #67c23a;">{{ (row.memberDiscountRate * 10).toFixed(1) }}折</div>
                </div>
                <span v-else style="color: #909399; font-size: 12px;">非会员</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'active'" type="success" size="small">进行中</el-tag>
                <el-tag v-else-if="row.status === 'completed'" type="info" size="small">已完成</el-tag>
                <el-tag v-else-if="row.status === 'cancelled'" type="danger" size="small">已作废</el-tag>
                <el-tag v-else type="warning" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作员" width="100" />
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
import OrderDetailDrawer from '@/components/OrderDetailDrawer.vue'

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

onMounted(() => {
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

const getChannelName = (channel: string) => {
  const map: Record<string, string> = {
    store: '店内',
    meituan: '美团',
    dianping: '大众点评'
  }
  return map[channel] || channel
}

const getChannelType = (channel: string): 'success' | 'warning' | 'danger' | 'info' => {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    store: 'success',
    meituan: 'warning',
    dianping: 'danger'
  }
  return map[channel] || 'info'
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

.has-pause {
  color: #e6a23c;
  font-weight: 500;
}

.order-no {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  color: #606266;
}

.pause-text {
  font-size: 11px;
  color: #e6a23c;
  margin-top: 2px;
}
</style>
