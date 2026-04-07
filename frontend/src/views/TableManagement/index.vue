<template>
  <div class="table-management">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="never">
      <div class="header">
        <div class="header__main">
          <h2 class="header__title">桌台管理</h2>
          <div class="header__actions">
            <!-- 搜索框 -->
            <el-input
              v-model="searchKeyword"
              placeholder="搜索桌台名称"
              :prefix-icon="Search"
              clearable
              size="small"
              style="width: 200px; margin-right: 8px"
              @input="handleSearch"
              @clear="handleSearch"
            />
            <!-- 已超时筛选 -->
            <el-checkbox
              v-model="isOvertime"
              size="small"
              style="margin-right: 8px"
              @change="handleSearch"
            >
              已超时
            </el-checkbox>
            <!-- 剩余分钟数筛选 -->
            <el-input-number
              v-model="remainingMinutes"
              placeholder="剩余分钟"
              :min="1"
              :max="999"
              size="small"
              style="width: 130px; margin-right: 8px"
              controls-position="right"
              @change="handleSearch"
            />
            <span style="font-size: 13px; color: #909399; margin-right: 12px">分钟内到期</span>
            <!-- 批量操作按钮 -->
            <template v-if="batchSelectionMode">
              <span class="selected-count">已选择 {{ selectedTableIds.size }} 个桌台</span>
              <el-button v-if="permissions.canBatchDelete" type="danger" :icon="Delete" size="small" @click="handleBatchDelete" :disabled="selectedTableIds.size === 0">
                批量删除
              </el-button>
              <el-button size="small" @click="exitBatchSelectMode">退出选择</el-button>
            </template>
            <!-- 正常操作按钮 -->
            <template v-else>
              <el-button v-if="permissions.canManageCategories" type="primary" :icon="Setting" size="small" @click="showCategoryDialog = true">
                分类管理
              </el-button>
              <el-button v-if="currentCategory !== 0 && permissions.canConfig" type="primary" :icon="Setting" size="small" @click="showConfigDialog = true">
                桌台配置
              </el-button>
              <el-button v-if="permissions.canBatchDelete" type="warning" :icon="Delete" size="small" @click="enterBatchSelectMode">
                批量删除
              </el-button>
              <el-button :icon="Refresh" size="small" @click="refreshTables" :loading="loading">
                刷新
              </el-button>
            </template>
          </div>
        </div>
        <div class="header__bottom">
          <div class="category-tabs">
            <div
              v-for="category in categories"
              :key="category.id"
              :class="['category-tab', { active: currentCategory === category.id }]"
              @click="switchCategory(category.id)"
            >
              <el-icon v-if="category.icon && iconMap[category.icon]">
                <component :is="iconMap[category.icon]" />
              </el-icon>
              <span>{{ category.name }}</span>
              <span class="count">({{ category.tableCount }})</span>
            </div>
          </div>
          <div class="header__stats">
            <el-tag size="small" type="success">空闲: {{ idleCount }}</el-tag>
            <el-tag size="small" type="primary">使用中: {{ usingCount }}</el-tag>
            <el-tag size="small" type="warning">暂停: {{ pausedCount }}</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 布局编辑器 -->
    <TableLayoutEditor
      :tables="tables"
      :category-id="currentCategory"
      :categories="categories"
      :selection-mode="batchSelectionMode"
      :selected-table-ids="selectedTableIds"
      @start="handleStart"
      @pause="handlePause"
      @resume="handleResume"
      @end="handleEnd"
      @extend="handleExtend"
      @ignoreRemind="handleIgnoreRemind"
      @refresh="refreshTables"
      @edit="handleEdit"
      @select="handleTableSelect"
      @batch-delete="handleBatchDelete"
      @reserve="handleReserve"
      @cancelReservation="handleCancelReservation"
    />

    <!-- 开始计时对话框 -->
    <StartTimerDialog
      v-model="showStartDialog"
      :table="selectedTable"
      @success="refreshTables"
    />

    <!-- 结账对话框 -->
    <BillDialog
      v-model="showBillDialog"
      :table-id="selectedTable?.id ?? null"
      @confirmed="refreshTables"
    />

    <!-- 续费对话框 -->
    <ExtendDialog
      v-model="showExtendDialog"
      :table="selectedTable"
      @success="refreshTables"
    />

    <!-- 预定对话框 -->
    <ReservationDialog
      v-model="showReservationDialog"
      :table="selectedTable"
      @success="refreshTables"
    />

    <!-- 桌台配置对话框 -->
    <TableConfigDialog
      v-model="showConfigDialog"
      :categories="categories"
      :tables="tables"
      :category-id="currentCategory"
      @success="refreshTables"
    />

    <!-- 编辑桌台对话框 -->
    <EditTableDialog
      v-model="showEditDialog"
      :table="editingTable"
      :categories="categories"
      @success="handleEditSuccess"
    />

    <!-- 分类管理对话框 -->
    <CategoryDialog
      v-model="showCategoryDialog"
      :categories="categories"
      @refresh="loadCategories"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, type Component } from 'vue'
