<template>
  <el-dialog
    v-model="visible"
    title="开始计时"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" label-width="100px">
      <!-- 桌台信息 -->
      <el-form-item label="桌台编号">
        <el-tag type="primary" size="large">{{ table?.name }}</el-tag>
      </el-form-item>

      <!-- 时长设置 -->
      <el-form-item label="预设时长" required>
        <el-radio-group v-model="durationType" @change="handleDurationTypeChange">
          <el-radio value="1h">1小时</el-radio>
          <el-radio value="2h">2小时</el-radio>
          <el-radio value="3h">3小时</el-radio>
          <el-radio value="custom">自定义</el-radio>
          <el-radio value="unlimited">不设时长</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 自定义时长输入 -->
      <el-form-item v-if="durationType === 'custom'" label="自定义时长" required>
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
      <el-form-item v-if="durationType !== 'unlimited'" label="时长预览">
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
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { TableInfo } from '@/api/table'
import { startTable } from '@/api/table'

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

// 表单数据
const form = ref({
  presetDuration: 0, // 秒
  remark: ''
})

// 时长类型
const durationType = ref<'1h' | '2h' | '3h' | 'custom' | 'unlimited'>('2h')

// 自定义时长
const customHours = ref(2)
const customMinutes = ref(0)

// 对话框显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 时长预览
const durationPreview = computed(() => {
  if (durationType.value === 'unlimited') {
    return ''
  }

  let seconds = 0
  if (durationType.value === 'custom') {
    seconds = customHours.value * 3600 + customMinutes.value * 60
  } else {
    const typeMap = {
      '1h': 3600,
      '2h': 7200,
      '3h': 10800
    }
    seconds = typeMap[durationType.value]
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

// 监听时长类型变化，自动设置预设时长
watch(
  [durationType, customHours, customMinutes],
  () => {
    if (durationType.value === 'unlimited') {
      form.value.presetDuration = 0
    } else if (durationType.value === 'custom') {
      form.value.presetDuration = customHours.value * 3600 + customMinutes.value * 60
    } else {
      const typeMap = {
        '1h': 3600,
        '2h': 7200,
        '3h': 10800
      }
      form.value.presetDuration = typeMap[durationType.value]
    }
  },
  { immediate: true }
)

// 时长类型变化处理
const handleDurationTypeChange = () => {
  // 可以在这里添加额外的处理逻辑
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.value = {
    presetDuration: 7200, // 默认2小时
    remark: ''
  }
  durationType.value = '2h'
  customHours.value = 2
  customMinutes.value = 0
  formRef.value?.clearValidate()
}

// 确认开始计时
const handleConfirm = async () => {
  if (!props.table) {
    ElMessage.error('桌台信息不存在')
    return
  }

  // 验证自定义时长
  if (durationType.value === 'custom') {
    const totalSeconds = customHours.value * 3600 + customMinutes.value * 60
    if (totalSeconds <= 0) {
      ElMessage.error('请设置有效的时长')
      return
    }
  }

  loading.value = true

  try {
    await startTable(props.table.id, {
      presetDuration: form.value.presetDuration,
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

:deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

:deep(.el-radio) {
  margin-right: 0;
}

:deep(.el-input-number) {
  width: 100%;
}
</style>
