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
        <div class="current-status">
          <div v-if="table?.presetDuration">
            <span>预设时长：</span>
            <span class="status-value">{{ formatDuration(table.presetDuration) }}</span>
          </div>
          <div v-if="remainingTime > 0">
            <span>剩余时长：</span>
            <span class="status-value remaining">{{ formatDuration(remainingTime) }}</span>
          </div>
          <div v-if="table?.duration">
            <span>已用时长：</span>
            <span class="status-value">{{ formatDuration(table.duration) }}</span>
          </div>
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
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { TableInfo } from '@/api/table'
import { extendTable } from '@/api/table'
import { getBillingRuleConfig, type BillingRule, type BillingRuleItem } from '@/api/config'

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

// 表单数据
const form = ref({
  additionalDuration: 0, // 秒
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
    return `总预设时长: ${hours}小时${minutes}分钟`
  } else if (hours > 0) {
    return `总预设时长: ${hours}小时`
  } else {
    return `总预设时长: ${minutes}分钟`
  }
})

// 当前渠道的规则
const currentRules = computed(() => {
  if (!billingRules.value || !billingRules.value.channels || !Array.isArray(billingRules.value.channels)) return []
  const channel = billingRules.value.channels.find(c => c.channel === selectedChannel.value)
  return channel?.rules || []
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
      setDefaultBillingRules()
      return
    }

    console.log('解析后的计费规则:', parsed)

    if (!parsed || typeof parsed !== 'object' || !parsed.channels || !Array.isArray(parsed.channels)) {
      console.error('计费规则数据格式错误')
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

    // 设置当前选中的渠道
    const currentChannelExists = parsed.channels.some((c: any) => c.channel === selectedChannel.value)
    if (!currentChannelExists && parsed.channels.length > 0) {
      selectedChannel.value = parsed.channels[0].channel
    }

    await nextTick()

    // 默认选中第一个规则
    if (currentRules.value.length > 0) {
      selectedRuleIndex.value = 0
    } else {
      selectedRuleIndex.value = 'unlimited'
    }
  } catch (error: any) {
    console.error('加载计费规则失败', error)
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
    remark: ''
  }

  selectedChannel.value = 'store'

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
    remark: ''
  }
  selectedChannel.value = 'store'
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

  console.log('=== handleConfirm ===')
  console.log('selectedRuleIndex:', selectedRuleIndex.value)
  console.log('selectedChannel:', selectedChannel.value)

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

  console.log('Additional duration:', additionalDuration, 'seconds')

  loading.value = true

  try {
    await extendTable(props.table.id, {
      additionalDuration,
      channel: selectedChannel.value
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
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
</style>
