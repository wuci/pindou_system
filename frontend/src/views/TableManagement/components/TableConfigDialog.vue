<template>
  <el-dialog
    v-model="visible"
    title="桌台配置"
    width="500px"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="所属分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%" @change="handleCategoryChange">
          <el-option
            v-for="category in categories.filter(c => c.id !== 0)"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="操作类型">
        <el-radio-group v-model="form.operationType">
          <el-radio label="add">增加桌台</el-radio>
          <el-radio label="remove">减少桌台</el-radio>
          <el-radio label="set">设置总数量</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="数量" prop="count">
        <el-input-number
          v-model="form.count"
          :min="1"
          :max="form.operationType === 'set' ? 50 : 50"
          :placeholder="form.operationType === 'set' ? '设置桌台总数量' : '输入要增减的数量'"
        />
        <span class="tip-text">
          当前该分类有 <span class="highlight">{{ currentTableCount }}</span> 个桌台
          <template v-if="form.count">
            ，操作后将有 <span class="highlight">{{ finalCount }}</span> 个桌台
          </template>
        </span>
      </el-form-item>

      <el-alert
        v-if="form.count && finalCount < currentTableCount"
        type="warning"
        :closable="false"
        style="margin-top: 10px"
      >
        将删除 {{ currentTableCount - finalCount }} 个桌台（只能删除空闲状态的桌台）
      </el-alert>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { configTableCount } from '@/api/table'
import type { TableCategoryResponse } from '@/api/tableCategory'
import type { TableInfo } from '@/api/table'

interface Props {
  modelValue: boolean
  categories: TableCategoryResponse[]
  tables: TableInfo[]
  categoryId?: number
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = ref({
  categoryId: null as number | null,
  operationType: 'add' as 'add' | 'remove' | 'set',
  count: null as number | null
})

// 计算最终数量
const finalCount = computed(() => {
  if (form.value.count === null) return currentTableCount.value

  switch (form.value.operationType) {
    case 'add':
      return currentTableCount.value + form.value.count
    case 'remove':
      return Math.max(0, currentTableCount.value - form.value.count)
    case 'set':
      return form.value.count
    default:
      return currentTableCount.value
  }
})

// 分类变更时重置数量
const handleCategoryChange = () => {
  form.value.count = null
}

const currentTableCount = computed(() => {
  if (!form.value.categoryId) return 0
  return props.tables.filter(t => t.categoryId === form.value.categoryId).length
})

const rules: FormRules = {
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  count: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type: 'number', min: 1, max: 50, message: '数量范围为1-50', trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.categoryId && props.categoryId !== 0) {
    form.value.categoryId = props.categoryId
    form.value.operationType = 'add'
    form.value.count = null
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // 计算最终要设置的总数量
    const targetCount = finalCount.value

    if (targetCount < 1) {
      ElMessage.warning('桌台数量不能少于1个')
      return
    }

    if (targetCount > 50) {
      ElMessage.warning('桌台数量不能超过50个')
      return
    }

    await configTableCount(targetCount, form.value.categoryId!)
    ElMessage.success('配置成功')
    emit('success')
    handleClose()
  } catch (error) {
    // 验证失败或请求失败
  } finally {
    submitting.value = false
  }
}

defineExpose({
  name: 'TableConfigDialog'
})
</script>

<style scoped>
.tip-text {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.tip-text .highlight {
  color: #409eff;
  font-weight: bold;
  font-size: 14px;
}
</style>
