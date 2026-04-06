<template>
  <div class="log-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <el-button v-if="permissions.canExport" type="primary" :icon="Download" @click="handleExport">
            导出日志
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery">
          <el-form-item label="操作模块">
            <el-select
              v-model="queryForm.module"
              placeholder="全部模块"
              clearable
              style="width: 150px"
            >
              <el-option label="用户管理" value="用户管理" />
              <el-option label="角色管理" value="角色管理" />
              <el-option label="桌台管理" value="桌台管理" />
              <el-option label="订单管理" value="订单管理" />
              <el-option label="系统配置" value="系统配置" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作类型">
            <el-select
              v-model="queryForm.operation"
              placeholder="全部类型"
              clearable
              style="width: 120px"
            >
              <el-option label="新增" value="新增" />
              <el-option label="修改" value="修改" />
              <el-option label="删除" value="删除" />
              <el-option label="查询" value="查询" />
              <el-option label="登录" value="登录" />
              <el-option label="登出" value="登出" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="queryForm.status"
              placeholder="全部状态"
              clearable
              style="width: 120px"
            >
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="x"
              @change="handleDateChange"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 日志列表 -->
      <el-table
        v-loading="loading"
        :data="logList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operation" label="操作类型" width="100" />
        <el-table-column prop="description" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column label="执行时长" width="100">
          <template #default="{ row }">
            {{ row.duration }}ms
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="permissions.canViewDetail"
              type="primary"
              size="small"
              link
              @click="handleViewDetail(row)"
            >
              详情
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

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="日志详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作模块">{{ detailLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detailLog.operation }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ detailLog.description }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailLog.username }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ detailLog.method }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ detailLog.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailLog.status === 1 ? 'success' : 'danger'" size="small">
            {{ detailLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailLog.params" label="请求参数" :span="2">
          <div class="json-content">{{ detailLog.params }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailLog.errorMsg" label="错误信息" :span="2">
          <div class="error-text">{{ detailLog.errorMsg }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDateTime(detailLog.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getLogs, exportLogs, type LogInfo, type LogQueryParams } from '@/api/log'
import { useUserStore } from '@/stores/user'

// 用户状态和权限
const userStore = useUserStore()

const permissions = computed(() => ({
  canViewDetail: userStore.hasPermission('log:detail'),
  canExport: userStore.hasPermission('log:export')
}))

// 数据
const loading = ref(false)
const logList = ref<LogInfo[]>([])
const total = ref(0)
const dateRange = ref<number[]>([])

// 查询表单
const queryForm = reactive<LogQueryParams>({
  page: 1,
  pageSize: 10,
  module: '',
  operation: '',
  status: undefined,
  startTime: undefined,
  endTime: undefined
})

// 详情弹窗
const detailVisible = ref(false)
const detailLog = ref<LogInfo>({} as LogInfo)

// 加载日志列表
const loadLogs = async () => {
  loading.value = true
  try {
    const result = await getLogs(queryForm)
    logList.value = result.list
    total.value = result.total
  } catch (error) {
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.page = 1
  loadLogs()
}

// 重置
const handleReset = () => {
  dateRange.value = []
  queryForm.module = ''
  queryForm.operation = ''
  queryForm.status = undefined
  queryForm.startTime = undefined
  queryForm.endTime = undefined
  queryForm.page = 1
  loadLogs()
}

// 日期变化
const handleDateChange = (values: number[]) => {
  if (values && values.length === 2) {
    queryForm.startTime = values[0]
    queryForm.endTime = values[1]
  } else {
    queryForm.startTime = undefined
    queryForm.endTime = undefined
  }
}

// 分页
const handlePageChange = (page: number) => {
  queryForm.page = page
  loadLogs()
}

const handleSizeChange = (size: number) => {
  queryForm.pageSize = size
  queryForm.page = 1
  loadLogs()
}

// 查看详情
const handleViewDetail = (row: LogInfo) => {
  detailLog.value = row
  detailVisible.value = true
}

// 导出
const handleExport = async () => {
  try {
    const data = await exportLogs({
      module: queryForm.module,
      operation: queryForm.operation,
      startTime: queryForm.startTime,
      endTime: queryForm.endTime
    })

    // 创建下载链接
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${new Date().toLocaleDateString()}.json`
    link.click()
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
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
    minute: '2-digit',
    second: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.log-page {
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

.json-content {
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

.error-text {
  color: #f56c6c;
}
</style>
