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
        <div v-if="selectedMember" style="margin-top: 8px; color: #67c23a;">
          <el-tag type="success" size="small">{{ selectedMember.levelName }}</el-tag>
          <span style="margin-left: 8px;">{{ (selectedMember.discountRate * 10).toFixed(1) }}折</span>
        </div>
      </el-form-item>

      <!-- 渠道选择 -->
      <el-form-item label="订餐渠道">
        <el-radio-group v-model="selectedChannel" @change="handleChannelChange">
          <el-radio
            v-for="channel in billingRules?.channels || []"
            :key="channel.channel"
            :value="channel.channel"
          >
            {{ channel.channelName }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 时长设置 -->
      <el-form-item label="预设时长" required>
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
      <el-form-item v-if="selectedRuleIndex !== 'unlimited'" label="时长预览">
        <span class="duration-preview">{{ durationPreview }}</span>
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
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { TableInfo } from '@/api/table'
import { startTable } from '@/api/table'
import { getBillingRuleConfig, type BillingRule, type BillingRuleItem } from '@/api/config'
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

// 标志：是否正在初始化（用于避免 handleChannelChange 干扰）
const isInitializing = ref(false)

// 表单数据
const form = ref({
  presetDuration: 0, // 秒
  remark: ''
})

// 计费规则数据
const billingRules = ref<BillingRule | null>(null)
const selectedChannel = ref<string>('store')

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

// 加载计费规则
const loadBillingRules = async () => {
  try {
    loadingRules.value = true
    const configStr = await getBillingRuleConfig()
    console.log('计费规则配置字符串:', configStr)

    let parsed: any
    try {
      parsed = JSON.parse(configStr)
    } catch (parseError) {
      console.error('JSON解析失败:', parseError)
      ElMessage.error('计费规则数据格式错误，请前往设置页面重新配置')
      // 设置默认值
      setDefaultBillingRules()
      return
    }

    console.log('解析后的计费规则:', parsed)

    // 验证数据结构
    if (!parsed || typeof parsed !== 'object') {
      console.error('解析后的数据不是对象:', parsed)
      ElMessage.error('计费规则数据格式错误，请前往设置页面重新配置')
      setDefaultBillingRules()
      return
    }

    if (!parsed.channels || !Array.isArray(parsed.channels)) {
      console.error('channels字段缺失或不是数组:', parsed.channels)
      ElMessage.error('计费规则数据格式错误，请前往设置页面重新配置')
      setDefaultBillingRules()
      return
    }

    // 验证每个渠道的数据结构
    for (const channel of parsed.channels) {
      if (!channel.channel || !channel.channelName || !Array.isArray(channel.rules)) {
        console.error('渠道数据结构不正确:', channel)
        ElMessage.error('计费规则数据格式错误，请前往设置页面重新配置')
        setDefaultBillingRules()
        return
      }
    }

    billingRules.value = parsed

    // 设置当前选中的渠道（如果当前渠道不存在，则选择第一个渠道）
    const currentChannelExists = parsed.channels.some((c: any) => c.channel === selectedChannel.value)
    if (!currentChannelExists && parsed.channels.length > 0) {
      selectedChannel.value = parsed.channels[0].channel
    }

    // 使用 nextTick 确保响应式更新后再访问 currentRules
    await nextTick()

    // 默认选中第一个规则并初始化表单
    if (currentRules.value.length > 0) {
      selectedRuleIndex.value = 0
      console.log('默认选中规则:', currentRules.value[0])
    } else {
      console.warn('当前渠道没有可用的计费规则')
    }
  } catch (error: any) {
    console.error('加载计费规则失败', error)
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
    console.log('=== handleChannelChange skipped (initializing) ===')
    return
  }

  console.log('=== Channel changed to', selectedChannel.value)
  // 切换渠道后，默认选中第一个规则
  if (currentRules.value.length > 0) {
    customHours.value = 1
    customMinutes.value = 0
    selectedRuleIndex.value = 0
    console.log('Set selectedRuleIndex to 0, first rule:', currentRules.value[0])
  } else {
    selectedRuleIndex.value = 'unlimited'
    console.log('No rules found, set to unlimited')
  }
  console.log('=====================')
}

// 规则切换处理
const handleRuleChange = () => {
  // 规则切换时的处理逻辑
}

// 监听对话框打开
watch(() => props.modelValue, async (newVal) => {
  console.log('=== Dialog modelValue changed:', newVal)
  if (newVal) {
    // 对话框打开时，加载计费规则
    if (!billingRules.value) {
      console.log('Billing rules not loaded, loading...')
      await loadBillingRules()
    } else {
      console.log('Billing rules already loaded')
    }
    // 重置到默认选择
    initializeForm()
  }
  console.log('===========================')
})

// 初始化表单（每次打开对话框时调用）
const initializeForm = () => {
  console.log('=== initializeForm called ===')
  // 设置初始化标志，防止 handleChannelChange 干扰
  isInitializing.value = true

  // 先重置表单数据
  form.value = {
    presetDuration: 0,
    remark: ''
  }

  // 重置会员选择
  isMember.value = false
  selectedMember.value = null

  // 重置为默认渠道
  selectedChannel.value = 'store'

  // 使用 nextTick 确保 billingRules 已更新
  nextTick(() => {
    console.log('initializeForm nextTick: currentRules:', currentRules.value)
    if (currentRules.value.length > 0) {
      const firstRule = currentRules.value[0]
      console.log('initializeForm: first rule:', firstRule)

      // 直接设置 presetDuration，不依赖 watch
      if (firstRule && !firstRule.unlimited && firstRule.minutes) {
        form.value.presetDuration = firstRule.minutes * 60
        console.log('initializeForm: Directly set presetDuration to', form.value.presetDuration)
      } else {
        form.value.presetDuration = 0
        console.log('initializeForm: Set presetDuration to 0 (unlimited or invalid)')
      }

      // 然后再设置其他状态（用于 UI 显示）
      customHours.value = 1
      customMinutes.value = 0
      selectedRuleIndex.value = 0

      console.log('initializeForm: Set selectedRuleIndex to 0')
    } else {
      form.value.presetDuration = 0
      selectedRuleIndex.value = 'unlimited'
      console.log('initializeForm: No rules found, set to unlimited')
    }

    // 清除初始化标志
    nextTick(() => {
      isInitializing.value = false
      console.log('initializeForm: Initialization complete, presetDuration =', form.value.presetDuration)
    })
  })
  console.log('=========================')
}

// 监听规则选择变化，自动设置预设时长
watch(
  [selectedRuleIndex, customHours, customMinutes],
  () => {
    console.log('=== Watch triggered ===')
    console.log('selectedRuleIndex:', selectedRuleIndex.value)
    console.log('currentRules:', currentRules.value)
    console.log('customHours:', customHours.value, 'customMinutes:', customMinutes.value)

    if (selectedRuleIndex.value === 'unlimited') {
      form.value.presetDuration = 0
      console.log('Set presetDuration to 0 (unlimited)')
    } else if (selectedRuleIndex.value === 'custom') {
      form.value.presetDuration = customHours.value * 3600 + customMinutes.value * 60
      console.log('Set presetDuration to', form.value.presetDuration, '(custom)')
    } else if (typeof selectedRuleIndex.value === 'number') {
      const rule = currentRules.value[selectedRuleIndex.value]
      console.log('Rule at index', selectedRuleIndex.value, ':', rule)
      if (rule && !rule.unlimited && rule.minutes) {
        form.value.presetDuration = rule.minutes * 60
        console.log('Set presetDuration to', form.value.presetDuration, '(rule:', rule.minutes, 'minutes)')
      } else {
        form.value.presetDuration = 0
        console.log('Set presetDuration to 0 (invalid rule)')
      }
    }
    console.log('Final presetDuration:', form.value.presetDuration)
    console.log('=====================')
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
    remark: ''
  }
  selectedChannel.value = 'store'
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

  console.log('=== handleConfirm ===')
  console.log('selectedRuleIndex:', selectedRuleIndex.value)
  console.log('selectedChannel:', selectedChannel.value)
  console.log('customHours:', customHours.value, 'customMinutes:', customMinutes.value)
  console.log('currentRules:', currentRules.value)
  console.log('selectedMember:', selectedMember.value)
  if (typeof selectedRuleIndex.value === 'number') {
    console.log('Selected rule:', currentRules.value[selectedRuleIndex.value])
  }
  console.log('====================')

  // 直接根据选择的规则计算时长，避免依赖 form.presetDuration 可能的时序问题
  let actualDuration = 0

  if (selectedRuleIndex.value === 'unlimited') {
    actualDuration = 0
  } else if (selectedRuleIndex.value === 'custom') {
    actualDuration = customHours.value * 3600 + customMinutes.value * 60
    // 验证自定义时长
    if (actualDuration <= 0) {
      ElMessage.error('请设置有效的时长')
      return
    }
  } else if (typeof selectedRuleIndex.value === 'number') {
    const rule = currentRules.value[selectedRuleIndex.value]
    if (rule && !rule.unlimited && rule.minutes) {
      actualDuration = rule.minutes * 60
    } else {
      actualDuration = 0
    }
  }

  console.log('Actual duration to submit:', actualDuration, 'seconds')

  loading.value = true

  try {
    await startTable(props.table.id, {
      presetDuration: actualDuration,
      channel: selectedChannel.value,
      memberId: selectedMember.value?.id,
      remark: form.value.remark
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
const handleMemberToggle = (value: boolean) => {
  if (!value) {
    selectedMember.value = null
  }
}

// 显示会员选择对话框
const showMemberDialog = () => {
  memberDialogVisible.value = true
}

// 会员选择回调
const handleMemberSelected = (member: MemberInfo) => {
  selectedMember.value = member
  ElMessage.success(`已选择会员：${member.name}`)
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
</style>
