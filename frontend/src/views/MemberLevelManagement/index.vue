<template>
  <div class="member-level-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>会员等级配置</span>
          <div>
            <el-button v-if="permissions.canInit" type="success" :icon="Refresh" @click="handleInitDefault">
              初始化默认等级
            </el-button>
            <el-button v-if="permissions.canCreate" type="primary" :icon="Plus" @click="handleAdd">
              新增等级
            </el-button>
          </div>
        </div>
      </template>

      <!-- 等级列表 -->
      <el-table
        v-loading="loading"
        :data="levelList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="等级名称" width="150" />
        <el-table-column prop="minAmount" label="最小金额" width="120">
          <template #default="{ row }">
            ¥{{ row.minAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="maxAmount" label="最大金额" width="120">
          <template #default="{ row }">
            {{ row.maxAmount ? `¥${row.maxAmount.toFixed(2)}` : '无上限' }}
          </template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣率" width="100">
          <template #default="{ row }">
            {{ (row.discountRate * 10).toFixed(1) }}折
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="permissions.canUpdate" type="primary" size="small" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-if="permissions.canDelete" type="danger" size="small" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 等级新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="等级名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="例如：豆豆萌新"
          />
        </el-form-item>
        <el-form-item label="最小累计金额" prop="minAmount">
          <el-input-number
            v-model="formData.minAmount"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
          <div class="form-tip">达到此金额即可升级到该等级</div>
        </el-form-item>
        <el-form-item label="最大累计金额" prop="maxAmount">
          <el-input-number
            v-model="formData.maxAmount"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%"
            :controls="false"
          />
          <div class="form-tip">留空表示无上限</div>
        </el-form-item>
        <el-form-item label="折扣率" prop="discountRate">
          <el-input-number
            v-model="formData.discountRate"
            :min="0.1"
            :max="1"
            :precision="3"
            :step="0.01"
            style="width: 100%"
          />
          <div class="form-tip">0.9表示9折，0.95表示9.5折</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="999"
            style="width: 100%"
          />
          <div class="form-tip">数字越小越靠前</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getMemberLevelList,
  createMemberLevel,
  updateMemberLevel,
  deleteMemberLevel,
  initDefaultMemberLevels,
  type MemberLevelInfo,
  type CreateMemberLevelParams,
  type UpdateMemberLevelParams
} from '@/api/memberLevel'

// 用户状态
const userStore = useUserStore()

// 权限检查
const permissions = computed(() => ({
  canInit: userStore.hasPermission('member:level:init'),
  canCreate: userStore.hasPermission('member:level:create'),
  canUpdate: userStore.hasPermission('member:level:update'),
  canDelete: userStore.hasPermission('member:level:delete')
}))

// 数据
const loading = ref(false)
const levelList = ref<MemberLevelInfo[]>([])

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增等级')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  id: 0,
  name: '',
  minAmount: 0,
  maxAmount: null as number | null,
  discountRate: 1.0,
  sort: 0
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入等级名称', trigger: 'blur' }
  ],
  minAmount: [
    { required: true, message: '请输入最小累计金额', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (formData.maxAmount !== null && value >= formData.maxAmount) {
          callback(new Error('最小金额必须小于最大金额'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  discountRate: [
    { required: true, message: '请输入折扣率', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

// 加载等级列表
const loadLevels = async () => {
  loading.value = true
  try {
    const result = await getMemberLevelList()
    levelList.value = result
  } catch (error) {
    ElMessage.error('加载等级列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化默认等级
const handleInitDefault = async () => {
  try {
    await ElMessageBox.confirm(
      '确认要初始化默认会员等级吗？这将创建4个默认等级。',
      '初始化默认等级',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await initDefaultMemberLevels()
    ElMessage.success('初始化成功')
    loadLevels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('初始化失败')
    }
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增等级'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: MemberLevelInfo) => {
  dialogTitle.value = '编辑等级'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    minAmount: row.minAmount,
    maxAmount: row.maxAmount,
    discountRate: row.discountRate,
    sort: row.sort
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: MemberLevelInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要删除等级 "${row.name}" 吗？删除后该等级的会员将需要重新分配等级。`,
      '删除等级',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteMemberLevel(row.id)
    ElMessage.success('删除成功')
    loadLevels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      const params: UpdateMemberLevelParams = {
        name: formData.name,
        minAmount: formData.minAmount,
        maxAmount: formData.maxAmount,
        discountRate: formData.discountRate,
        sort: formData.sort
      }
      await updateMemberLevel(formData.id, params)
      ElMessage.success('更新等级成功')
    } else {
      const params: CreateMemberLevelParams = {
        name: formData.name,
        minAmount: formData.minAmount,
        maxAmount: formData.maxAmount,
        discountRate: formData.discountRate,
        sort: formData.sort
      }
      await createMemberLevel(params)
      ElMessage.success('新增等级成功')
    }

    dialogVisible.value = false
    loadLevels()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '更新等级失败' : '新增等级失败')
    }
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  formData.id = 0
  formData.name = ''
  formData.minAmount = 0
  formData.maxAmount = null
  formData.discountRate = 1.0
  formData.sort = 0
  formRef.value?.resetFields()
}

// 格式化时间
const formatDateTime = (timestamp: number) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  loadLevels()
})
</script>

<style scoped>
.member-level-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
