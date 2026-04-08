<template>
  <div class="order-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">订单管理</span>
          <el-button
            v-if="permissions.canExport"
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
            row-key="id"
            :tree-props="{ children: 'childOrders', hasChildren: 'hasChildren' }"
            :default-expand-all="false"
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
            <el-table-column label="续费次数" width="100" align="center">
              <template #default="{ row }">
                <div v-if="row.extendCount && row.extendCount > 0" class="extend-count-cell">
                  <el-tag type="warning" size="small" effect="plain">{{ row.extendCount }}次</el-tag>
                </div>
                <span v-else class="non-member">-</span>
              </template>
            </el-table-column>
            <el-table-column label="套餐类型" width="150" align="center">
              <template #default="{ row }">
                <el-tag v-if="!row.extendCount || row.extendCount === 0" :type="getPackageTypeColor(row)" size="large" effect="plain">
                  {{ getPackageTypeName(row) }}
                </el-tag>
                <span v-else class="non-member">-</span>
              </template>
            </el-table-column>
            <el-table-column label="渠道" width="110" align="center">
              <template #default="{ row }">
                <el-tag v-if="!row.extendCount || row.extendCount === 0" :type="getChannelTagType(row.channel)" size="small" effect="plain">
                  {{ getChannelDisplayName(row.channel) }}
                </el-tag>
                <span v-else class="non-member">-</span>
              </template>
            </el-table-column>
            <el-table-column label="开始时间" width="170">
              <template #default="{ row }">
                <span class="time-text">{{ formatDateTime(row.startTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="使用时长" width="140" align="center">
              <template #default="{ row }">
                <div v-if="!row.parentId" class="duration-cell">
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
                <div v-if="(!row.extendCount || row.extendCount === 0) && row.memberName" class="member-cell">
                  <div class="member-name">{{ row.memberName }}</div>
                  <el-tag v-if="row.memberDiscountRate" type="success" size="small" effect="plain">
                    {{ (row.memberDiscountRate * 10).toFixed(1) }}折
                  </el-tag>
                </div>
                <span v-else class="non-member">-</span>
              </template>
            </el-table-column>
            <el-table-column label="支付方式" width="160" align="center">
              <template #default="{ row }">
                <div v-if="(!row.extendCount || row.extendCount === 0) && row.paymentMethod" class="payment-method-cell">
                  <el-tag :type="getPaymentMethodTagType(row.paymentMethod)" size="small" effect="plain">
                    {{ getPaymentMethodLabel(row.paymentMethod) }}
                  </el-tag>
                  <div v-if="row.paymentMethod === 'combined' || row.paymentMethod === 'balance'" class="payment-breakdown">
                    <div v-if="row.balanceAmount > 0" class="payment-item balance">
                      余额 ¥{{ formatMoney(row.balanceAmount) }}
                    </div>
                    <div v-if="row.otherPaymentAmount > 0" class="payment-item offline">
                      线下 ¥{{ formatMoney(row.otherPaymentAmount) }}
                    </div>
                  </div>
                </div>
                <span v-else class="non-member">-</span>
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
                  v-if="permissions.canViewDetail"
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

        <!-- 进行中订单 -->
        <el-tab-pane label="进行中订单" name="active-now">
          <!-- 自动刷新提示 -->
          <div v-if="activeNowOrders.length > 0" class="auto-refresh-tip">
            <el-icon class="refresh-icon"><RefreshLeft /></el-icon>
            <span>数据每30秒自动刷新</span>
            <el-button
              type="primary"
              size="small"
              link
              @click="loadActiveNowOrders"
              :loading="activeNowLoading"
            >
              立即刷新
            </el-button>
          </div>

          <div v-if="activeNowLoading && activeNowOrders.length === 0" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="activeNowOrders.length === 0" class="empty-container">
            <el-empty description="暂无进行中订单" />
          </div>
          <el-table
            v-else
            :data="activeNowOrders"
            stripe
            class="order-table"
            :row-style="{ height: '64px' }"
            :cell-style="{ fontSize: '14px' }"
            :header-cell-style="{ fontSize: '14px', fontWeight: '600', background: '#f5f7fa' }"
            row-key="id"
            :tree-props="{ children: 'childOrders', hasChildren: 'hasChildren' }"
            :default-expand-all="false"
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
            <el-table-column label="续费次数" width="100" align="center">
              <template #default="{ row }">
                <div v-if="row.extendCount && row.extendCount > 0" class="extend-count-cell">
                  <el-tag type="warning" size="small" effect="plain">{{ row.extendCount }}次</el-tag>
                </div>
                <span v-else class="non-member">-</span>
              </template>
            </el-table-column>
            <el-table-column label="套餐类型" width="150" align="center">
              <template #default="{ row }">
                <el-tag v-if="!row.extendCount || row.extendCount === 0" :type="getPackageTypeColor(row)" size="large" effect="plain">
                  {{ getPackageTypeName(row) }}
                </el-tag>
                <span v-else class="non-member">-</span>
              </template>
            </el-table-column>
            <el-table-column label="渠道" width="110" align="center">
              <template #default="{ row }">
                <el-tag v-if="!row.extendCount || row.extendCount === 0" :type="getChannelTagType(row.channel)" size="small" effect="plain">
                  {{ getChannelDisplayName(row.channel) }}
                </el-tag>
                <span v-else class="non-member">-</span>
              </template>
            </el-table-column>
            <el-table-column label="开始时间" width="170">
              <template #default="{ row }">
                <span class="time-text">{{ formatDateTime(row.startTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="使用时长" width="140" align="center">
              <template #default="{ row }">
                <div v-if="!row.parentId" class="duration-cell">
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
                <div v-if="(!row.extendCount || row.extendCount === 0) && row.memberName" class="member-cell">
                  <div class="member-name">{{ row.memberName }}</div>
                  <el-tag v-if="row.memberDiscountRate" type="success" size="small" effect="plain">
                    {{ (row.memberDiscountRate * 10).toFixed(1) }}折
                  </el-tag>
                </div>
                <span v-else class="non-member">-</span>
              </template>
            </el-table-column>
            <el-table-column label="支付方式" width="160" align="center">
              <template #default="{ row }">
                <div v-if="(!row.extendCount || row.extendCount === 0) && row.paymentMethod" class="payment-method-cell">
                  <el-tag :type="getPaymentMethodTagType(row.paymentMethod)" size="small" effect="plain">
                    {{ getPaymentMethodLabel(row.paymentMethod) }}
                  </el-tag>
                  <div v-if="row.paymentMethod === 'combined' || row.paymentMethod === 'balance'" class="payment-breakdown">
                    <div v-if="row.balanceAmount > 0" class="payment-item balance">
                      余额 ¥{{ formatMoney(row.balanceAmount) }}
                    </div>
                    <div v-if="row.otherPaymentAmount > 0" class="payment-item offline">
                      线下 ¥{{ formatMoney(row.otherPaymentAmount) }}
                    </div>
                  </div>
                </div>
                <span v-else class="non-member">-</span>
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
                  v-if="permissions.canViewDetail"
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
              v-model:current-page="activeNowQueryForm.page"
              v-model:page-size="activeNowQueryForm.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="activeNowTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleActiveNowSizeChange"
              @current-change="handleActiveNowPageChange"
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
            row-key="id"
            :tree-props="{ children: 'childOrders', hasChildren: 'hasChildren' }"
            :default-expand-all="false"
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
            <el-table-column label="续费次数" width="100" align="center">
              <template #default="{ row }">
                <div v-if="row.extendCount && row.extendCount > 0" class="extend-count-cell">
                  <el-tag type="warning" size="small" effect="plain">{{ row.extendCount }}次</el-tag>
                </div>
                <span v-else class="non-member">-</span>
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
                <div v-if="!row.parentId" class="duration-cell">
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
            <el-table-column label="支付方式" width="160" align="center">
              <template #default="{ row }">
                <div v-if="row.paymentMethod" class="payment-method-cell">
                  <el-tag :type="getPaymentMethodTagType(row.paymentMethod)" size="small" effect="plain">
                    {{ getPaymentMethodLabel(row.paymentMethod) }}
                  </el-tag>
                  <div v-if="row.paymentMethod === 'combined' || row.paymentMethod === 'balance'" class="payment-breakdown">
                    <div v-if="row.balanceAmount > 0" class="payment-item balance">
                      余额 ¥{{ formatMoney(row.balanceAmount) }}
                    </div>
                    <div v-if="row.otherPaymentAmount > 0" class="payment-item offline">
                      线下 ¥{{ formatMoney(row.otherPaymentAmount) }}
                    </div>
                  </div>
                  <div v-if="row.paidAt" class="payment-time">
                    {{ formatDateTime(row.paidAt) }}
                  </div>
                </div>
                <span v-else class="non-member">-</span>
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
                  v-if="permissions.canViewDetail"
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
import { ref, reactive, onMounted, onUnmounted, watch, computed } from 'vue'
import { getActiveOrders, getActiveOrdersNow, getHistoryOrders, exportOrders } from '@/api/order'
import type { OrderInfo } from '@/api/order'
import { ElMessage } from 'element-plus'
import { Download, Search, RefreshLeft } from '@element-plus/icons-vue'
import OrderDetailDrawer from '@/components/OrderDetailDrawer.vue'
import { useChannelTranslation } from '@/composables/useChannelTranslation'
import { useUserStore } from '@/stores/user'

// 用户状态和权限
const userStore = useUserStore()

const permissions = computed(() => ({
  canViewDetail: userStore.hasPermission('order:detail'),
  canExport: userStore.hasPermission('order:export')
}))

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

// 进行中订单
const activeNowLoading = ref(false)
const activeNowOrders = ref<OrderInfo[]>([])
const activeNowTotal = ref(0)
const activeNowQueryForm = reactive({
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

// 自动刷新定时器
let autoRefreshTimer: number | null = null
const AUTO_REFRESH_INTERVAL = 30000 // 30秒自动刷新一次

// 启动自动刷新
const startAutoRefresh = () => {
  // 先清除已有的定时器
  stopAutoRefresh()
  // 启动新的定时器
  autoRefreshTimer = window.setInterval(() => {
    if (activeTab.value === 'active-now') {
      loadActiveNowOrders()
    }
  }, AUTO_REFRESH_INTERVAL)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (autoRefreshTimer !== null) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

// 监听标签页变化，控制自动刷新
watch(activeTab, (newTab) => {
  if (newTab === 'active-now') {
    // 切换到进行中订单标签页，启动自动刷新
    startAutoRefresh()
  } else {
    // 切换到其他标签页，停止自动刷新
    stopAutoRefresh()
  }
})

onMounted(async () => {
  await loadBillingRules()
  loadActiveOrders()
  loadActiveNowOrders()
  loadHistoryOrders()
})

onUnmounted(() => {
  // 组件卸载时清除定时器
  stopAutoRefresh()
})

const handleTabChange = (tabName: string | number) => {
  const name = String(tabName)
  if (name === 'active') {
    if (activeOrders.value.length === 0) {
      activeQueryForm.page = 1
      loadActiveOrders()
    }
  } else if (name === 'active-now') {
    if (activeNowOrders.value.length === 0) {
      activeNowQueryForm.page = 1
      loadActiveNowOrders()
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
    const result = await getActiveOrders(activeQueryForm)
    activeOrders.value = (result.list || []).map((order: any) => ({
      ...order,
      hasChildren: order.childOrders && order.childOrders.length > 0
    }))
    activeTotal.value = result.total || 0
  } catch (error) {
    ElMessage.error('加载当前订单失败')
  } finally {
    activeLoading.value = false
  }
}

const loadActiveNowOrders = async () => {
  activeNowLoading.value = true
  try {
    const result = await getActiveOrdersNow(activeNowQueryForm)
    activeNowOrders.value = (result.list || []).map((order: any) => ({
      ...order,
      hasChildren: order.childOrders && order.childOrders.length > 0
    }))
    activeNowTotal.value = result.total || 0
  } catch (error) {
    ElMessage.error('加载进行中订单失败')
  } finally {
    activeNowLoading.value = false
  }
}

const loadHistoryOrders = async () => {
  historyLoading.value = true
  try {
    const result = await getHistoryOrders(queryForm)
    historyOrders.value = (result.list || []).map((order: any) => ({
      ...order,
      hasChildren: order.childOrders && order.childOrders.length > 0
    }))
    total.value = result.total || 0
  } catch (error) {
    ElMessage.error('加载历史订单失败')
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

const handleActivePageChange = (page: number) => {
  activeQueryForm.page = page
  loadActiveOrders()
}

const handleActiveSizeChange = (size: number) => {
  activeQueryForm.pageSize = size
  activeQueryForm.page = 1
  loadActiveOrders()
}

const handleActiveNowPageChange = (page: number) => {
  activeNowQueryForm.page = page
  loadActiveNowOrders()
}

const handleActiveNowSizeChange = (size: number) => {
  activeNowQueryForm.pageSize = size
  activeNowQueryForm.page = 1
  loadActiveNowOrders()
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
 * 获取支付方式标签
 */
const getPaymentMethodLabel = (method: string) => {
  const labels: Record<string, string> = {
    offline: '线下支付',
    online: '线上支付',
    balance: '会员余额',
    combined: '组合支付'
  }
  return labels[method] || method
}

/**
 * 获取支付方式标签类型
 */
const getPaymentMethodTagType = (method: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined => {
  const types: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined> = {
    offline: undefined,
    online: 'primary',
    balance: 'success',
    combined: 'warning'
  }
  return types[method] || undefined
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

/* 自动刷新提示 */
.auto-refresh-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #e7f3ff 0%, #f0f9ff 100%);
  border: 1px solid #b3d8ff;
  border-radius: 6px;
  font-size: 14px;
  color: #409eff;
}

.auto-refresh-tip .refresh-icon {
  font-size: 16px;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
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

/* 支付方式样式 */
.payment-method-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.payment-breakdown {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 4px;
}

.payment-item {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 500;
}

.payment-item.balance {
  color: #67c23a;
  background-color: #e7f7e9;
}

.payment-item.offline {
  color: #f56c6c;
  background-color: #fef0f0;
}

.payment-time {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
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

/* 续费次数单元格 */
.extend-count-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Tree 表格样式 */
.order-table :deep(.el-table__expand-icon) {
  font-size: 16px;
}

.order-table :deep(.el-table__row--level-1 .el-table__cell) {
  background-color: #fafbfc;
}

.order-table :deep(.el-table__row--level-1:hover) {
  background-color: #f5f7fa !important;
}
</style>
