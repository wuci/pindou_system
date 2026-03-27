<template>
  <div class="table-management">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="never">
      <div class="header">
        <div class="header__left">
          <h2 class="header__title">桌台管理</h2>
          <div class="header__stats">
            <el-tag type="success">空闲: {{ idleCount }}</el-tag>
            <el-tag type="primary">使用中: {{ usingCount }}</el-tag>
            <el-tag type="warning">暂停: {{ pausedCount }}</el-tag>
          </div>
        </div>
        <div class="header__right">
          <el-button type="primary" :icon="Setting" @click="showConfigDialog = true">
            桌台配置
          </el-button>
          <el-button :icon="Refresh" @click="refreshTables" :loading="loading">
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 布局编辑器 -->
    <TableLayoutEditor
      :tables="tables"
      @start="handleStart"
      @pause="handlePause"
      @resume="handleResume"
      @end="handleEnd"
      @ignoreRemind="handleIgnoreRemind"
      @refresh="refreshTables"
    />

    <!-- 开始计时对话框 -->
    <StartTimerDialog
      v-model="showStartDialog"
      :table="selectedTable"
      @success="refreshTables"
    />

    <!-- 桌台配置对话框 -->
    <el-dialog
      v-model="showConfigDialog"
      title="桌台配置"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-width="120px">
        <el-alert
          title="配置说明"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <ul style="margin: 8px 0; padding-left: 20px">
            <li>增加桌台：自动创建新的空闲桌台</li>
            <li>减少桌台：只删除空闲状态的桌台</li>
            <li>桌台数量范围：1-50个</li>
          </ul>
        </el-alert>

        <el-form-item label="当前桌台数">
          <el-tag type="info" size="large">{{ tables.length }} 个</el-tag>
        </el-form-item>

        <el-form-item label="设置桌台数" prop="tableCount">
          <el-input-number
            v-model="configForm.tableCount"
            :min="1"
            :max="50"
            :step="1"
            controls-position="right"
            style="width: 200px"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button type="primary" :loading="configLoading" @click="handleConfig">
          确认配置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Setting, Refresh } from '@element-plus/icons-vue'
import TableLayoutEditor from '@/components/TableLayoutEditor.vue'
import StartTimerDialog from '@/components/StartTimerDialog.vue'
import { getTableList, configTableCount, pauseTable, resumeTable, endTable, ignoreRemind, type TableInfo } from '@/api/table'

// 数据
const tables = ref<TableInfo[]>([])
const loading = ref(false)
const selectedTable = ref<TableInfo | null>(null)

// 对话框状态
const showStartDialog = ref(false)
const showConfigDialog = ref(false)

// 配置表单
const configFormRef = ref<FormInstance>()
const configForm = ref({
  tableCount: 10
})
const configLoading = ref(false)
const configRules: FormRules = {
  tableCount: [
    { required: true, message: '请输入桌台数量', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value < 1 || value > 50) {
          callback(new Error('桌台数量范围为1-50'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 统计数据
const idleCount = computed(() => {
  if (!Array.isArray(tables.value)) return 0
  return tables.value.filter(t => t.status === 'idle').length
})
const usingCount = computed(() => {
  if (!Array.isArray(tables.value)) return 0
  return tables.value.filter(t => t.status === 'using').length
})
const pausedCount = computed(() => {
  if (!Array.isArray(tables.value)) return 0
  return tables.value.filter(t => t.status === 'paused').length
})

// 轮询定时器
let pollingTimer: number | null = null

// 获取桌台列表
const fetchTables = async () => {
  loading.value = true
  try {
    const data = await getTableList()

    // 确保 tables 始终是数组
    tables.value = Array.isArray(data) ? data : []

    // 更新配置表单的桌台数量
    configForm.value.tableCount = tables.value.length
  } catch (error: any) {
    console.error('获取桌台列表失败:', error)
    // 发生错误时，确保 tables 是空数组
    tables.value = []
    ElMessage.error(error.message || '获取桌台列表失败')
  } finally {
    loading.value = false
  }
}

// 刷新桌台列表
const refreshTables = () => {
  fetchTables()
}

// 启动轮询
const startPolling = () => {
  if (pollingTimer !== null) return
  pollingTimer = window.setInterval(() => {
    fetchTables()
  }, 5000) // 每5秒轮询一次
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer !== null) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 桌台卡片点击事件
const handleTableClick = (table: TableInfo) => {
  selectedTable.value = table
  // 可以在这里添加点击后显示详情的逻辑
}

// 开始计时
const handleStart = (table: TableInfo) => {
  selectedTable.value = table
  showStartDialog.value = true
}

// 暂停计时
const handlePause = async (table: TableInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要暂停 ${table.name} 的计时吗？`,
      '暂停计时',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await pauseTable(table.id, {})
    ElMessage.success('暂停计时成功')
    refreshTables()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '暂停计时失败')
    }
  }
}

// 恢复计时
const handleResume = async (table: TableInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要恢复 ${table.name} 的计时吗？`,
      '恢复计时',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await resumeTable(table.id)
    ElMessage.success('恢复计时成功')
    refreshTables()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '恢复计时失败')
    }
  }
}

// 结束结账
const handleEnd = async (table: TableInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要结束 ${table.name} 的计时并结账吗？`,
      '结束结账',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await endTable(table.id)
    ElMessage.success('结账成功')
    refreshTables()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '结账失败')
    }
  }
}

// 忽略提醒
const handleIgnoreRemind = async (table: TableInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要忽略 ${table.name} 的提醒吗？`,
      '忽略提醒',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 调用忽略提醒接口
    await ignoreRemind(table.id)
    ElMessage.success('已忽略提醒')
    refreshTables()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '忽略提醒失败')
    }
  }
}

// 桌台配置
const handleConfig = async () => {
  if (!configFormRef.value) return

  try {
    await configFormRef.value.validate()
    configLoading.value = true

    await configTableCount(configForm.value.tableCount)

    ElMessage.success('桌台配置成功')
    showConfigDialog.value = false

    // 刷新桌台列表
    await refreshTables()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '桌台配置失败')
    }
  } finally {
    configLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  fetchTables()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.table-management {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.header-card {
  margin-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header__left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header__stats {
  display: flex;
  gap: 12px;
}

.header__right {
  display: flex;
  gap: 12px;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header__left {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header__right {
    width: 100%;
    flex-direction: column;
  }

  .header__right .el-button {
    width: 100%;
  }
}

/* 响应式布局 */
@media (max-width: 768px) {
  .table-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header__left {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header__right {
    width: 100%;
    flex-direction: column;
  }

  .header__right .el-button {
    width: 100%;
  }
}
</style>
