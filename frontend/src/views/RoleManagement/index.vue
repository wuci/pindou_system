<template>
  <div class="role-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button v-if="permissions.canCreate" type="primary" :icon="Plus" @click="handleAdd">
            新增角色
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery">
          <el-form-item label="角色名称">
            <el-input
              v-model="queryForm.name"
              placeholder="请输入角色名称"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="角色编码">
            <el-input
              v-model="queryForm.code"
              placeholder="请输入角色编码"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="queryForm.status"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 角色列表 -->
      <el-table
        v-loading="loading"
        :data="roleList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色编码" width="150" />
        <el-table-column label="权限数量" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.permissions?.length || 0 }} 个</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isBuiltIn === 1" type="warning" size="small">系统</el-tag>
            <el-tag v-else type="info" size="small">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
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
            <el-button
              v-if="permissions.canDelete && row.code !== 'super_admin'"
              type="danger"
              size="small"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 角色新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色名称" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入角色名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色编码" prop="code">
              <el-input
                v-model="formData.code"
                placeholder="请输入角色编码"
                :disabled="isEdit"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number
                v-model="formData.sort"
                :min="0"
                :max="999"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权限配置">
          <div class="permission-container">
            <PermissionTree ref="permissionTreeRef" v-model="formData.permissions" />
            <div class="permission-summary">
              已选择 <el-tag type="primary" size="small">{{ formData.permissions?.length || 0 }}</el-tag> 项权限
            </div>
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
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
import { Plus } from '@element-plus/icons-vue'
import PermissionTree from '@/components/PermissionTree.vue'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  type RoleInfo,
  type CreateRoleParams,
  type UpdateRoleParams
} from '@/api/role'
import { useUserStore } from '@/stores/user'

// 用户状态和权限
const userStore = useUserStore()

const permissions = computed(() => ({
  canCreate: userStore.hasPermission('role:create'),
  canUpdate: userStore.hasPermission('role:update'),
  canDelete: userStore.hasPermission('role:delete')
}))

// 数据
const loading = ref(false)
const roleList = ref<RoleInfo[]>([])
const total = ref(0)

// 查询表单
const queryForm = reactive({
  page: 1,
  pageSize: 10,
  name: '',
  code: '',
  status: undefined as number | undefined
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const permissionTreeRef = ref()

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  code: '',
  permissions: [] as string[],
  sort: 0,
  status: 1,
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '角色编码只能包含字母、数字和下划线', trigger: 'blur' }
  ]
}

// 加载角色列表
const loadRoles = async () => {
  loading.value = true
  try {
    const result = await getRoleList(queryForm)
    roleList.value = result.list
    total.value = result.total
  } catch (error) {
    ElMessage.error('加载角色列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.page = 1
  loadRoles()
}

// 重置
const handleReset = () => {
  queryForm.name = ''
  queryForm.code = ''
  queryForm.status = undefined
  queryForm.page = 1
  loadRoles()
}

// 分页
const handlePageChange = (page: number) => {
  queryForm.page = page
  loadRoles()
}

const handleSizeChange = (size: number) => {
  queryForm.pageSize = size
  queryForm.page = 1
  loadRoles()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: RoleInfo) => {
  dialogTitle.value = '编辑角色'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code,
    permissions: [...(row.permissions || [])],
    sort: row.sort,
    status: row.status,
    description: row.description
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: RoleInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要删除角色 "${row.name}" 吗？删除后不可恢复！`,
      '删除角色',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error: any) {
    // 点击取消时，error 是 'cancel' 字符串
    if (error === 'cancel') {
      return
    }

    // 显示后端返回的具体错误信息
    const errorMessage = error?.message || '删除失败'
    ElMessage.error(errorMessage)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      const params: UpdateRoleParams = {
        id: formData.id,
        name: formData.name,
        permissions: formData.permissions,
        sort: formData.sort,
        status: formData.status,
        description: formData.description
      }
      await updateRole(params)
      ElMessage.success('更新角色成功')
    } else {
      const params: CreateRoleParams = {
        name: formData.name,
        code: formData.code,
        permissions: formData.permissions,
        sort: formData.sort,
        status: formData.status,
        description: formData.description
      }
      await createRole(params)
      ElMessage.success('新增角色成功')
    }

    dialogVisible.value = false
    loadRoles()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '更新角色失败' : '新增角色失败')
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
  formData.id = ''
  formData.name = ''
  formData.code = ''
  formData.permissions = []
  formData.sort = 0
  formData.status = 1
  formData.description = ''
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
  loadRoles()
})
</script>

<style scoped>
.role-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.permission-container {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.permission-summary {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  font-size: 13px;
  color: #606266;
}
</style>
