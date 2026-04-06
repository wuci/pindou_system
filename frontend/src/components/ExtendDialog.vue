<template>
  <el-dialog
    v-model="visible"
    title="续费时长"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" label-width="100px" v-loading="loadingRules">
      <!-- 桌台信息 -->
      <el-form-item label="桌台编号">
        <el-tag type="primary" size="large">{{ table?.name }}</el-tag>
      </el-form-item>

      <!-- 当前状态 -->
      <el-form-item label="当前状态">
        <div class="current-status-inline">
          <span v-if="table?.presetDuration">
            套餐：<span class="value">{{ formatDuration(table.presetDuration) }}</span>
          </span>
          <span v-if="remainingTime > 0">
            剩余：<span class="value green">{{ formatDuration(remainingTime) }}</span>
          </span>
          <span v-if="table?.duration">
            已用：<span class="value">{{ formatDuration(table.duration) }}</span>
          </span>
        </div>
      </el-form-item>

      <!-- 会员信息 -->
      <el-form-item label="会员">
        <!-- 桌台已有会员：直接展示会员信息 -->
        <div v-if="isMemberLocked && selectedMember" class="member-info-detail">
          <div class="member-info-row">
            <span class="member-name-inline">{{ selectedMember.name }}</span>
            <el-tag type="success" size="small">{{ selectedMember.levelName || '会员' }}</el-tag>
            <span class="discount-rate-inline">{{ (selectedMember.discountRate * 10).toFixed(1) }}折</span>
          </div>
          <div class="member-info-row">
            <span class="member-balance-label">会员余额：</span>
            <span class="member-balance-value">¥{{ selectedMember.balance.toFixed(2) }}</span>
          </div>
        </div>
        <!-- 桌台无会员：允许选择会员 -->
        <template v-else>
          <div class="member-selection-inline">
            <el-switch
              v-model="isMember"
              active-text="会员"
              inactive-text="非会员"
              @change="handleMemberToggle"
            />
            <el-button
              v-if="isMember"
              type="primary"
              size="small"
              @click="showMemberDialog"
              style="margin-left: 12px"
            >
              {{ selectedMember ? selectedMember.name : '选择会员' }}
            </el-button>
            <div v-if="selectedMember" class="member-info-detail">
              <div class="member-info-row">
                <el-tag type="success" size="small">{{ selectedMember.levelName || '会员' }}</el-tag>
                <span style="margin-left: 8px;">{{ (selectedMember.discountRate * 10).toFixed(1) }}折</span>
              </div>
              <div class="member-info-row">
                <span class="member-balance-label">会员余额：</span>
                <span class="member-balance-value">¥{{ selectedMember.balance.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </template>
      </el-form-item>

      <!-- 渠道选择 -->
      <el-form-item label="套餐类型">
        <el-select v-model="selectedChannel" placeholder="请选择套餐类型" @change="handleChannelChange" style="width: 100%">
          <el-option
            v-for="channel in billingRules?.channels || []"
            :key="channel.channel"
            :label="channel.channelName"
            :value="channel.channel"
          />
        </el-select>
      </el-form-item>

      <!-- 续费时长选择 -->
      <el-form-item label="续费时长" required>
        <el-radio-group v-model="selectedRuleIndex" @change="handleRuleChange" class="rules-radio-group">
          <el-radio
            v-for="(rule, index) in currentRules"
            :key="index"
            :value="index"
            class="rule-radio"
          >
            <span class="rule-label">
              <span class="rule-time">{{ formatRuleTime(rule) }}</span>
              <span class="rule-price">¥{{ rule.price }}</span>
            </span>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 费用信息 -->
      <el-form-item v-if="selectedRuleIndex !== 'unlimited'" label="费用">
        <div class="extend-price-summary-inline">
          <!-- 当前订单费用 -->
          <div class="price-item-inline">
            <span class="price-title">当前订单：</span>
            <span class="price-value-inline">¥{{ currentOrderOriginalAmount.toFixed(2) }}</span>
            <span v-if="selectedMember && memberDiscount > 0" class="price-final">
              → <span class="discount">¥{{ (currentOrderOriginalAmount * memberDiscount).toFixed(2) }}</span>
            </span>
            <span v-if="table?.originalAmount && table.originalAmount < table.amount" class="price-paid">
              (已付¥{{ currentOrderAmount.toFixed(2) }})
            </span>
          </div>

          <!-- 续费费用 -->
          <div class="price-item-inline">
            <span class="price-title">本次续费：</span>
            <span class="price-value-inline">¥{{ extendOriginalPrice.toFixed(2) }}</span>
            <span v-if="selectedMember && memberDiscount > 0" class="price-final">
              → <span class="discount">¥{{ extendFinalPrice.toFixed(2) }}</span>
            </span>
          </div>

          <!-- 合计 -->
          <div class="price-item-inline total">
            <span class="price-title">合计：</span>
            <span class="price-value-inline total">¥{{ totalOriginalAmount.toFixed(2) }}</span>
            <span v-if="selectedMember" class="price-final">
              → <span class="discount">¥{{ totalFinalAmount.toFixed(2) }}</span>
              <span v-if="memberDiscount > 0" class="discount-tag-inline">
                ({{ (memberDiscount * 10).toFixed(1) }}折)
              </span>
            </span>
          </div>
        </div>
      </el-form-item>

      <!-- 支付方式选择 -->
      <el-form-item label="支付方式">
        <el-select
          v-model="selectedPaymentMethod"
          placeholder="请选择支付方式"
          style="width: 100%"
        >
          <el-option
            v-for="method in availablePaymentMethods"
            :key="method.value"
            :label="method.label"
            :value="method.value"
          >
            <div class="payment-option-content">
              <span class="payment-option-label">{{ method.label }}</span>
              <span v-if="method.description" class="payment-option-description">{{ method.description }}</span>
            </div>
          </el-option>
        </el-select>
        <!-- 组合支付信息提示 -->
        <div v-if="selectedPaymentMethod === 'combined' && combinedPaymentInfo" class="combined-payment-info">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ combinedPaymentInfo.description }}</span>
        </div>
        <!-- 会员余额提示 -->
        <div v-if="selectedPaymentMethod === 'balance' && selectedMember" class="balance-amount-info">
          <span v-if="isBalanceSufficient" class="balance-sufficient">
            <el-icon><SuccessFilled /></el-icon>
            余额充足，当前余额 ¥{{ selectedMember.balance.toFixed(2) }}
          </span>
          <span v-else class="balance-insufficient">
            <el-icon><WarningFilled /></el-icon>
            余额不足，当前余额 ¥{{ selectedMember.balance.toFixed(2) }}，总需支付 ¥{{ finalAmountForDisplay.toFixed(2) }}（当前订单¥{{ currentOrderOriginalAmount.toFixed(2) }} + 续费¥{{ extendOriginalPrice.toFixed(2) }}），还差 ¥{{ (finalAmountForDisplay - selectedMember.balance).toFixed(2) }}
          </span>
        </div>
      </el-form-item>

      <!-- 自定义时长输入 -->
      <el-form-item v-if="selectedRuleIndex === 'custom'" label="自定义时长" required>
        <el-input-number
          v-model="customHours"
          :min="1"
          :max="24"
          :step="1"
          controls-position="right"
          style="width: 120px"
        />
        <span style="margin: 0 8px">小时</span>
        <el-input-number
          v-model="customMinutes"
          :min="0"
          :max="59"
          :step="1"
          controls-position="right"
          style="width: 120px"
        />
        <span style="margin-left: 8px">分钟</span>
      </el-form-item>

      <!-- 续费后预计 -->
      <el-form-item v-if="selectedRuleIndex !== 'unlimited'" label="续费后">
        <span class="duration-preview">{{ extensionPreview }}</span>
      </el-form-item>

      <!-- 活动折扣 -->
      <el-form-item label="活动折扣">
        <el-checkbox v-model="form.applyActivityDiscount" @change="handleActivityDiscountChange">
          应用活动折扣
        </el-checkbox>
      </el-form-item>

      <!-- 折扣选择 -->
      <el-form-item v-if="form.applyActivityDiscount" label="选择折扣" required>
        <el-select
          v-model="form.discountId"
          placeholder="请选择折扣"
          style="width: 100%"
          @change="handleDiscountChange"
        >
          <el-option
            v-for="discount in availableDiscounts"
            :key="discount.id"
            :label="formatDiscountLabel(discount)"
            :value="discount.id"
          >
            <div class="discount-option">
              <span class="discount-name">{{ discount.name }}</span>
              <span class="discount-type">{{ discount.typeName }}</span>
              <span class="discount-rate">{{ (discount.discountRate * 10).toFixed(1) }}折</span>
            </div>
          </el-option>
        </el-select>
        <div v-if="selectedDiscount" class="selected-discount-info">
          <span v-if="selectedDiscount.minAmount" class="discount-condition">
            满¥{{ selectedDiscount.minAmount }}可用
          </span>
          <span v-if="selectedDiscount.maxDiscount" class="discount-condition">
            最高优惠¥{{ selectedDiscount.maxDiscount }}
          </span>
          <span v-if="selectedDiscount.description" class="discount-desc">
            {{ selectedDiscount.description }}
          </span>
        </div>
      </el-form-item>

      <!-- 备注 -->
      <el-form-item label="备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息（可选）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleConfirm">
          确认续费
        </el-button>
      </div>
    </template>

    <!-- 会员选择弹窗 -->
    <MemberSelectDialog
      v-model="memberDialogVisible"
      @selected="handleMemberSelected"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { TableInfo } from '@/api/table'
