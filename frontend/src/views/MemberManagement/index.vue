<template>
  <div class="member-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>会员管理</span>
          <el-button v-if="permissions.canCreate" type="primary" :icon="Plus" @click="handleAdd">
            新增会员
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery">
          <el-form-item label="关键词">
            <el-input
              v-model="queryForm.keyword"
              placeholder="手机号或姓名"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 会员列表 -->
      <el-table
        v-loading="loading"
        :data="memberList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="levelName" label="等级" width="120">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.levelName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣" width="100">
          <template #default="{ row }">
            {{ (row.discountRate * 10).toFixed(1) }}折
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="累计消费" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="120">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: 600;">¥{{ row.balance.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button v-if="permissions.canRecharge" type="success" size="small" link @click="handleRecharge(row)">
              充值
            </el-button>
            <el-button v-if="permissions.canViewRecord" type="info" size="small" link @click="handleViewRecords(row)">
              查看记录
            </el-button>
            <el-button v-if="permissions.canUpdate" type="primary" size="small" link @click="handleEdit(row)">
              编辑
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

    <!-- 会员新增/编辑弹窗 -->
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
        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入姓名"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入手机号"
            :disabled="isEdit"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item label="地址">
          <el-input
            v-model="formData.address"
            type="textarea"
            placeholder="请输入地址（选填）"
            :rows="3"
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

    <!-- 充值弹窗 -->
    <RechargeDialog
      v-model="rechargeDialogVisible"
      :member="currentMember"
      @success="handleRechargeSuccess"
    />

    <!-- 记录查看弹窗 -->
    <el-dialog
      v-model="recordsDialogVisible"
      :title="`${currentMember?.name || '会员'} - 交易记录`"
      width="900px"
      @close="handleRecordsDialogClose"
    >
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 充值记录 -->
        <el-tab-pane label="充值记录" name="recharge">
          <el-table
            v-loading="rechargeRecordsLoading"
            :data="rechargeRecords"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="amount" label="充值金额" width="120">
              <template #default="{ row }">
                <span style="color: #67c23a; font-weight: 600;">+¥{{ row.amount.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="balanceBefore" label="充值前余额" width="120">
              <template #default="{ row }">
                ¥{{ row.balanceBefore.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="balanceAfter" label="充值后余额" width="120">
              <template #default="{ row }">
                ¥{{ row.balanceAfter.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethodName" label="支付方式" width="100" />
            <el-table-column prop="operatorName" label="操作人" width="100" />
            <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column label="充值时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>

          <!-- 充值记录分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="rechargePagination.page"
              v-model:page-size="rechargePagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="rechargePagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="loadRechargeRecords"
              @current-change="loadRechargeRecords"
            />
          </div>
        </el-tab-pane>

        <!-- 消费记录 -->
        <el-tab-pane label="消费记录" name="consumption">
          <el-table
            v-loading="consumptionRecordsLoading"
            :data="consumptionRecords"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="orderId" label="订单号" width="100" />
            <el-table-column prop="amount" label="消费金额" width="120">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: 600;">-¥{{ row.amount.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="balanceBefore" label="消费前余额" width="120">
              <template #default="{ row }">
                ¥{{ row.balanceBefore.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="balanceAfter" label="消费后余额" width="120">
              <template #default="{ row }">
                ¥{{ row.balanceAfter.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
            <el-table-column label="消费时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>

          <!-- 消费记录分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="consumptionPagination.page"
              v-model:page-size="consumptionPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="consumptionPagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="loadConsumptionRecords"
              @current-change="loadConsumptionRecords"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getMemberList,
  createMember,
  updateMember,
  deleteMember,
  getRechargeRecords,
  getConsumptionRecords,
  type MemberInfo,
  type CreateMemberParams,
  type UpdateMemberParams,
  type RechargeRecord,
  type ConsumptionRecord
} from '@/api/member'
import RechargeDialog from '@/components/RechargeDialog.vue'

// 用户状态
const userStore = useUserStore()

// 权限检查
const permissions = computed(() => ({
  canCreate: userStore.hasPermission('member:create'),
  canUpdate: userStore.hasPermission('member:update'),
  canDelete: userStore.hasPermission('member:delete'),
  canRecharge: userStore.hasPermission('member:recharge'),
  canViewRecord: userStore.hasPermission('member:record')
}))

// 数据
const loading = ref(false)
const memberList = ref<MemberInfo[]>([])
const total = ref(0)

// 查询表单
const queryForm = reactive({
  page: 1,
  pageSize: 10,
  keyword: ''
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增会员')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  id: 0,
  name: '',
  phone: '',
  address: ''
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 加载会员列表
const loadMembers = async () => {
  loading.value = true
  try {
    const result = await getMemberList(queryForm)
    memberList.value = result.list
    total.value = result.total
  } catch (error) {
    ElMessage.error('加载会员列表失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.page = 1
  loadMembers()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.page = 1
  loadMembers()
}

// 分页
const handlePageChange = (page: number) => {
  queryForm.page = page
  loadMembers()
}

const handleSizeChange = (size: number) => {
  queryForm.pageSize = size
  queryForm.page = 1
  loadMembers()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增会员'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: MemberInfo) => {
  dialogTitle.value = '编辑会员'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    phone: row.phone,
    address: row.address || ''
  })
  dialogVisible.value = true
}

// 充值
const rechargeDialogVisible = ref(false)
const currentMember = ref<MemberInfo | null>(null)

const handleRecharge = (row: MemberInfo) => {
  currentMember.value = row
  rechargeDialogVisible.value = true
}

const handleRechargeSuccess = () => {
  loadMembers()
}

// 记录查看
const recordsDialogVisible = ref(false)
const activeTab = ref('recharge')
const rechargeRecords = ref<RechargeRecord[]>([])
const consumptionRecords = ref<ConsumptionRecord[]>([])
const rechargeRecordsLoading = ref(false)
const consumptionRecordsLoading = ref(false)

// 充值记录分页
const rechargePagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 消费记录分页
const consumptionPagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const handleViewRecords = (row: MemberInfo) => {
  currentMember.value = row
  recordsDialogVisible.value = true
  activeTab.value = 'recharge'
  loadRechargeRecords()
}

const handleRecordsDialogClose = () => {
  activeTab.value = 'recharge'
  rechargeRecords.value = []
  consumptionRecords.value = []
  rechargePagination.page = 1
  consumptionPagination.page = 1
}

const loadRechargeRecords = async () => {
  if (!currentMember.value) return
  rechargeRecordsLoading.value = true
  try {
    const result = await getRechargeRecords(
      currentMember.value.id,
      rechargePagination.page,
      rechargePagination.pageSize
    )
    rechargeRecords.value = result.list
    rechargePagination.total = result.total
  } catch (error) {
    ElMessage.error('加载充值记录失败')
  } finally {
    rechargeRecordsLoading.value = false
  }
}

const loadConsumptionRecords = async () => {
  if (!currentMember.value) return
  consumptionRecordsLoading.value = true
  try {
    const result = await getConsumptionRecords(
      currentMember.value.id,
      consumptionPagination.page,
      consumptionPagination.pageSize
    )
    consumptionRecords.value = result.list
    consumptionPagination.total = result.total
  } catch (error) {
    ElMessage.error('加载消费记录失败')
  } finally {
    consumptionRecordsLoading.value = false
  }
}

// 监听tab切换，加载数据
watch(activeTab, (newTab) => {
  if (newTab === 'recharge' && rechargeRecords.value.length === 0) {
    loadRechargeRecords()
  } else if (newTab === 'consumption' && consumptionRecords.value.length === 0) {
    loadConsumptionRecords()
  }
})

// 删除
const handleDelete = async (row: MemberInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要删除会员 "${row.name}" 吗？`,
      '删除会员',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteMember(row.id)
    ElMessage.success('删除成功')
    loadMembers()
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
      const params: UpdateMemberParams = {
        name: formData.name,
        address: formData.address || undefined
      }
      await updateMember(formData.id, params)
      ElMessage.success('更新会员成功')
    } else {
      const params: CreateMemberParams = {
        name: formData.name,
        phone: formData.phone,
        address: formData.address || undefined
      }
      await createMember(params)
      ElMessage.success('新增会员成功')
    }

    dialogVisible.value = false
    loadMembers()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '更新会员失败' : '新增会员失败')
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
  formData.phone = ''
  formData.address = ''
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
  loadMembers()
})
</script>

<style scoped>
.member-management {
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
