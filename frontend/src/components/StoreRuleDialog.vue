<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑规则' : '新增规则'"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="规则分类" prop="category">
        <el-select v-model="formData.category" placeholder="请选择规则分类" :disabled="isEdit">
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="规则标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入规则标题" />
      </el-form-item>

      <el-form-item label="规则类型" prop="ruleType">
        <el-select v-model="formData.ruleType" placeholder="请选择规则类型">
          <el-option
            v-for="item in ruleTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="排序号" prop="sortOrder">
        <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
      </el-form-item>

      <el-form-item label="规则内容" prop="content">
        <el-tabs v-model="contentTab" class="content-tabs">
          <el-tab-pane label="富文本编辑" name="html">
            <el-input
              v-model="formData.content"
              type="textarea"
              :rows="12"
              placeholder="请输入HTML内容，例如：<ul><li>规则项1</li><li>规则项2</li></ul>"
            />
            <div class="content-tips">
              <p>支持的HTML标签：ul, ol, li, p, strong, br 等</p>
            </div>
          </el-tab-pane>

          <el-tab-pane label="表格数据(JSON)" name="json" v-if="formData.ruleType === 'table'">
            <el-input
              v-model="jsonContent"
              type="textarea"
              :rows="12"
              placeholder='请输入JSON数组，例如：[{"name":"套餐名","price":"¥99","content":"包含内容"}]'
            />
            <div class="content-tips">
              <p v-if="formData.category === 'packages'">套餐表格字段：name, price, content, suitable, duration</p>
              <p v-else-if="formData.category === 'services'">服务表格字段：name, price, description</p>
              <el-button type="primary" link @click="formatJson">格式化JSON</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-form-item>

      <el-form-item label="启用状态" prop="isEnabled">
        <el-switch v-model="formData.isEnabled" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, FormInstance } from 'element-plus'
import { createRule, updateRule, type StoreRuleRequest, type StoreRule } from '@/api/storeRules'

interface Props {
  modelValue: boolean
  rule?: StoreRule | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const saving = ref(false)
const contentTab = ref('html')
const jsonContent = ref('')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.rule?.id)

const formData = reactive<StoreRuleRequest>({
  id: '',
  category: 'packages',
  title: '',
  content: '',
  ruleType: 'list',
  sortOrder: 1,
  isEnabled: true
})

const categoryOptions = [
  { value: 'packages', label: '套餐规则' },
  { value: 'services', label: '增值服务' },
  { value: 'safety', label: '安全须知' },
  { value: 'other', label: '其他规定' }
]

const ruleTypeOptions = [
  { value: 'table', label: '表格数据' },
  { value: 'list', label: '列表' },
  { value: 'warning', label: '警告框' },
  { value: 'special', label: '特色服务' }
]

const rules = {
  category: [{ required: true, message: '请选择规则分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入规则标题', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入规则内容', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序号', trigger: 'blur' }]
}

// 格式化JSON
const formatJson = () => {
  try {
    const parsed = JSON.parse(jsonContent.value)
    jsonContent.value = JSON.stringify(parsed, null, 2)
    ElMessage.success('JSON格式化成功')
  } catch {
    ElMessage.error('JSON格式错误，请检查')
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: '',
    category: 'packages',
    title: '',
    content: '',
    ruleType: 'list',
    sortOrder: 1,
    isEnabled: true
  })
  jsonContent.value = ''
  contentTab.value = 'html'
  formRef.value?.clearValidate()
}

// 监听规则类型变化，自动切换tab
watch(() => formData.ruleType, (newType) => {
  if (newType === 'table') {
    contentTab.value = 'json'
  } else {
    contentTab.value = 'html'
  }
})

// 监听jsonContent变化，同步到formData.content
watch(jsonContent, (newVal) => {
  formData.content = newVal
})

// 监听传入的规则数据
watch(() => props.rule, (rule) => {
  if (rule) {
    Object.assign(formData, {
      id: rule.id,
      category: rule.category,
      title: rule.title || '',
      content: rule.content,
      ruleType: rule.ruleType || 'list',
      sortOrder: rule.sortOrder,
      isEnabled: rule.isEnabled
    })
    // 如果是表格类型，设置jsonContent
    if (rule.ruleType === 'table') {
      jsonContent.value = rule.content
      contentTab.value = 'json'
    } else {
      contentTab.value = 'html'
    }
  } else {
    resetForm()
  }
}, { immediate: true })

// 关闭对话框
const handleClose = () => {
  visible.value = false
  resetForm()
}

// 保存
const handleSave = async () => {
  try {
    await formRef.value?.validate()

    // 如果是表格类型且当前在json tab，使用jsonContent
    if (formData.ruleType === 'table' && contentTab.value === 'json') {
      // 验证JSON格式
      try {
        JSON.parse(jsonContent.value)
        formData.content = jsonContent.value
      } catch {
        ElMessage.error('JSON格式错误，请检查')
        return
      }
    }

    saving.value = true

    if (isEdit.value) {
      await updateRule(formData)
      ElMessage.success('更新成功')
    } else {
      await createRule(formData)
      ElMessage.success('创建成功')
    }

    emit('success')
    handleClose()
  } catch {
    // 表单验证失败
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.content-tabs {
  width: 100%;

  :deep(.el-tabs__content) {
    padding-top: 10px;
  }
}

.content-tips {
  margin-top: 10px;
  padding: 10px;
  background: rgba(212, 165, 255, 0.08);
  border-radius: 8px;
  font-size: 12px;
  color: #999;

  p {
    margin: 4px 0;
  }
}
</style>
