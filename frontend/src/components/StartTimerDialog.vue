<template>
  <el-dialog
    v-model="visible"
    title="开始计时"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" label-width="100px" v-loading="loadingRules">
      <!-- 桌台信息 -->
      <el-form-item label="桌台编号">
        <el-tag type="primary" size="large">{{ table?.name }}</el-tag>
      </el-form-item>

      <!-- 会员选择 -->
      <el-form-item label="会员">
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
            <el-tag type="success" size="small">{{ selectedMember.levelName }}</el-tag>
            <span style="margin-left: 8px;">{{ (selectedMember.discountRate * 10).toFixed(1) }}折</span>
          </div>
          <div class="member-info-row">
            <span class="member-balance-label">会员余额：</span>
            <span class="member-balance-value">¥{{ selectedMember.balance.toFixed(2) }}</span>
          </div>
        </div>
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

      <!-- 时长设置 -->
      <el-form-item label="套餐时长" required>
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

      <!-- 当前选择的时长预览 -->
      <el-form-item v-if="selectedRuleIndex !== 'unlimited'" label="套餐时长">
        <span class="duration-preview">{{ durationPreview }}</span>
      </el-form-item>

      <!-- 延长时间（仅非不限时时显示） -->
      <el-form-item v-if="selectedRuleIndex !== 'unlimited'" label="延长时间">
        <span class="extend-time-display">{{ formatExtendTime }}</span>
      </el-form-item>

      <!-- 总时长预览（仅非不限时时显示） -->
      <el-form-item v-if="selectedRuleIndex !== 'unlimited'" label="总时长">
        <span class="duration-preview total-preview">{{ totalDurationPreview }}</span>
      </el-form-item>

      <!-- 费用信息（仅非不限时时显示） -->
      <el-form-item v-if="selectedRuleIndex !== 'unlimited'" label="费用">
        <!-- 选择会员时：显示原价和折扣价 -->
        <div v-if="selectedMember" class="price-display-row">
          <div class="price-item-inline">
            <span class="price-label">原价：</span>
            <span class="price-original">¥{{ originalPrice.toFixed(2) }}</span>
          </div>
          <div class="price-item-inline">
            <span class="price-label">折扣价：</span>
            <span class="price-discount">¥{{ discountedPrice.toFixed(2) }}</span>
            <span v-if="memberDiscount > 0" class="discount-tag">
              ({{ (memberDiscount * 10).toFixed(1) }}折)
            </span>
          </div>
        </div>
        <!-- 未选择会员时：只显示价格 -->
        <span v-else class="price-normal">¥{{ originalPrice.toFixed(2) }}</span>
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
            余额不足，当前余额 ¥{{ selectedMember.balance.toFixed(2) }}，需要支付 ¥{{ finalAmountForDisplay.toFixed(2) }}，还差 ¥{{ (finalAmountForDisplay - selectedMember.balance).toFixed(2) }}
          </span>
        </div>
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
          确认开始
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
import { startTable } from '@/api/table'
import { NON_MEMBER_PAYMENT_METHODS, MEMBER_PAYMENT_METHODS, type PaymentMethod } from '@/api/table'
import { getBillingRuleConfig, getSystemConfig, type BillingRule, type BillingRuleItem, type SystemConfig } from '@/api/config'
import { getActiveDiscounts, calculateDiscountById } from '@/api/discount'
import type { MemberInfo } from '@/api/member'
import MemberSelectDialog from './MemberSelectDialog.vue'

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

// 会员相关
const isMember = ref(false)
const selectedMember = ref<MemberInfo | null>(null)
const memberDialogVisible = ref(false)

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
  const totalAmount = discountedPrice.value || originalPrice.value || 0
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
  const finalAmount = discountedPrice.value || originalPrice.value || 0
  return balance >= finalAmount
})

// 用于显示的最终金额
const finalAmountForDisplay = computed(() => {
  return discountedPrice.value || originalPrice.value || 0
})

// 标志：是否正在初始化（用于避免 handleChannelChange 干扰）
const isInitializing = ref(false)

