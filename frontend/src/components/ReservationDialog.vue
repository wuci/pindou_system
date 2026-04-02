<template>
  <el-dialog
    v-model="visible"
    title="预定桌台"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <!-- 桌台信息 -->
      <el-form-item label="桌台编号">
        <el-tag type="primary" size="large">{{ table?.name }}</el-tag>
      </el-form-item>

      <!-- 预定截止时间 -->
      <el-form-item label="预定截止时间" prop="reservationEndTime">
        <el-date-picker
          v-model="form.reservationEndTime"
          type="datetime"
          placeholder="选择预定截止时间"
          format="YYYY-MM-DD HH:mm"
          value-format="x"
          :disabled-date="disabledDate"
          :disabled-hours="disabledHours"
          :disabled-minutes="disabledMinutes"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 预订人姓名 -->
      <el-form-item label="预订人姓名" prop="reservationName">
        <el-input
          v-model="form.reservationName"
          placeholder="请输入预订人姓名"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 预订人手机号 -->
      <el-form-item label="预订人手机号" prop="reservationPhone">
        <el-input
          v-model="form.reservationPhone"
          placeholder="请输入预订人手机号"
          maxlength="11"
        />
      </el-form-item>

      <!-- 快捷时间选择 -->
      <el-form-item label="快捷选择">
        <div class="quick-time-buttons">
          <el-button size="small" @click="setQuickTime(30)">30分钟后</el-button>
          <el-button size="small" @click="setQuickTime(60)">1小时后</el-button>
          <el-button size="small" @click="setQuickTime(120)">2小时后</el-button>
          <el-button size="small" @click="setQuickTime(180)">3小时后</el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleConfirm">
          确认预定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { TableInfo } from '@/api/table'
import { createReservation } from '@/api/table'

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
  reservationEndTime: undefined as number | undefined,
  reservationName: '',
  reservationPhone: ''
})

// 表单验证规则
const rules = ref<FormRules>({
  reservationEndTime: [
    { required: true, message: '请选择预定截止时间', trigger: 'change' }
  ],
  reservationName: [
    { required: true, message: '请输入预订人姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度为2-50个字符', trigger: 'blur' }
  ],
  reservationPhone: [
    { required: true, message: '请输入预订人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
})

// 对话框显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

// 禁用过去的小时
const disabledHours = () => {
  const now = new Date()
  const hours = []
  for (let i = 0; i < now.getHours(); i++) {
    hours.push(i)
  }
  return hours
}

// 禁用过去的分钟
const disabledMinutes = () => {
  const now = new Date()
  const minutes = []
  if (form.value.reservationEndTime) {
    const selectedDate = new Date(form.value.reservationEndTime)
    if (selectedDate.getDate() === now.getDate() &&
        selectedDate.getMonth() === now.getMonth() &&
        selectedDate.getFullYear() === now.getFullYear()) {
      // 如果是今天，禁用当前小时之前的分钟
      for (let i = 0; i < now.getMinutes(); i++) {
        minutes.push(i)
      }
    }
  }
  return minutes
}

// 快捷设置时间
const setQuickTime = (minutes: number) => {
  const now = Date.now()
  form.value.reservationEndTime = now + minutes * 60 * 1000
}

// 监听对话框打开，重置表单
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

// 重置表单
const resetForm = () => {
  form.value = {
    reservationEndTime: undefined,
    reservationName: '',
    reservationPhone: ''
  }
  formRef.value?.clearValidate()
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 确认预定
const handleConfirm = async () => {
  if (!props.table) {
    ElMessage.error('桌台信息不存在')
    return
  }

  const valid = await formRef.value?.validate()
  if (!valid) return

  // 验证预定时间必须晚于当前时间
  const now = Date.now()
  if (form.value.reservationEndTime && form.value.reservationEndTime <= now) {
    ElMessage.error('预定截止时间必须晚于当前时间')
    return
  }

  loading.value = true

  try {
    await createReservation(props.table.id, {
      reservationEndTime: form.value.reservationEndTime!,
      reservationName: form.value.reservationName,
      reservationPhone: form.value.reservationPhone
    })

    ElMessage.success('预定成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '预定失败')
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

.quick-time-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
