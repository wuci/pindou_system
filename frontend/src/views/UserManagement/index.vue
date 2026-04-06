<template>
  <div class="user-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button v-if="permissions.canCreate" type="primary" :icon="Plus" @click="handleAdd">
            新增用户
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery">
          <el-form-item label="用户名">
            <el-input
              v-model="queryForm.username"
              placeholder="请输入用户名"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select
              v-model="queryForm.roleId"
              placeholder="全部角色"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="role in roleList"
                :key="role.id"
                :label="role.name"
                :value="role.id"
              />
            </el-select>
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

      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="roleName" label="角色" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="180">
          <template #default="{ row }">
            {{ row.lastLoginAt ? formatDateTime(row.lastLoginAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-if="permissions.canUpdate" type="primary" size="small" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-if="permissions.canResetPassword" type="warning" size="small" link @click="handleChangePassword(row)">
              修改密码
            </el-button>
            <el-button v-if="permissions.canResetPassword" type="info" size="small" link @click="handleResetPassword(row)">
              重置密码
            </el-button>
            <el-button v-if="permissions.canDelete" type="danger" size="small" link @click="handleDelete(row)">
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

    <!-- 用户新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="formData.nickname"
            placeholder="请输入昵称"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select
            v-model="formData.roleId"
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="450px"
      :close-on-click-modal="false"
      @close="handlePasswordDialogClose"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordFormData"
        :rules="passwordFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名">
          <el-input
            :value="passwordFormData.username"
            disabled
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordFormData.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位字符）"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordFormData.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSubmitting" @click="handleChangePasswordSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserList, createUser, updateUser, deleteUser, resetPassword, changePassword, type UserInfo } from '@/api/user'
import { getAllRoles } from '@/api/role'

// 用户状态
const userStore = useUserStore()
const router = useRouter()

// 权限检查
const permissions = computed(() => ({
  canCreate: userStore.hasPermission('user:create'),
  canUpdate: userStore.hasPermission('user:update'),
  canDelete: userStore.hasPermission('user:delete'),
  canResetPassword: userStore.hasPermission('user:resetPassword')
}))

interface Role {
  id: string
  name: string
}

// 数据
const loading = ref(false)
const userList = ref<UserInfo[]>([])
const total = ref(0)
const roleList = ref<Role[]>([])

// 查询表单
const queryForm = reactive({
  page: 1,
  pageSize: 10,
  username: '',
  roleId: '',
  status: undefined as number | undefined
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 修改密码弹窗相关
const passwordDialogVisible = ref(false)
const passwordSubmitting = ref(false)
const passwordFormRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  id: '',
  username: '',
  password: '',
  nickname: '',
  roleId: '',
  status: 1
})

// 修改密码表单数据
const passwordFormData = reactive({
  userId: '',
  username: '',
  newPassword: '',
  confirmPassword: ''
})

// 自定义验证：确认密码必须与新密码一致
const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordFormData.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 修改密码表单验证规则
const passwordFormRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const result = await getUserList(queryForm)
    userList.value = result.list
    total.value = result.total
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const result = await getAllRoles()
    roleList.value = result
  } catch (error) {
  }
}

// 查询
const handleQuery = () => {
  queryForm.page = 1
  loadUsers()
}

// 重置
const handleReset = () => {
  queryForm.username = ''
  queryForm.roleId = ''
  queryForm.status = undefined
  queryForm.page = 1
  loadUsers()
}

// 分页
const handlePageChange = (page: number) => {
  queryForm.page = page
  loadUsers()
}

const handleSizeChange = (size: number) => {
  queryForm.pageSize = size
  queryForm.page = 1
  loadUsers()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: UserInfo) => {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    roleId: row.roleId,
    status: row.status
  })
  dialogVisible.value = true
}

// 重置密码
const handleResetPassword = async (row: UserInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要重置用户 "${row.username}" 的密码吗？重置后密码为：123456`,
      '重置密码',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await resetPassword(row.id, '123456')
    ElMessage.success('重置密码成功，新密码为：123456')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置密码失败')
    }
  }
}

// 打开修改密码弹窗
const handleChangePassword = (row: UserInfo) => {
  passwordFormData.userId = row.id
  passwordFormData.username = row.username
  passwordFormData.newPassword = ''
  passwordFormData.confirmPassword = ''
  passwordDialogVisible.value = true
}

// 提交修改密码
const handleChangePasswordSubmit = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    passwordSubmitting.value = true

    await changePassword({
      userId: passwordFormData.userId,
      newPassword: passwordFormData.newPassword
    })

    ElMessage.success('修改密码成功')

    // 如果修改的是当前登录用户的密码，需要退出登录
    if (passwordFormData.userId === userStore.userInfo?.id) {
      await ElMessageBox.confirm(
        '密码已修改，需要重新登录',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
          showCancelButton: false,
          closeOnClickModal: false,
          closeOnPressEscape: false
        }
      )

      // 退出登录并跳转到登录页
      await userStore.logout()
      router.push('/login')
    } else {
      passwordDialogVisible.value = false
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('修改密码失败')
    }
  } finally {
    passwordSubmitting.value = false
  }
}

// 关闭修改密码弹窗
const handlePasswordDialogClose = () => {
  passwordFormData.userId = ''
  passwordFormData.username = ''
  passwordFormData.newPassword = ''
  passwordFormData.confirmPassword = ''
  passwordFormRef.value?.resetFields()
}

// 删除
const handleDelete = async (row: UserInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要删除用户 "${row.username}" 吗？`,
      '删除用户',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadUsers()
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
      await updateUser(formData.id, {
        nickname: formData.nickname,
        roleId: formData.roleId,
        status: formData.status
      })
      ElMessage.success('更新用户成功')
    } else {
      await createUser({
        username: formData.username,
        password: formData.password,
        nickname: formData.nickname,
        roleId: formData.roleId,
        status: formData.status
      })
      ElMessage.success('新增用户成功')
    }

    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '更新用户失败' : '新增用户失败')
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
  formData.username = ''
  formData.password = ''
  formData.nickname = ''
  formData.roleId = ''
  formData.status = 1
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
  loadUsers()
  loadRoles()
})
</script>

<style scoped>
.user-management {
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
</style>
