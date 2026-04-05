<template>
  <el-drawer
    v-model="drawerVisible"
    title="订单详情"
    size="500px"
    @close="handleClose"
  >
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>
    <div v-else-if="detail" class="order-detail">
      <!-- 订单基本信息 -->
      <section class="detail-section">
        <h3 class="section-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">桌台</span>
            <span class="value">{{ detail.tableName }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态</span>
            <el-tag :type="detail.status === 'active' ? 'success' : 'info'" size="small">
              {{ detail.status === 'active' ? '进行中' : '已完成' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">操作员</span>
            <span class="value">{{ detail.operatorName }}</span>
          </div>
          <div class="info-item">
            <span class="label">套餐时长</span>
            <span class="value">{{ detail.presetDuration ? formatDuration(detail.presetDuration) : '不设时长' }}</span>
          </div>
          <div v-if="detail.channel" class="info-item">
            <span class="label">渠道</span>
            <el-tag type="primary" size="small">{{ getChannelName(detail.channel) }}</el-tag>
          </div>
        </div>
      </section>

      <!-- 时间信息 -->
      <section class="detail-section">
        <h3 class="section-title">时间信息</h3>
        <div class="info-grid">
          <div class="info-item full-width">
            <span class="label">开始时间</span>
            <span class="value">{{ formatDateTime(detail.startTime) }}</span>
          </div>
          <div v-if="detail.endTime" class="info-item full-width">
            <span class="label">结束时间</span>
            <span class="value">{{ formatDateTime(detail.endTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">使用时长</span>
            <span class="value highlight">{{ formatDuration(detail.duration) }}</span>
          </div>
          <div class="info-item">
            <span class="label">暂停时长</span>
            <span class="value">{{ formatDuration(detail.pauseDuration) }}</span>
          </div>
          <div class="info-item">
            <span class="label">计费时长</span>
            <span class="value highlight">{{ formatDuration(detail.actualDuration) }}</span>
          </div>
        </div>
      </section>

      <!-- 费用明细 -->
      <section class="detail-section">
        <h3 class="section-title">费用明细</h3>
        <div class="amount-list">
          <div v-if="detail.originalAmount && detail.originalAmount > detail.amount" class="amount-item">
            <span class="label">原价</span>
            <span class="value original">¥{{ formatMoney(detail.originalAmount) }}</span>
          </div>
          <div v-if="(detail.discountAmount ?? 0) > 0" class="amount-item">
            <span class="label">会员优惠</span>
            <span class="value discount">-¥{{ formatMoney(detail.discountAmount ?? 0) }}</span>
          </div>
          <div class="amount-item">
            <span class="label">正常费用</span>
            <span class="value">¥{{ formatMoney(detail.normalAmount) }}</span>
          </div>
          <div v-if="(detail.overtimeAmount ?? 0) > 0" class="amount-item">
            <span class="label">超时费用</span>
            <span class="value overtime">¥{{ formatMoney(detail.overtimeAmount ?? 0) }}</span>
          </div>
          <div class="amount-item total">
            <span class="label">实付金额</span>
            <span class="value total-amount">¥{{ formatMoney(detail.amount) }}</span>
          </div>
        </div>
      </section>

      <!-- 支付方式信息 -->
      <section v-if="detail.paymentMethod" class="detail-section">
        <h3 class="section-title">支付方式</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">支付方式</span>
            <el-tag :type="getPaymentMethodTagType(detail.paymentMethod)" size="small">
              {{ getPaymentMethodLabel(detail.paymentMethod) }}
            </el-tag>
          </div>
          <div v-if="(detail.balanceAmount ?? 0) > 0" class="info-item">
            <span class="label">余额支付</span>
            <span class="value balance">¥{{ formatMoney(detail.balanceAmount ?? 0) }}</span>
          </div>
          <div v-if="(detail.otherPaymentAmount ?? 0) > 0" class="info-item">
            <span class="label">线下支付</span>
            <span class="value offline">¥{{ formatMoney(detail.otherPaymentAmount ?? 0) }}</span>
          </div>
          <div v-if="detail.paidAt" class="info-item">
            <span class="label">支付时间</span>
            <span class="value">{{ formatDateTime(detail.paidAt) }}</span>
          </div>
        </div>
      </section>

      <!-- 会员信息 -->
      <section v-if="detail.memberName" class="detail-section">
        <h3 class="section-title">会员信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">会员姓名</span>
            <span class="value">{{ detail.memberName }}</span>
          </div>
          <div v-if="detail.memberLevelName" class="info-item">
            <span class="label">会员等级</span>
            <el-tag type="success" size="small">{{ detail.memberLevelName }}</el-tag>
          </div>
          <div v-if="detail.memberDiscountRate" class="info-item">
            <span class="label">折扣率</span>
            <span class="value discount">{{ (detail.memberDiscountRate * 10).toFixed(1) }}折</span>
          </div>
        </div>
      </section>

      <!-- 时间线 -->
      <section class="detail-section">
        <h3 class="section-title">操作记录</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in detail.timeLine"
            :key="index"
            :timestamp="formatDateTime(item.time)"
            placement="top"
          >
            <div class="timeline-content">
              <div class="timeline-action">{{ item.action }}</div>
              <div v-if="item.description" class="timeline-desc">{{ item.description }}</div>
              <div class="timeline-operator">操作人：{{ item.operator }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </section>
    </div>
    <div v-else class="empty-container">
      <el-empty description="订单详情加载失败" />
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { getOrderDetail } from '@/api/order'
import type { OrderDetail } from '@/api/order'
import { ElMessage } from 'element-plus'
import { useChannelTranslation } from '@/composables/useChannelTranslation'

// 渠道翻译
const { loadBillingRules, getChannelName } = useChannelTranslation()

// 初始化
onMounted(async () => {
  await loadBillingRules()
})

/**
 * 订单详情抽屉组件
 * 用于展示订单的详细信息，包括基本信息、时间信息、费用明细和操作时间线
 */
interface Props {
  modelValue: boolean
  orderId: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const detail = ref<OrderDetail | null>(null)

const drawerVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

watch(
  () => props.modelValue,
  (val) => {
    if (val && props.orderId) {
      loadOrderDetail()
    } else {
      detail.value = null
    }
  }
)

const loadOrderDetail = async () => {
  if (!props.orderId) return

  loading.value = true
  try {
    detail.value = await getOrderDetail(props.orderId)
  } catch (error) {
    ElMessage.error('加载订单详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
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

  const parts: string[] = []
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分`)
  if (secs > 0 || parts.length === 0) parts.push(`${secs}秒`)

  return parts.join('')
}

const formatMoney = (amount: number) => {
  return amount.toFixed(2)
}

// 获取支付方式标签
const getPaymentMethodLabel = (method: string) => {
  const labels: Record<string, string> = {
    offline: '线下支付',
    online: '线上支付',
    balance: '会员余额',
    combined: '组合支付'
  }
  return labels[method] || method
}

// 获取支付方式标签类型
const getPaymentMethodTagType = (method: string): 'success' | 'primary' | 'info' | 'warning' | 'danger' | undefined => {
  const types: Record<string, 'success' | 'primary' | 'info' | 'warning' | 'danger' | undefined> = {
    offline: undefined,
    online: 'primary',
    balance: 'success',
    combined: 'warning'
  }
  return types[method] ?? undefined
}
</script>

<style scoped>
.loading-container {
  padding: 20px;
}

.order-detail {
  padding-bottom: 20px;
}

.detail-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.detail-section:last-of-type {
  border-bottom: none;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item .label {
  font-size: 12px;
  color: #909399;
}

.info-item .value {
  font-size: 14px;
  color: #303133;
}

.info-item .value.highlight {
  font-weight: 600;
  color: #409eff;
}

.info-item .value.discount {
  font-weight: 600;
  color: #67c23a;
}

.info-item .value.balance {
  font-weight: 600;
  color: #67c23a;
}

.info-item .value.offline {
  font-weight: 600;
  color: #f56c6c;
}

.amount-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.amount-item .label {
  font-size: 14px;
  color: #606266;
}

.amount-item .value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.amount-item .value.overtime {
  color: #e6a23c;
}

.amount-item .value.original {
  color: #909399;
  text-decoration: line-through;
}

.amount-item .value.discount {
  color: #67c23a;
}

.amount-item.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-top: 8px;
}

.amount-item.total .label {
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
}

.amount-item.total .total-amount {
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
}

.timeline-content {
  padding-left: 8px;
}

.timeline-action {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.timeline-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.timeline-operator {
  font-size: 12px;
  color: #909399;
}

.empty-container {
  padding: 60px 0;
}
</style>