// 活动折扣相关
const availableDiscounts = ref<any[]>([])
const selectedDiscount = ref<any>(null)

// 表单数据
const form = ref({
  presetDuration: 0, // 秒
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

// 延长时间（从系统配置读取，单位：分钟）
const extendTime = ref(0)

// 对话框显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 当前渠道的规则
const currentRules = computed(() => {
  if (!billingRules.value || !billingRules.value.channels || !Array.isArray(billingRules.value.channels)) return []
  const channel = billingRules.value.channels.find(c => c.channel === selectedChannel.value)
  return channel?.rules || []
})

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

// 时长预览
const durationPreview = computed(() => {
  if (selectedRuleIndex.value === 'unlimited') {
    return '不设时长'
  }

  let seconds = 0
  if (selectedRuleIndex.value === 'custom') {
    seconds = customHours.value * 3600 + customMinutes.value * 60
  } else if (typeof selectedRuleIndex.value === 'number') {
    const rule = currentRules.value[selectedRuleIndex.value]
    if (rule && !rule.unlimited && rule.minutes) {
      seconds = rule.minutes * 60
    } else {
      return '不限时'
    }
  }

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  if (hours > 0 && minutes > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${minutes}分钟`
  }
})

// 格式化延长时间显示
const formatExtendTime = computed(() => {
  const minutes = extendTime.value
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60

  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${mins}分钟`
  }
})

// 会员折扣率（用于计算，可以被活动折扣覆盖）
const memberDiscount = ref(1)

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

// 折扣价（可以被活动折扣计算结果覆盖）
const discountedPrice = ref(0)

// 总时长预览（套餐时长 + 延时时长）
const totalDurationPreview = computed(() => {
  let baseSeconds = 0
  if (selectedRuleIndex.value === 'custom') {
    baseSeconds = customHours.value * 3600 + customMinutes.value * 60
  } else if (typeof selectedRuleIndex.value === 'number') {
    const rule = currentRules.value[selectedRuleIndex.value]
    if (rule && !rule.unlimited && rule.minutes) {
      baseSeconds = rule.minutes * 60
    } else {
      return '不限时'
    }
  }

  // 始终加上延长时间
  const extendSeconds = extendTime.value * 60
  const totalSeconds = baseSeconds + extendSeconds

  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)

  if (hours > 0 && minutes > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${minutes}分钟`
  }
})

// 加载系统配置（获取延长时间）
const loadSystemConfig = async () => {
  try {
    const configStr = await getSystemConfig()

    if (!configStr) {
      extendTime.value = 30
      return
    }

    // 解析 JSON
    const config = JSON.parse(configStr) as SystemConfig

    if (config && config.extendTime !== undefined && config.extendTime !== null) {
      extendTime.value = config.extendTime
    } else {
      extendTime.value = 30
    }
  } catch (error) {
    ElMessage.warning('无法获取延长时间配置，使用默认值 30 分钟')
    // 设置默认延长时间为 30 分钟
    extendTime.value = 30
  }
}

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
      // 设置默认值
      setDefaultBillingRules()
      return
    }

    // 验证数据结构
    if (!parsed || typeof parsed !== 'object') {
      ElMessage.error('计费规则数据格式错误，请前往设置页面重新配置')
      setDefaultBillingRules()
      return
    }

    if (!parsed.channels || !Array.isArray(parsed.channels)) {
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

    // 默认选中第一条渠道数据
    if (parsed.channels && parsed.channels.length > 0) {
      selectedChannel.value = parsed.channels[0].channel
    }

    // 使用 nextTick 确保响应式更新后再访问 currentRules
    await nextTick()

    // 默认选中第一个规则并初始化表单
    if (currentRules.value.length > 0) {
      selectedRuleIndex.value = 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载计费规则失败，请前往设置页面重新配置')
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
  // 如果正在初始化，跳过处理（避免与 initializeForm 冲突）
  if (isInitializing.value) {
    return
  }

  // 切换渠道后，默认选中第一个规则
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
    // 对话框打开时，同时加载计费规则和系统配置
    await Promise.all([
      billingRules.value ? Promise.resolve() : loadBillingRules(),
      loadSystemConfig()
    ])
    // 重置到默认选择
    initializeForm()
  }
})

// 初始化表单（每次打开对话框时调用）
const initializeForm = () => {
  // 设置初始化标志，防止 handleChannelChange 干扰
  isInitializing.value = true

  // 先重置表单数据
  form.value = {
    presetDuration: 0,
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
  discountedPrice.value = 0
  memberDiscount.value = 1

  // 重置支付方式为默认值
  selectedPaymentMethod.value = 'offline'

  // 如果桌台已有会员信息，自动设置会员
  if (props.table?.memberId && props.table?.memberName) {
    isMember.value = true
    const discountRate = props.table.memberDiscountRate || 1
    selectedMember.value = {
      id: props.table.memberId,
      name: props.table.memberName,
      phone: '',
      address: '',
      totalAmount: 0,
      balance: props.table.memberBalance || 0,
      levelId: 0,
      levelName: '',
      discountRate: discountRate,
      createdAt: 0,
      updatedAt: 0
    }
    // 更新会员折扣率
    memberDiscount.value = discountRate
  }

  // 重置为第一条渠道数据
  if (billingRules.value && billingRules.value.channels && billingRules.value.channels.length > 0) {
    selectedChannel.value = billingRules.value.channels[0].channel
  }

  // 使用 nextTick 确保 billingRules 已更新
  nextTick(() => {
    if (currentRules.value.length > 0) {
      const firstRule = currentRules.value[0]

      // 直接设置 presetDuration，不依赖 watch
      if (firstRule && !firstRule.unlimited && firstRule.minutes) {
        form.value.presetDuration = firstRule.minutes * 60
      } else {
        form.value.presetDuration = 0
      }

      // 然后再设置其他状态（用于 UI 显示）
      customHours.value = 1
      customMinutes.value = 0
      selectedRuleIndex.value = 0
    } else {
      form.value.presetDuration = 0
      selectedRuleIndex.value = 'unlimited'
    }

    // 清除初始化标志
    nextTick(() => {
      isInitializing.value = false
    })
  })
}

// 监听规则选择变化，自动设置预设时长
watch(
  [selectedRuleIndex, customHours, customMinutes],
  () => {
    if (selectedRuleIndex.value === 'unlimited') {
      form.value.presetDuration = 0
    } else if (selectedRuleIndex.value === 'custom') {
      form.value.presetDuration = customHours.value * 3600 + customMinutes.value * 60
    } else if (typeof selectedRuleIndex.value === 'number') {
      const rule = currentRules.value[selectedRuleIndex.value]
      if (rule && !rule.unlimited && rule.minutes) {
        form.value.presetDuration = rule.minutes * 60
      } else {
        form.value.presetDuration = 0
      }
    }
  }
  // 移除 immediate: true，避免在计费规则加载前触发
)

// 关闭对话框
const handleClose = () => {
  visible.value = false
  // 不重置表单，保持用户的选择状态
}

// 重置表单（外部可调用）
const resetForm = () => {
  form.value = {
    presetDuration: 0,
    remark: '',
    applyActivityDiscount: false,
    discountId: ''
  }
  // 重置为第一条渠道数据
  if (billingRules.value && billingRules.value.channels && billingRules.value.channels.length > 0) {
    selectedChannel.value = billingRules.value.channels[0].channel
  }
  // 使用 nextTick 确保响应式更新
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

// 确认开始计时
const handleConfirm = async () => {
  if (!props.table) {
    ElMessage.error('桌台信息不存在')
    return
  }

  // 直接根据选择的规则计算套餐时长（不包含延长时间）
  let packageDuration = 0

  if (selectedRuleIndex.value === 'unlimited') {
    packageDuration = 0
  } else if (selectedRuleIndex.value === 'custom') {
    packageDuration = customHours.value * 3600 + customMinutes.value * 60
    // 验证自定义时长
    if (packageDuration <= 0) {
      ElMessage.error('请设置有效的时长')
      return
    }
  } else if (typeof selectedRuleIndex.value === 'number') {
    const rule = currentRules.value[selectedRuleIndex.value]
    if (rule && !rule.unlimited && rule.minutes) {
      packageDuration = rule.minutes * 60
    } else {
      packageDuration = 0
    }
  }

  // 验证会员余额是否足够（仅对会员余额支付方式）
  if (selectedMember.value && selectedPaymentMethod.value === 'balance') {
    const memberBalance = selectedMember.value.balance || 0
    const finalAmount = discountedPrice.value || 0

    if (memberBalance < finalAmount) {
      ElMessageBox.alert(
        `会员余额不足！<br/>当前余额：¥${memberBalance.toFixed(2)}<br/>需要支付：¥${finalAmount.toFixed(2)}<br/>还差：¥${(finalAmount - memberBalance).toFixed(2)}`,
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
    await startTable(props.table.id, {
      presetDuration: packageDuration,  // 只传套餐时长，不包含延长时间
      channel: selectedChannel.value,
      memberId: selectedMember.value?.id,
      remark: form.value.remark,
      paymentMethod: selectedPaymentMethod.value,
      discountId: form.value.applyActivityDiscount ? form.value.discountId : undefined
    })

    ElMessage.success('开始计时成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '开始计时失败')
  } finally {
    loading.value = false
  }
}

// 会员切换处理
const handleMemberToggle = (value: string | number | boolean) => {
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
  memberDialogVisible.value = true
}

// 会员选择回调
const handleMemberSelected = (member: MemberInfo) => {
  selectedMember.value = member
  // 更新会员折扣率
  memberDiscount.value = member.discountRate || 1
  // 更新折扣价
  if (originalPrice.value > 0 && memberDiscount.value > 0) {
    discountedPrice.value = originalPrice.value * memberDiscount.value
  }
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
    loadDiscounts()
  } else {
    form.value.discountId = ''
    selectedDiscount.value = null
    availableDiscounts.value = []
  }
}

// 折扣选择变化处理
const handleDiscountChange = async (discountId: string) => {
  const discount = availableDiscounts.value.find(d => d.id === discountId)
  selectedDiscount.value = discount

  // 如果选择了折扣且有原价，调用API计算折扣
  if (discount && originalPrice.value > 0) {
    try {
      const response = await calculateDiscountById(discountId, originalPrice.value, selectedMember.value?.id)

      if (response) {
        // 更新折扣价格显示
        discountedPrice.value = response.finalAmount
        memberDiscount.value = response.discountRate
      }
    } catch (error: any) {
      // 恢复原价
      discountedPrice.value = originalPrice.value
      memberDiscount.value = 1
    }
  } else {
    // 未选择折扣，恢复原价
    discountedPrice.value = originalPrice.value
    memberDiscount.value = 1
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
const formatDiscountLabel = (discount: any) => {
  return `${discount.name} - ${(discount.discountRate * 10).toFixed(1)}折`
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
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.duration-preview {
  font-size: 16px;
  font-weight: 500;
  color: #409EFF;
  padding: 8px 16px;
  background: #ecf5ff;
  border-radius: 4px;
}

.extend-time-display {
  font-size: 15px;
  font-weight: 500;
  color: #E6A23C;
  padding: 8px 16px;
  background: #fdf6ec;
  border-radius: 4px;
  border: 1px dashed #E6A23C;
}

.total-preview {
  font-size: 18px;
  font-weight: 600;
  color: #67C23A;
  background: #f0f9ff;
  border: 2px solid #67C23A;
}

.price-display {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.price-display-row {
  display: flex;
  align-items: center;
  gap: 24px;
  width: 100%;
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

.price-normal {
  font-size: 18px;
  font-weight: 600;
  color: #409EFF;
}

.price-original {
  font-size: 16px;
  font-weight: 500;
  color: #909399;
  text-decoration: line-through;
}

.price-discount {
  font-size: 18px;
  font-weight: 600;
  color: #67C23A;
}

.discount-tag {
  font-size: 12px;
  color: #67C23A;
  background: #f0f9ff;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: 4px;
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