import { Setting, Refresh, Delete, Search, Grid, List, Star, OfficeBuilding, House } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getTableList, batchDeleteTables, cancelReservation, type TableInfo } from '@/api/table'
import { getCategories, type TableCategoryResponse } from '@/api/tableCategory'
import TableLayoutEditor from '@/components/TableLayoutEditor.vue'
import StartTimerDialog from '@/components/StartTimerDialog.vue'
import BillDialog from '@/components/BillDialog.vue'
import ExtendDialog from '@/components/ExtendDialog.vue'
import ReservationDialog from '@/components/ReservationDialog.vue'
import TableConfigDialog from './components/TableConfigDialog.vue'
import EditTableDialog from './components/EditTableDialog.vue'
import CategoryDialog from './components/CategoryDialog.vue'

// 用户状态
const userStore = useUserStore()

// 权限检查
const permissions = computed(() => ({
  canManageCategories: userStore.hasPermission('table:config'),
  canConfig: userStore.hasPermission('table:config'),
  canBatchDelete: userStore.hasPermission('table:delete')
}))

// 数据
const tables = ref<TableInfo[]>([])
const categories = ref<TableCategoryResponse[]>([])
const currentCategory = ref<number>(0)
const loading = ref(false)
const selectedTable = ref<TableInfo | null>(null)
const editingTable = ref<TableInfo | null>(null)
const selectedTableIds = ref<Set<number>>(new Set())
const batchSelectionMode = ref(false)
const showStartDialog = ref(false)
const showBillDialog = ref(false)
const showExtendDialog = ref(false)
const showReservationDialog = ref(false)
const showConfigDialog = ref(false)
const showEditDialog = ref(false)
const showCategoryDialog = ref(false)
const searchKeyword = ref('')
// 查询条件
const isOvertime = ref(false)
const remainingMinutes = ref<number | null>(null)

// 图标组件映射
const iconMap: Record<string, Component> = {
  Grid,
  List,
  Star,
  OfficeBuilding,
  House
}

let refreshTimer: number | null = null

// 统计数据
const idleCount = computed(() => tables.value.filter(t => t.status === 'idle').length)
const usingCount = computed(() => tables.value.filter(t => t.status === 'using').length)
const pausedCount = computed(() => tables.value.filter(t => t.status === 'paused').length)

// 加载分类
const loadCategories = async () => {
  try {
    const data = await getCategories()
    categories.value = data
  } catch (error) {
  }
}

