<template>
  <el-dialog
    v-model="visible"
    title="编辑桌台"
    width="450px"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
      <el-form-item label="桌台编号" prop="id">
        <el-input v-model="form.id" disabled />
      </el-form-item>
      <el-form-item label="桌台名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入桌台名称"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="所属分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
          <el-option label="未分类" :value="null" />
          <el-option
            v-for="category in categories.filter(c => c.id !== 0)"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>
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
import { updateTable } from '@/api/table'
import type { TableInfo } from '@/api/table'
import type { TableCategoryResponse } from '@/api/tableCategory'

interface Props {
  modelValue: boolean
  table: TableInfo | null
  categories: TableCategoryResponse[]
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
  id: 0,
  name: '',
  categoryId: null as number | null
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入桌台名称', trigger: 'blur' },
    { min: 2, max: 20, message: '桌台名称长度为2-20个字符', trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.table) {
    form.value = {
      id: props.table.id,
      name: props.table.name,
      categoryId: props.table.categoryId
    }
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

    await updateTable(form.value.id, {
      name: form.value.name,
      categoryId: form.value.categoryId
    })

    ElMessage.success('更新成功')
    emit('success')
    handleClose()
  } catch (error) {
    // 验证失败或请求失败
  } finally {
    submitting.value = false
  }
}
</script>

<script lang="ts">
export default {
  name: 'EditTableDialog'
}
</script>