import { extendTable, NON_MEMBER_PAYMENT_METHODS, MEMBER_PAYMENT_METHODS, type PaymentMethod } from '@/api/table'
import { getBillingRuleConfig, type BillingRule, type BillingRuleItem } from '@/api/config'
import { getActiveDiscounts, calculateDiscountById, type DiscountInfo } from '@/api/discount'
import type { MemberInfo } from '@/api/member'
import MemberSelectDialog from '@/components/MemberSelectDialog.vue'

interface Props {
  modelValue: boolean
  table: TableInfo | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const loadingRules = ref(false)

// 标志：是否正在初始化
const isInitializing = ref(false)

// 会员相关
const isMember = ref(false)
const selectedMember = ref<MemberInfo | null>(null)
const memberDialogVisible = ref(false)

// 活动折扣相关
const availableDiscounts = ref<DiscountInfo[]>([])
const selectedDiscount = ref<DiscountInfo | null>(null)

// 支付方式
const selectedPaymentMethod = ref<PaymentMethod>('offline')

// 可用的支付方式列表
const availablePaymentMethods = computed(() => {
  return isMember.value ? MEMBER_PAYMENT_METHODS : NON_MEMBER_PAYMENT_METHODS
})

// 组合支付信息计算
const combinedPaymentInfo = computed(() => {
  if (selectedPaymentMethod.value !== 'combined' || !selectedMember.value) {
    return null
  }
  const balance = selectedMember.value.balance || 0
  const totalAmount = totalFinalAmount.value || totalOriginalAmount.value || 0
  if (balance >= totalAmount) {
    return {
      balanceAmount: totalAmount,
      otherAmount: 0,
      description: '余额充足，全部使用余额支付'
    }
  } else {
    return {
      balanceAmount: balance,
      otherAmount: totalAmount - balance,
      description: `余额${balance.toFixed(2)}元 + 线下${(totalAmount - balance).toFixed(2)}元`
    }
  }
})

// 会员余额是否充足
const isBalanceSufficient = computed(() => {
  if (!selectedMember.value) return false
  const balance = selectedMember.value.balance || 0
  const totalAmount = totalFinalAmount.value || totalOriginalAmount.value || 0
  return balance >= totalAmount
})

// 用于显示的最终金额
const finalAmountForDisplay = computed(() => {
  return totalFinalAmount.value || totalOriginalAmount.value || 0
})

// 判断会员是否被锁定（桌台已有会员，不允许修改）
const isMemberLocked = computed(() => {
  return !!props.table?.memberId
})

// 表单数据
const form = ref({
  additionalDuration: 0, // 秒
  remark: '',
  applyActivityDiscount: false, // 是否应用活动折扣
  discountId: '' // 选择的折扣ID
})

// 计费规则数据
const billingRules = ref<BillingRule | null>(null)
const selectedChannel = ref<string>('')

// 选中的规则索引或特殊值
const selectedRuleIndex = ref<number | 'custom' | 'unlimited'>(0)

// 自定义时长
const customHours = ref(1)
const customMinutes = ref(0)

// 对话框显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 计算剩余时间
const remainingTime = computed(() => {
  if (!props.table?.presetDuration) {
    return 0
  }
  const remaining = props.table.presetDuration - (props.table.duration || 0)
  return Math.max(0, remaining)
})

// 格式化时长为 HH:MM:SS
const formatDuration = (seconds: number): string => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  const pad = (num: number) => num.toString().padStart(2, '0')
  return `${pad(hours)}:${pad(minutes)}:${pad(secs)}`
}