// 加载桌台
const loadTables = async () => {
  try {
    loading.value = true
    const data = await getTableList(
      '',
      currentCategory.value,
      searchKeyword.value,
      isOvertime.value || undefined,
      remainingMinutes.value || undefined
    )
    tables.value = data
  } catch (error) {
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  loadTables()
}

// 切换分类
const switchCategory = (categoryId: number) => {
  currentCategory.value = categoryId
  loadTables()
}

// 刷新桌台
const refreshTables = () => {
  loadTables()
  loadCategories()
}

// 开始计时
const handleStart = (table: TableInfo) => {
  selectedTable.value = table
  showStartDialog.value = true
}

// 暂停
const handlePause = async (_table: TableInfo) => {
  refreshTables()
}

// 继续
const handleResume = async (_table: TableInfo) => {
  refreshTables()
}

// 结束
const handleEnd = (table: TableInfo) => {
  selectedTable.value = table
  showBillDialog.value = true
}

// 续费
const handleExtend = (table: TableInfo) => {
  selectedTable.value = table
  showExtendDialog.value = true
}

// 忽略提醒
const handleIgnoreRemind = async (_table: TableInfo) => {
  refreshTables()
}

// 预定桌台
const handleReserve = (table: TableInfo) => {
  selectedTable.value = table
  showReservationDialog.value = true
}

// 取消预定
const handleCancelReservation = async (table: TableInfo) => {
  try {
    await ElMessageBox.confirm(
      `确认要取消桌台「${table.name}」的预定吗？`,
      '取消预定',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    await cancelReservation(table.id)
    ElMessage.success('取消预定成功')
    refreshTables()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消预定失败')
    }
  }
}

// 编辑桌台
const handleEdit = (table: TableInfo) => {
  editingTable.value = table
  showEditDialog.value = true
}

// 编辑成功
const handleEditSuccess = () => {
  refreshTables()
  loadCategories()
}

// 进入批量选择模式
const enterBatchSelectMode = () => {
  batchSelectionMode.value = true
  selectedTableIds.value.clear()
}

// 退出批量选择模式
const exitBatchSelectMode = () => {
  batchSelectionMode.value = false
  selectedTableIds.value.clear()
}

// 桌台选择处理
const handleTableSelect = (tableId: number, selected: boolean) => {
  if (selected) {
    selectedTableIds.value.add(tableId)
  } else {
    selectedTableIds.value.delete(tableId)
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedTableIds.value.size === 0) {
    ElMessage.warning('请先选择要删除的桌台')
    return
  }

  // 检查选中的桌台是否都是空闲状态
  const nonIdleTables = tables.value.filter(t =>
    selectedTableIds.value.has(t.id) && t.status !== 'idle'
  )

  if (nonIdleTables.length > 0) {
    ElMessage.warning(`选中的桌台中有 ${nonIdleTables.length} 个正在使用，无法删除`)
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认要删除选中的 ${selectedTableIds.value.size} 个桌台吗？`,
      '批量删除',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    await batchDeleteTables(Array.from(selectedTableIds.value))
    ElMessage.success('删除成功')
    exitBatchSelectMode()
    refreshTables()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 定时刷新
const startRefresh = () => {
  refreshTimer = window.setInterval(() => {
    loadTables()
    loadCategories()
  }, 5000)
}

const stopRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 生命周期
onMounted(() => {
  loadCategories()
  loadTables()
  startRefresh()
})

onUnmounted(() => {
  stopRefresh()
})
</script>

<style scoped lang="scss">
.table-management {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header-card {
  margin-bottom: 12px;
  flex-shrink: 0;

  :deep(.el-card__body) {
    padding: 16px;
  }
}

.header {
  &__main {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  &__title {
    margin: 0;
    font-size: 18px;
    color: #303133;
    font-weight: 500;
  }

  &__actions {
    display: flex;
    gap: 8px;
  }

  .selected-count {
    margin-right: 12px;
    font-size: 14px;
    color: #606266;
  }

  &__bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
  }

  &__stats {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
}

.category-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  flex: 1;
  min-width: 0;

  .category-tab {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 5px 10px;
    background: #f5f7fa;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;
    user-select: none;
    font-size: 13px;
    white-space: nowrap;

    &:hover {
      background: #e8ebf0;
    }

    &.active {
      background: #409eff;
      color: #fff;

      .count {
        color: rgba(255, 255, 255, 0.8);
      }
    }

    .count {
      font-size: 11px;
      color: #909399;
    }
  }
}
</style>
