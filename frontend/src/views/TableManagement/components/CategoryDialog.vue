<template>
  <el-dialog
    v-model="visible"
    title="分类管理"
    width="600px"
    @close="handleClose"
  >
    <div class="category-dialog">
      <!-- 分类列表 -->
      <div class="category-list">
        <div
          v-for="category in sortedCategories"
          :key="category.id"
          class="category-item"
          :class="{ 'is-default': category.id === 0 }"
        >
          <div class="category-info">
            <el-icon v-if="category.icon" class="category-icon">
              <component :is="category.icon" />
            </el-icon>
            <span class="category-name">{{ category.name }}</span>
            <el-tag size="small" type="info">{{ category.tableCount }}个桌台</el-tag>
          </div>
          <div class="category-actions">
            <el-button
              v-if="category.id !== 0"
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(category)"
            >
              编辑
            </el-button>
            <el-button
              v-if="category.id !== 0"
              type="danger"
              link
              :icon="Delete"
              @click="handleDelete(category)"
            >
              删除
            </el-button>
          </div>
        </div>

        <el-empty v-if="sortedCategories.length === 0" description="暂无分类" />
      </div>

      <!-- 新增按钮 -->
      <div class="add-btn-wrapper">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增分类
        </el-button>
      </div>
    </div>

    <!-- 编辑表单对话框 -->
    <el-dialog
      v-model="showFormDialog"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="400px"
      append-to-body
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-select v-model="form.icon" placeholder="请选择图标">
            <el-option label="网格" value="Grid" />
            <el-option label="列表" value="List" />
            <el-option label="星标" value="Star" />
            <el-option label="大楼" value="OfficeBuilding" />
            <el-option label="房子" value="House" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFormDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory,
  type TableCategoryResponse
} from '@/api/tableCategory'

interface Props {
  modelValue: boolean
  categories: TableCategoryResponse[]
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'refresh'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const showFormDialog = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = ref({
  id: undefined as number | undefined,
  name: '',
  icon: 'Grid',
  sortOrder: 0,
  remark: ''
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

// 排序后的分类列表（默认分类排在第一位）
const sortedCategories = computed(() => {
  return [...props.categories].sort((a, b) => {
    if (a.id === 0) return -1
    if (b.id === 0) return 1
    return (a.sortOrder || 0) - (b.sortOrder || 0)
  })
})

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  form.value = {
    id: undefined,
    name: '',
    icon: 'Grid',
    sortOrder: sortedCategories.value.length,
    remark: ''
  }
  showFormDialog.value = true
}

// 编辑
const handleEdit = (category: TableCategoryResponse) => {
  isEdit.value = true
  form.value = {
    id: category.id,
    name: category.name,
    icon: category.icon || 'Grid',
    sortOrder: category.sortOrder || 0,
    remark: category.remark || ''
  }
  showFormDialog.value = true
}

// 删除
const handleDelete = async (category: TableCategoryResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${category.name}"吗？该分类下的桌台将变为未分类状态。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteCategory(category.id)
    ElMessage.success('删除成功')
    emit('refresh')
  } catch (error) {
    // 用户取消
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      await updateCategory(form.value)
      ElMessage.success('更新成功')
    } else {
      await createCategory(form.value)
      ElMessage.success('创建成功')
    }

    showFormDialog.value = false
    emit('refresh')
  } catch (error) {
    // 验证失败或请求失败
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.category-dialog {
  .category-list {
    max-height: 400px;
    overflow-y: auto;
  }

  .category-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 6px;
    margin-bottom: 8px;

    &.is-default {
      background: #e8ebf0;

      .category-name {
        font-weight: 500;
      }
    }

    .category-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .category-icon {
        font-size: 18px;
        color: #409eff;
      }

      .category-name {
        font-size: 14px;
        color: #303133;
      }
    }

    .category-actions {
      display: flex;
      gap: 8px;
    }
  }

  .add-btn-wrapper {
    margin-top: 16px;
    text-align: center;
  }
}
</style>