// 格式化规则时间显示
const formatRuleTime = (rule: BillingRuleItem): string => {
  if (rule.unlimited) {
    return '不限时'
  }
  const totalMinutes = rule.minutes || 0
  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60

  if (hours > 0 && minutes > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${minutes}分钟`
  }
}

// 续费后预览
const extensionPreview = computed(() => {
  if (selectedRuleIndex.value === 'unlimited') {
    return '改为不限时'
  }

  let additionalSeconds = 0
  if (selectedRuleIndex.value === 'custom') {
    additionalSeconds = customHours.value * 3600 + customMinutes.value * 60
  } else if (typeof selectedRuleIndex.value === 'number') {
    const rule = currentRules.value[selectedRuleIndex.value]
    if (rule && !rule.unlimited && rule.minutes) {
      additionalSeconds = rule.minutes * 60
    } else {
      return '不限时'
    }
  }

  // 计算续费后的预计总时长
  const newPresetDuration = props.table?.presetDuration
    ? props.table.presetDuration + additionalSeconds
    : additionalSeconds

  const hours = Math.floor(newPresetDuration / 3600)
  const minutes = Math.floor((newPresetDuration % 3600) / 60)

  if (hours > 0 && minutes > 0) {
    return `总套餐时长: ${hours}小时${minutes}分钟`
  } else if (hours > 0) {
    return `总套餐时长: ${hours}小时`
  } else {
    return `总套餐时长: ${minutes}分钟`
  }
})

// 当前渠道的规则
const currentRules = computed(() => {
  if (!billingRules.value || !billingRules.value.channels || !Array.isArray(billingRules.value.channels)) return []
  const channel = billingRules.value.channels.find(c => c.channel === selectedChannel.value)
  return channel?.rules || []
})

// 会员折扣率
const memberDiscount = computed(() => {
  return selectedMember.value?.discountRate || 0
})

// 当前选择的价格
const currentRulePrice = computed(() => {
  if (typeof selectedRuleIndex.value === 'number') {
    const rule = currentRules.value[selectedRuleIndex.value]
    return rule?.price || 0
  }
  return 0
})

// 原价
const originalPrice = computed(() => {
  return currentRulePrice.value
})

// 当前订单原价
const currentOrderOriginalAmount = computed(() => {
  return props.table?.originalAmount || props.table?.amount || 0
})

// 当前订单实付
const currentOrderAmount = computed(() => {
  return props.table?.amount || 0
})

// 续费原价
const extendOriginalPrice = computed(() => {
  return originalPrice.value
})

// 续费折后价
const extendFinalAmount = ref<number>(0) // 活动折扣计算的续费折后价

const extendFinalPrice = computed(() => {
  // 如果有活动折扣计算结果，优先使用
  if (form.value.applyActivityDiscount && extendFinalAmount.value > 0) {
    return extendFinalAmount.value
  }

  // 否则使用会员折扣
  if (selectedMember.value && memberDiscount.value > 0) {
    return extendOriginalPrice.value * memberDiscount.value
  }
  return extendOriginalPrice.value
})

// 总原价（当前订单原价 + 续费原价）
const totalOriginalAmount = computed(() => {
  return currentOrderOriginalAmount.value + extendOriginalPrice.value
})

// 总折后价
const totalFinalAmount = computed(() => {
  // 如果有活动折扣计算结果，使用：当前订单已付 + 续费折后价
  if (form.value.applyActivityDiscount && extendFinalAmount.value > 0) {
    return (currentOrderAmount.value || 0) + extendFinalAmount.value
  }
  // 否则使用会员折扣
  if (selectedMember.value && memberDiscount.value > 0) {
    return totalOriginalAmount.value * memberDiscount.value
  }
  return totalOriginalAmount.value
})

// 加载计费规则
const loadBillingRules = async () => {
  try {
    loadingRules.value = true
    const configStr = await getBillingRuleConfig()

    let parsed: any
    try {
      parsed = JSON.parse(configStr)
    } catch (parseError) {
      ElMessage.error('计费规则数据格式错误，请前往设置页面重新配置')
      setDefaultBillingRules()
      return
    }

    if (!parsed || typeof parsed !== 'object' || !parsed.channels || !Array.isArray(parsed.channels)) {
      ElMessage.error('计费规则数据格式错误，请前往设置页面重新配置')
      setDefaultBillingRules()
      return
    }

    // 验证每个渠道的数据结构
    for (const channel of parsed.channels) {
      if (!channel.channel || !channel.channelName || !Array.isArray(channel.rules)) {
        ElMessage.error('计费规则数据格式错误，请前往设置页面重新配置')
        setDefaultBillingRules()
        return
      }
    }

    billingRules.value = parsed

    await nextTick()

    // 默认选中第一个规则（注意：不在这里设置渠道，由 initializeForm 设置）
    if (currentRules.value.length > 0) {
      selectedRuleIndex.value = 0
    } else {
      selectedRuleIndex.value = 'unlimited'
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载计费规则失败')
    setDefaultBillingRules()
  } finally {
    loadingRules.value = false
  }
}

// 设置默认计费规则
const setDefaultBillingRules = () => {
  const defaultRules = [
    { minutes: 60, price: 19, unlimited: false },
    { minutes: 120, price: 35, unlimited: false },
    { minutes: 240, price: 54, unlimited: false },
    { minutes: null, price: 68, unlimited: true }
  ]

  billingRules.value = {
    channels: [
      { channel: 'store', channelName: '店内', rules: JSON.parse(JSON.stringify(defaultRules)) },
      { channel: 'meituan', channelName: '美团', rules: JSON.parse(JSON.stringify(defaultRules)) },
      { channel: 'dianping', channelName: '大众点评', rules: JSON.parse(JSON.stringify(defaultRules)) }
    ]
  }
}

// 渠道切换处理
const handleChannelChange = () => {
  if (isInitializing.value) {
    return
  }

  if (currentRules.value.length > 0) {
    customHours.value = 1
    customMinutes.value = 0
    selectedRuleIndex.value = 0
  } else {
    selectedRuleIndex.value = 'unlimited'
  }
}

// 规则切换处理
const handleRuleChange = () => {
  // 规则切换时的处理逻辑
}

// 监听对话框打开
watch(() => props.modelValue, async (newVal) => {
  if (newVal) {
    if (!billingRules.value) {
      await loadBillingRules()
    }
    initializeForm()
  }
})

// 初始化表单
const initializeForm = () => {
  isInitializing.value = true

  form.value = {
    additionalDuration: 0,
    remark: '',
    applyActivityDiscount: false,
    discountId: ''
  }

  // 重置折扣相关
  availableDiscounts.value = []
  selectedDiscount.value = null
  extendFinalAmount.value = 0

  // 初始化会员信息
  isMember.value = false
  selectedMember.value = null

  // 重置支付方式为默认值
  selectedPaymentMethod.value = 'offline'

  // 如果桌台已有会员信息，自动设置会员（锁定状态）
  if (props.table?.memberId && props.table?.memberName) {
    isMember.value = true
    selectedMember.value = {
      id: props.table.memberId,
      name: props.table.memberName,
      phone: '',
      address: '',
      totalAmount: 0,
      balance: props.table.memberBalance || 0,
      levelId: 0,
      levelName: '',
      discountRate: props.table.memberDiscountRate || 1,
      createdAt: 0,
      updatedAt: 0
    }
  }

  // 使用当前桌台的渠道，如果桌台有渠道且该渠道在计费规则中存在，则使用它
  // 否则使用第一条渠道，最后兜底使用 'store'
  let targetChannel = props.table?.channel || 'store'

  // 检查目标渠道是否在计费规则中存在
  const channelExists = billingRules.value?.channels?.some((c: any) => c.channel === targetChannel)

  if (channelExists) {
    selectedChannel.value = targetChannel
  } else {
    // 使用第一条渠道
    const firstChannel = billingRules.value?.channels?.[0]?.channel
    selectedChannel.value = firstChannel || 'store'
  }

  nextTick(() => {
    if (currentRules.value.length > 0) {
      customHours.value = 1
      customMinutes.value = 0
      selectedRuleIndex.value = 0
    } else {
      selectedRuleIndex.value = 'unlimited'
    }

    nextTick(() => {
      isInitializing.value = false
    })
  })
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 重置表单
const resetForm = () => {
  form.value = {
    additionalDuration: 0,
    remark: '',
    applyActivityDiscount: false,
    discountId: ''
  }

  // 重置会员选择
  isMember.value = false
  selectedMember.value = null

  // 重置折扣相关
  availableDiscounts.value = []
  selectedDiscount.value = null
  extendFinalAmount.value = 0

  // 如果桌台已有会员信息，自动设置会员（锁定状态）
  if (props.table?.memberId && props.table?.memberName) {
    isMember.value = true
    selectedMember.value = {
      id: props.table.memberId,
      name: props.table.memberName,
      phone: '',
      address: '',
      totalAmount: 0,
      balance: props.table.memberBalance || 0,
      levelId: 0,
      levelName: '',
      discountRate: props.table.memberDiscountRate || 1,
      createdAt: 0,
      updatedAt: 0
    }
  }

  const defaultChannel = props.table?.channel || 'store'
  selectedChannel.value = defaultChannel
  nextTick(() => {
    if (currentRules.value.length > 0) {
      customHours.value = 1
      customMinutes.value = 0
      selectedRuleIndex.value = 0
    } else {
      selectedRuleIndex.value = 'unlimited'
    }
  })
  formRef.value?.clearValidate()
}

// 确认续费
const handleConfirm = async () => {
  if (!props.table) {
    ElMessage.error('桌台信息不存在')
    return
  }

  // 计算要增加的时长
  let additionalDuration = 0

  if (selectedRuleIndex.value === 'unlimited') {
    additionalDuration = 0
  } else if (selectedRuleIndex.value === 'custom') {
    additionalDuration = customHours.value * 3600 + customMinutes.value * 60
    if (additionalDuration <= 0) {
      ElMessage.error('请设置有效的时长')
      return
    }
  } else if (typeof selectedRuleIndex.value === 'number') {
    const rule = currentRules.value[selectedRuleIndex.value]
    if (rule && !rule.unlimited && rule.minutes) {
      additionalDuration = rule.minutes * 60
    } else {
      additionalDuration = 0
    }
  }

  // 验证会员余额是否足够（仅对会员余额支付方式）
  if (selectedMember.value && selectedPaymentMethod.value === 'balance') {
    const memberBalance = selectedMember.value.balance || 0
    const finalAmount = totalFinalAmount.value || totalOriginalAmount.value || 0

    if (memberBalance < finalAmount) {
      ElMessageBox.alert(
        `会员余额不足！<br/>当前余额：¥${memberBalance.toFixed(2)}<br/>总需支付：¥${finalAmount.toFixed(2)}（当前订单¥${(currentOrderOriginalAmount.value || 0).toFixed(2)} + 续费¥${(extendOriginalPrice.value || 0).toFixed(2)}）<br/>还差：¥${(finalAmount - memberBalance).toFixed(2)}`,
        '余额不足',
        {
          confirmButtonText: '知道了',
          dangerouslyUseHTMLString: true,
          type: 'warning'
        }
      )
      return
    }
  }

  loading.value = true

  try {
    await extendTable(props.table.id, {
      additionalDuration,
      channel: selectedChannel.value,
      memberId: selectedMember.value?.id,
      paymentMethod: selectedPaymentMethod.value,
      discountId: form.value.applyActivityDiscount ? form.value.discountId : undefined
    })

    ElMessage.success('续费成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '续费失败')
  } finally {
    loading.value = false
  }
}

// 初始化加载
onMounted(() => {
  if (props.modelValue) {
    loadBillingRules()
  }
})

// 暴露重置方法
defineExpose({
  reset: resetForm
})

// 会员切换处理
const handleMemberToggle = (value: string | number | boolean) => {
  // 如果会员被锁定（桌台已有会员），不允许切换
  if (isMemberLocked.value) {
    // 恢复为会员状态
    isMember.value = true
    ElMessage.warning('当前订单已绑定会员，无法修改')
    return
  }

  if (!value) {
    selectedMember.value = null
  } else {
    // 切换到会员时，自动弹出会员选择对话框
    showMemberDialog()
  }
  // 切换会员状态时，重置支付方式为默认值
  selectedPaymentMethod.value = 'offline'
}

// 显示会员选择对话框
const showMemberDialog = () => {
  // 如果会员被锁定（桌台已有会员），不允许更换会员
  if (isMemberLocked.value) {
    ElMessage.warning('当前订单已绑定会员，无法更换')
    return
  }
  memberDialogVisible.value = true
}

// 会员选择回调
const handleMemberSelected = (member: MemberInfo) => {
  selectedMember.value = member
  ElMessage.success(`已选择会员：${member.name}`)
  // 刷新折扣列表（因为会员等级变化了）
  if (form.value.applyActivityDiscount) {
    loadDiscounts()
  }
}

// 活动折扣切换处理
const handleActivityDiscountChange = (val: string | number | boolean) => {
  const checked = val === true || val === 'true'
  if (checked) {
    form.value.discountId = ''
    selectedDiscount.value = null
    extendFinalAmount.value = 0
    loadDiscounts()
  } else {
    form.value.discountId = ''
    selectedDiscount.value = null
    availableDiscounts.value = []
    extendFinalAmount.value = 0
  }
}

// 折扣选择变化处理
const handleDiscountChange = async (discountId: string) => {
  const discount = availableDiscounts.value.find(d => d.id === discountId)
  if (!discount) return
  selectedDiscount.value = discount

  // 计算总原价（当前订单 + 续费）
  const totalOriginalAmount = currentOrderOriginalAmount.value + (extendOriginalPrice.value || 0)

  // 如果选择了折扣且有总原价，调用API计算折扣
  if (discount && totalOriginalAmount > 0) {
    try {
      const response = await calculateDiscountById(discountId, totalOriginalAmount, selectedMember.value?.id)

      if (response && response.finalAmount !== undefined) {
        // 计算续费后的折后价
        const currentOrderPaid = currentOrderAmount.value || 0
        const totalDiscounted = response.finalAmount

        // 续费部分的折后价 = 总折后价 - 当前订单已付部分
        let extendFinalValue = totalDiscounted - currentOrderPaid
        if (extendFinalValue < 0) extendFinalValue = 0

        // 更新续费折后价显示
        extendFinalAmount.value = extendFinalValue
      }
    } catch (error: any) {
      // 恢复原价
      extendFinalAmount.value = extendOriginalPrice.value || 0
    }
  } else {
    // 未选择折扣，恢复原价
    extendFinalAmount.value = extendOriginalPrice.value || 0
  }
}

// 加载折扣列表
const loadDiscounts = async () => {
  try {
    const discounts = await getActiveDiscounts()
    // 根据会员等级过滤折扣
    let filteredDiscounts = discounts

    if (selectedMember.value) {
      // 有会员：显示所有折扣（固定折扣、活动折扣、匹配的会员折扣）
      filteredDiscounts = discounts.filter(d => {
        if (d.type === 2) { // 会员折扣
          return d.memberLevelId === selectedMember.value?.levelId
        }
        return true
      })
    } else {
      // 无会员：只显示固定折扣和活动折扣，过滤掉会员折扣
      filteredDiscounts = discounts.filter(d => d.type !== 2)
    }

    availableDiscounts.value = filteredDiscounts

    if (filteredDiscounts.length === 0) {
      ElMessage.warning('暂无可用折扣')
    }
  } catch (error: any) {
    ElMessage.error('加载折扣列表失败')
  }
}

// 格式化折扣选项标签
const formatDiscountLabel = (discount: DiscountInfo) => {
  return `${discount.name} - ${(discount.discountRate * 10).toFixed(1)}折`
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.current-status-inline {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.current-status-inline span {
  font-size: 14px;
  color: #606266;
}

.current-status-inline .value {
  font-weight: 600;
  color: #303133;
  margin-left: 4px;
}

.current-status-inline .value.green {
  color: #67c23a;
}

.current-status {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.current-status > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-value {
  font-weight: 500;
  color: #303133;
}

.status-value.remaining {
  color: #67C23A;
}

.duration-preview {
  font-size: 16px;
  font-weight: 500;
  color: #409EFF;
  padding: 8px 16px;
  background: #ecf5ff;
  border-radius: 4px;
}

.rules-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.rule-radio {
  margin-right: 0;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px 16px;
  transition: all 0.3s;
}

.rule-radio:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.rule-radio.is-checked {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.rule-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.rule-time {
  font-size: 14px;
  color: #303133;
}

.rule-price {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
}

:deep(.el-radio) {
  margin-right: 0;
  width: 100%;
}

:deep(.el-radio__label) {
  width: 100%;
}

:deep(.el-input-number) {
  width: 100%;
}

/* 价格显示样式 */
.price-display-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.price-item-inline {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-label {
  font-size: 14px;
  color: #606266;
}

.price-original {
  font-size: 16px;
  color: #909399;
  text-decoration: line-through;
  font-weight: 400;
}

.price-discount {
  font-size: 18px;
  color: #409eff;
  font-weight: 600;
}

.discount-tag {
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
}

.price-normal {
  font-size: 18px;
  color: #409eff;
  font-weight: 600;
}

/* 续费费用汇总样式 - 紧凑版 */
.extend-price-summary-inline {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.price-item-inline {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.price-item-inline.total {
  background: #ecf5ff;
  border: 1px solid #409eff;
}

.price-title {
  color: #606266;
  font-weight: 500;
}

.price-value-inline {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.price-value-inline.total {
  color: #409eff;
  font-size: 16px;
}

.price-paid {
  color: #909399;
  font-size: 13px;
}

.price-final {
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-final .discount {
  color: #67c23a;
  font-weight: 600;
  font-size: 17px;
}

.discount-tag-inline {
  color: #67c23a;
  font-size: 12px;
  font-weight: 500;
}

/* 续费费用汇总样式 - 旧版（保留兼容） */
.extend-price-summary {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.price-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.price-section.highlight {
  background: #ecf5ff;
  border-color: #409eff;
}

.price-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.price-section-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.price-section .price-value {
  font-weight: 600;
  color: #303133;
}

.price-section.highlight .price-value {
  color: #409eff;
  font-size: 16px;
}

.price-section.highlight .price-value.discount {
  color: #67c23a;
  font-size: 18px;
}

/* 会员信息展示样式 - 紧凑版 */
.member-info-inline {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-name-inline {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.discount-rate-inline {
  font-size: 14px;
  color: #67c23a;
  font-weight: 500;
}

.member-selection-inline {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

/* 会员信息展示样式 - 旧版（保留兼容） */
.member-info-display {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.member-details {
  display: flex;
  align-items: center;
  gap: 8px;
}

.discount-rate {
  font-size: 14px;
  color: #67c23a;
  font-weight: 500;
}

.payment-option-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.payment-option-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.payment-option-description {
  font-size: 12px;
  color: #909399;
}

.combined-payment-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px 12px;
  background-color: #f0f9ff;
  border: 1px solid #67c23a;
  border-radius: 4px;
  font-size: 13px;
  color: #67c23a;
}

.combined-payment-info .el-icon {
  font-size: 16px;
}

.balance-amount-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.balance-sufficient {
  background-color: #f0f9ff;
  border: 1px solid #67c23a;
  color: #67c23a;
}

.balance-insufficient {
  background-color: #fef0f0;
  border: 1px solid #f56c6c;
  color: #f56c6c;
}

.balance-amount-info .el-icon {
  font-size: 14px;
}

.member-info-detail {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 8px;
}

.member-info-row {
  display: flex;
  align-items: center;
  color: #67c23a;
}

.member-balance-label {
  font-size: 13px;
  color: #606266;
}

.member-balance-value {
  font-size: 15px;
  font-weight: 600;
  color: #409eff;
  margin-left: 4px;
}

/* 折扣相关样式 */
.discount-option {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.discount-name {
  flex: 1;
  font-weight: 500;
}

.discount-type {
  font-size: 12px;
  color: #909399;
}

.discount-rate {
  font-weight: 600;
  color: #f56c6c;
}

.selected-discount-info {
  margin-top: 8px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.discount-condition {
  font-size: 12px;
  color: #67c23a;
  padding: 2px 8px;
  background: #f0f9ff;
  border-radius: 4px;
}

.discount-desc {
  font-size: 12px;
  color: #909399;
}

.discount-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
