<template>
  <el-dialog
    v-model="dialogVisible"
    title="结账确认"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
    <div v-else-if="bill" class="bill-content">
      <!-- 桌台信息 -->
      <div class="bill-section">
        <h3 class="section-title">桌台信息</h3>
        <div class="info-row">
          <span class="label">桌台</span>
          <span class="value">{{ bill.tableName }}</span>
        </div>
      </div>

      <!-- 时间信息 -->
      <div class="bill-section">
        <h3 class="section-title">时间信息</h3>
        <div class="info-row">
          <span class="label">开始时间</span>
          <span class="value">{{ formatDateTime(bill.startTime) }}</span>
        </div>
        <div class="info-row">
          <span class="label">使用时长</span>
          <span class="value highlight">{{ formatDuration(bill.duration) }}</span>
        </div>
        <div class="info-row">
          <span class="label">暂停时长</span>
          <span class="value">{{ formatDuration(bill.pauseDuration) }}</span>
        </div>
        <div class="info-row" v-if="bill.presetDuration">
          <span class="label">套餐时长</span>
          <span class="value">{{ formatDuration(bill.presetDuration) }}</span>
        </div>
        <div class="info-row">
          <span class="label">计费时长</span>
          <span class="value highlight">{{ formatDuration(bill.actualDuration) }}</span>
        </div>
      </div>

      <!-- 会员信息 -->
      <div class="bill-section" v-if="bill.member">
        <h3 class="section-title">会员信息</h3>
        <div class="info-row">
          <span class="label">会员姓名</span>
          <span class="value">{{ bill.member.name }}</span>
        </div>
        <div class="info-row">
          <span class="label">会员等级</span>
          <span class="value">
            <el-tag type="success" size="small">{{ bill.member.levelName }}</el-tag>
            <span style="margin-left: 8px;">{{ (bill.member.discountRate * 10).toFixed(1) }}折</span>
          </span>
        </div>
        <div class="info-row">
          <span class="label">会员余额</span>
          <span class="value member-balance">¥{{ formatMoney(bill.member.balance) }}</span>
        </div>
      </div>

      <!-- 费用明细 -->
      <div class="bill-section">
        <h3 class="section-title">费用明细</h3>

        <!-- 原价 -->
        <div class="amount-row" v-if="bill.originalAmount && bill.originalAmount > 0">
          <span class="label">原价</span>
          <span class="value original">¥{{ formatMoney(bill.originalAmount) }}</span>
        </div>

        <!-- 会员优惠 -->
        <div class="amount-row" v-if="bill.member && bill.member.discountAmount && bill.member.discountAmount > 0">
          <span class="label">会员优惠</span>
          <span class="value discount">-¥{{ formatMoney(bill.member.discountAmount) }}</span>
        </div>

        <div class="amount-row">
          <span class="label">正常费用</span>
          <span class="value">¥{{ formatMoney(bill.amountDetail?.normalAmount) }}</span>
        </div>
        <div class="amount-row" v-if="bill.amountDetail?.overtimeAmount > 0">
          <span class="label">超时费用</span>
          <span class="value overtime">¥{{ formatMoney(bill.amountDetail?.overtimeAmount) }}</span>
        </div>

        <!-- 支付方式信息 -->
        <div v-if="bill.paymentMethod" class="payment-method-section">
          <div class="payment-method-title">支付方式</div>
          <div class="payment-method-info">
            <div class="payment-method-name">{{ getPaymentMethodLabel(bill.paymentMethod) }}</div>
            <!-- 组合支付明细 -->
            <div v-if="bill.paymentMethod === 'combined' && (bill.balanceAmount >= 0 || bill.otherPaymentAmount >= 0)" class="payment-breakdown">
              <div class="payment-breakdown-header">支付明细</div>
              <div v-if="bill.balanceAmount != null && bill.balanceAmount > 0" class="payment-breakdown-item balance">
                <span class="breakdown-label">
                  <el-icon><WalletFilled /></el-icon>
                  会员余额支付
                </span>
                <span class="breakdown-value">-¥{{ formatMoney(bill.balanceAmount) }}</span>
              </div>
              <div v-if="bill.otherPaymentAmount != null && bill.otherPaymentAmount > 0" class="payment-breakdown-item offline">
                <span class="breakdown-label">
                  <el-icon><ShoppingCart /></el-icon>
                  线下支付
                </span>
                <span class="breakdown-value">-¥{{ formatMoney(bill.otherPaymentAmount) }}</span>
              </div>
            </div>
            <!-- 余额支付明细 -->
            <div v-if="bill.paymentMethod === 'balance' && bill.balanceAmount != null && bill.balanceAmount >= 0" class="payment-breakdown">
              <div class="payment-breakdown-item balance">
                <span class="breakdown-label">
                  <el-icon><WalletFilled /></el-icon>
                  会员余额支付
                </span>
                <span class="breakdown-value">-¥{{ formatMoney(bill.balanceAmount) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="amount-row total">
          <span class="label">应付金额</span>
          <span class="value total-amount">¥{{ formatMoney(bill.amountDetail?.totalAmount) }}</span>
        </div>
      </div>

      <!-- 操作员 -->
      <div class="bill-section">
        <div class="info-row">
          <span class="label">操作员</span>
          <span class="value">{{ bill.operatorName }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :loading="confirming">
          确认结账
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { WalletFilled, ShoppingCart } from '@element-plus/icons-vue'
import { getTableBill, endTable, PAYMENT_METHOD_LABELS } from '@/api/table'
import type { BillInfo, EndTableParams } from '@/api/table'

/**
 * 账单弹窗组件
 * 用于展示桌台结账信息并确认结账
 */
interface Props {
  modelValue: boolean
  tableId: number | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirmed'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const confirming = ref(false)
const bill = ref<BillInfo | null>(null)

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

watch(
  () => props.modelValue,
  (val) => {
    if (val && props.tableId) {
      loadBill()
    } else {
      bill.value = null
    }
  }
)

const loadBill = async () => {
  if (!props.tableId) return

  loading.value = true
  try {
    bill.value = await getTableBill(props.tableId)
    console.log('=== 账单数据 ===')
    console.log('支付方式:', bill.value.paymentMethod)
    console.log('余额支付金额:', bill.value.balanceAmount)
    console.log('其他支付金额:', bill.value.otherPaymentAmount)
    console.log('总金额:', bill.value.amountDetail?.totalAmount)
    console.log('完整账单:', bill.value)
    console.log('===============')
  } catch (error) {
    ElMessage.error('获取账单失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleConfirm = async () => {
  if (!props.tableId) return

  confirming.value = true
  try {
    // 传递结账参数：会员ID和支付方式
    const params: EndTableParams = {}

    // 如果账单中有会员信息，传递会员ID
    if (bill.value?.member?.id) {
      params.memberId = bill.value.member.id
    }

    // 如果账单中有支付方式，传递支付方式
    if (bill.value?.paymentMethod) {
      params.paymentMethod = bill.value.paymentMethod
    }

    await endTable(props.tableId, Object.keys(params).length > 0 ? params : undefined)
    ElMessage.success('结账成功')
    emit('confirmed')
    emit('update:modelValue', false)
  } catch (error) {
    ElMessage.error('结账失败')
    console.error(error)
  } finally {
    confirming.value = false
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
    minute: '2-digit'
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
  return amount?.toFixed(2) || '0.00'
}

const getPaymentMethodLabel = (method: string) => {
  return PAYMENT_METHOD_LABELS[method as keyof typeof PAYMENT_METHOD_LABELS] || method
}
</script>

<style scoped>
.loading-container {
  padding: 20px;
}

.bill-content {
  padding: 10px 0;
}

.bill-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.bill-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  font-size: 14px;
  color: #606266;
}

.info-row .value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.info-row .value.highlight {
  color: #409eff;
  font-weight: 600;
}

.info-row .value.member-balance {
  color: #67c23a;
  font-weight: 600;
  font-size: 16px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.amount-row:last-child {
  margin-bottom: 0;
}

.amount-row .label {
  font-size: 14px;
  color: #606266;
}

.amount-row .value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.amount-row .value.overtime {
  color: #e6a23c;
}

.amount-row .value.original {
  color: #909399;
  text-decoration: line-through;
}

.amount-row .value.discount {
  color: #67c23a;
  font-weight: 600;
}

.amount-row.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-top: 12px;
}

.amount-row.total .label {
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
}

.amount-row.total .total-amount {
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.payment-method-section {
  margin-top: 16px;
  padding: 12px;
  background: #f0f9ff;
  border: 1px solid #67c23a;
  border-radius: 4px;
}

.payment-method-title {
  font-size: 13px;
  font-weight: 600;
  color: #67c23a;
  margin-bottom: 8px;
}

.payment-method-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.payment-method-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.payment-breakdown {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
  padding-left: 12px;
}

.payment-breakdown-header {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 4px;
}

.payment-breakdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  padding: 6px 8px;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.6);
}

.payment-breakdown-item.balance {
  background-color: #e7f7e9;
  border-left: 3px solid #67c23a;
}

.payment-breakdown-item.offline {
  background-color: #fef0f0;
  border-left: 3px solid #f56c6c;
}

.breakdown-label {
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}

.breakdown-label .el-icon {
  font-size: 14px;
}

.payment-breakdown-item.balance .breakdown-label {
  color: #67c23a;
}

.payment-breakdown-item.offline .breakdown-label {
  color: #f56c6c;
}

.breakdown-value {
  color: #303133;
  font-weight: 600;
}

.payment-breakdown-item.balance .breakdown-value {
  color: #67c23a;
}

.payment-breakdown-item.offline .breakdown-value {
  color: #f56c6c;
}
</style>
