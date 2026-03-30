<template>
  <el-drawer
    v-model="visible"
    title="订单详情"
    :size="600"
    @close="handleClose"
  >
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>
    <div v-else-if="orderDetail" class="order-detail">
      <!-- 基本信息 -->
      <div class="detail-section">
        <h4 class="section-title">基本信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单ID">{{ orderDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="桌台名称">{{ orderDetail.tableName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag
              :type="orderDetail.status === 'active' ? 'success' : 'info'"
              size="small"
            >
              {{ orderDetail.status === 'active' ? '进行中' : '已完成' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作员">{{ orderDetail.operatorName }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 时间信息 -->
      <div class="detail-section">
        <h4 class="section-title">时间信息</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="开始时间">
            {{ formatDateTime(orderDetail.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间" v-if="orderDetail.endTime">
            {{ formatDateTime(orderDetail.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="支付时间" v-if="orderDetail.paidAt">
            {{ formatDateTime(orderDetail.paidAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 时长信息 -->
      <div class="detail-section">
        <h4 class="section-title">时长信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总时长">
            {{ formatDuration(orderDetail.duration) }}
          </el-descriptions-item>
          <el-descriptions-item label="暂停时长">
            {{ formatDuration(orderDetail.pauseDuration) }}
          </el-descriptions-item>
          <el-descriptions-item label="实际使用时长">
            {{ formatDuration(orderDetail.actualDuration) }}
          </el-descriptions-item>
          <el-descriptions-item label="预设时长" v-if="orderDetail.presetDuration">
            {{ formatDuration(orderDetail.presetDuration) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 费用明细 -->
      <div class="detail-section">
        <h4 class="section-title">费用明细</h4>
        <div class="amount-detail">
          <div class="amount-item">
            <span class="amount-label">正常费用：</span>
            <span class="amount-value">¥{{ formatMoney(orderDetail.normalAmount) }}</span>
          </div>
          <div class="amount-item" v-if="orderDetail.overtimeAmount > 0">
            <span class="amount-label">超时费用：</span>
            <span class="amount-value overtime">¥{{ formatMoney(orderDetail.overtimeAmount) }}</span>
          </div>
          <div class="amount-item total">
            <span class="amount-label">总计：</span>
            <span class="amount-value total-amount">¥{{ formatMoney(orderDetail.amount) }}</span>
          </div>
        </div>
      </div>

      <!-- 时间线 -->
      <div class="detail-section" v-if="orderDetail.timeLine && orderDetail.timeLine.length > 0">
        <h4 class="section-title">操作时间线</h4>
        <el-timeline>
          <el-timeline-item
            v-for="item in orderDetail.timeLine"
            :key="item.time"
            :timestamp="formatDateTime(item.time)"
            placement="top"
          >
            <div class="timeline-content">
              <div class="timeline-action">{{ item.action }}</div>
              <div class="timeline-desc">{{ item.description }}</div>
              <div class="timeline-operator">操作人：{{ item.operator }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getOrderDetail } from '@/api/order'
import type { OrderDetail } from '@/api/order'
import { ElMessage } from 'element-plus'

interface Props {
  modelValue: boolean
  orderId: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(false)
const loading = ref(false)
const orderDetail = ref<OrderDetail | null>(null)

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.orderId) {
      loadOrderDetail()
    }
  },
  { immediate: true }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const loadOrderDetail = async () => {
  if (!props.orderId) return

  loading.value = true
  try {
    const data = await getOrderDetail(props.orderId)
    orderDetail.value = data
  } catch (error) {
    ElMessage.error('加载订单详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  visible.value = false
  orderDetail.value = null
}

const formatDateTime = (timestamp: number) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
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
.loading-container {
  padding: 20px;
}

.order-detail {
  padding: 0 20px;
}

.detail-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

.amount-detail {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e4e7ed;
}

.amount-item:last-child {
  border-bottom: none;
}

.amount-label {
  font-size: 14px;
  color: #606266;
}

.amount-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.amount-value.overtime {
  color: #e6a23c;
}

.amount-item.total {
  margin-top: 10px;
  padding-top: 15px;
  border-top: 2px solid #409eff;
}

.total-amount {
  font-size: 20px;
  color: #409eff;
}

.timeline-content {
  padding-left: 10px;
}

.timeline-action {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.timeline-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 3px;
}

.timeline-operator {
  font-size: 12px;
  color: #909399;
}
</style>
