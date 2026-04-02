<template>
  <div class="table-layout-editor">
    <!-- 工具栏（只在具体分类下显示） -->
    <div v-if="categoryId !== 0" class="editor-toolbar">
      <div class="toolbar-left">
        <!-- 视图切换 -->
        <el-button-group>
          <el-button :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'">
            <el-icon><Grid /></el-icon>
            网格视图
          </el-button>
          <el-button :type="viewMode === 'layout' ? 'primary' : ''" @click="viewMode = 'layout'">
            <el-icon><Position /></el-icon>
            布局视图
          </el-button>
        </el-button-group>

        <!-- 当前分类提示（布局视图下显示） -->
        <div v-if="viewMode === 'layout' && currentCategoryInfo" class="layout-category-badge">
          <el-tag :closable="false" size="small" type="primary">
            {{ currentCategoryInfo.name }}
          </el-tag>
          <span class="category-tip">此分类独立布局</span>
        </div>
      </div>
      <div class="toolbar-right">
        <template v-if="viewMode === 'layout' && layoutEditMode">
          <!-- 批量操作按钮 -->
          <template v-if="localSelectedIds.size > 0">
            <span class="selected-count">已选择 {{ localSelectedIds.size }} 个桌台</span>
            <el-button type="danger" @click="handleBatchDelete" :icon="Delete">批量删除</el-button>
            <el-button @click="clearSelection">取消选择</el-button>
          </template>
          <!-- 编辑模式按钮 -->
          <template v-else>
            <el-button @click="layoutEditMode = false" :icon="Close">取消编辑</el-button>
            <el-button @click="resetLayout" :icon="RefreshLeft">重置</el-button>
            <el-button type="primary" @click="saveLayout" :icon="Check">保存布局</el-button>
          </template>
        </template>
        <template v-else-if="viewMode === 'layout'">
          <el-button type="primary" @click="layoutEditMode = true" :icon="Edit">
            编辑布局
          </el-button>
        </template>
      </div>
    </div>

    <!-- 网格视图 -->
    <div v-show="viewMode === 'grid'" class="grid-view">
      <div v-if="safeTables.length === 0" class="empty-state">
        <el-empty description="暂无桌台数据，请先配置桌台" />
      </div>
      <div v-else class="table-grid">
        <div
          v-for="table in safeTables"
          :key="table.id"
          class="table-grid__item"
          :class="{ 'table-grid__item--selected': localSelectedIds.has(table.id) }"
        >
          <div class="table-card-wrapper">
            <!-- 选择器 -->
            <div
              v-if="selectionMode"
              class="table-card-selector"
              :class="{ 'table-card-selector--checked': localSelectedIds.has(table.id) }"
              @click.stop="handleTableSelect(table.id, !localSelectedIds.has(table.id))"
            >
              <el-icon v-if="localSelectedIds.has(table.id)" class="selector-icon">
                <Check />
              </el-icon>
            </div>
            <div class="table-card" :class="{ 'table-card--selected': localSelectedIds.has(table.id) }">
              <TableCard
                :table="table"
                :system-extend-time="extendTime"
                @start="handleStart"
                @pause="handlePause"
                @resume="handleResume"
                @end="handleEnd"
                @extend="handleExtend"
                @ignoreRemind="handleIgnoreRemind"
                @edit="handleEditTableName"
                @reserve="handleReserve"
                @cancelReservation="handleCancelReservation"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 布局视图 -->
    <div v-show="viewMode === 'layout'" class="layout-view">
      <!-- 提示选择分类 -->
      <div v-if="categoryId === 0" class="layout-category-tip">
        <el-icon :size="48" color="#909399">
          <InfoFilled />
        </el-icon>
        <div class="tip-content">
          <div class="tip-title">请选择具体分类</div>
          <div class="tip-desc">布局视图按分类独立管理，请选择上方分类标签查看对应布局</div>
        </div>
      </div>

      <!-- 布局画布 -->
      <div
        v-else
        ref="layoutCanvas"
        class="layout-canvas"
        :class="{ 'layout-canvas--edit': layoutEditMode }"
        @click="handleCanvasClick"
        @contextmenu.prevent
      >
        <!-- 网格背景（仅在编辑模式下显示） -->
        <div v-if="layoutEditMode" class="grid-background"></div>

        <!-- 桌台元素 -->
        <div
          v-for="table in layoutTables"
          :key="table.id"
          class="table-element"
          :class="{
            'table-element--selected': selectedTableId === table.id || selectedTableIds.has(table.id),
            'table-element--idle': table.status === 'idle',
            'table-element--using': table.status === 'using',
            'table-element--paused': table.status === 'paused',
            'table-element--editable': layoutEditMode
          }"
          :style="getTableStyle(table)"
          @mousedown.stop="layoutEditMode && handleTableMouseDown(table, $event)"
          @click.stop="handleTableClick(table)"
          @contextmenu.prevent.stop="layoutEditMode && showContextMenu(table, $event)"
        >
          <!-- 选择器（选择模式或编辑模式） -->
          <div
            v-if="selectionMode || layoutEditMode"
            class="table-element-selector"
            :class="{ 'table-element-selector--checked': localSelectedIds.has(table.id) }"
            @click.stop="handleTableSelect(table.id, !localSelectedIds.has(table.id))"
          >
            <el-icon v-if="localSelectedIds.has(table.id)" class="selector-icon">
              <Check />
            </el-icon>
          </div>

          <!-- 桌台内容 -->
          <div class="table-element__content">
            <!-- 桌台名称（可编辑） -->
            <div v-if="layoutEditMode && editingTableId === table.id" class="table-element__name-edit">
              <el-input
                v-model="editingTableName"
                size="small"
                maxlength="20"
                show-word-limit
                @blur="finishEditName(table)"
                @keyup.enter="finishEditName(table)"
                @click.stop
                ref="nameInputRef"
              />
            </div>
            <div v-else class="table-element__name" @dblclick="layoutEditMode && startEditName(table)">
              {{ table.name }}
            </div>
            <div class="table-element__status">
              <span v-if="table.status === 'idle'">空闲</span>
              <span v-else-if="table.status === 'using'" class="status-using">使用中</span>
              <span v-else class="status-paused">暂停</span>
            </div>
            <div v-if="table.status !== 'idle'" class="table-element__time">
              {{ formatDuration(table.duration) }}
              <!-- 显示延长倒计时或超时时长 -->
              <span v-if="table.presetDuration && table.duration > table.presetDuration" class="table-element__extend-info">
                <template v-if="table.duration <= table.presetDuration + extendTime * 60">
                  (+{{ formatDuration(table.presetDuration + extendTime * 60 - table.duration) }})
                </template>
                <template v-else>
                  <span class="table-element__overtime">+{{ formatDuration(table.duration - table.presetDuration - extendTime * 60) }}</span>
                </template>
              </span>
            </div>
            <div v-if="table.status !== 'idle'" class="table-element__amount">
              ¥{{ table.amount.toFixed(0) }}
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="table-element__actions">
            <el-button
              v-if="table.status === 'idle'"
              size="small"
              type="primary"
              @click.stop="handleStart(table)"
            >
              开始
            </el-button>
            <el-button
              v-if="table.status === 'using'"
              size="small"
              @click.stop="handlePause(table)"
            >
              暂停
            </el-button>
            <el-button
              v-if="table.status === 'paused'"
              size="small"
              type="warning"
              @click.stop="handleResume(table)"
            >
              继续
            </el-button>
            <el-button
              v-if="table.status !== 'idle'"
              size="small"
              type="success"
              @click.stop="handleEnd(table)"
            >
              结账
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右键菜单 -->
    <el-dropdown
      ref="contextMenu"
      v-model:visible="contextMenuVisible"
      :virtual-ref="contextMenuRef"
      trigger="contextmenu"
      @command="handleContextMenuCommand"
    >
      <span></span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="edit">编辑桌台</el-dropdown-item>
          <el-dropdown-item command="delete">删除桌台</el-dropdown-item>
          <el-dropdown-item command="duplicate">复制桌台</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 快速编辑桌台名称对话框 -->
    <el-dialog
      v-model="showEditNameDialog"
      title="编辑桌台名称"
      width="400px"
      @close="handleCloseEditDialog"
    >
      <el-form :model="editNameForm" label-width="80px">
        <el-form-item label="桌台编号">
          <el-input v-model="editNameForm.id" disabled />
        </el-form-item>
        <el-form-item label="桌台名称" prop="name">
          <el-input
            v-model="editNameForm.name"
            placeholder="请输入桌台名称"
            maxlength="20"
            show-word-limit
            ref="editNameInputRef"
            @keyup.enter="handleSaveName"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditNameDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveName" :loading="savingName">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Grid, Position, RefreshLeft, Check, Edit, Close, Delete, InfoFilled, WarningFilled } from '@element-plus/icons-vue'
import TableCard from './TableCard.vue'
import type { TableInfo } from '@/api/table'
import { getLayoutConfig, saveLayoutConfig, type TableLayoutItem } from '@/api/tableLayout'
import { deleteTable, batchDeleteTables, updateTable } from '@/api/table'
import { getSystemConfig, type SystemConfig } from '@/api/config'

interface LayoutTable extends TableInfo {
  x: number
  y: number
  width: number
  height: number
}

interface Props {
  tables: TableInfo[]
  categoryId: number
  selectionMode?: boolean
  selectedTableIds?: Set<number>
  categories?: { id: number; name: string; icon?: string }[]
}

const props = withDefaults(defineProps<Props>(), {
  tables: () => [],
  categoryId: 0,
  selectionMode: false,
  selectedTableIds: () => new Set(),
  categories: () => []
})

// 确保 tables 始终是一个数组
const safeTables = computed(() => {
  return Array.isArray(props.tables) ? props.tables : []
})

const emit = defineEmits<{
  start: [table: TableInfo]
  pause: [table: TableInfo]
  resume: [table: TableInfo]
  end: [table: TableInfo]
  extend: [table: TableInfo]
  ignoreRemind: [table: TableInfo]
  refresh: []
  edit: [table: TableInfo]
  select: [tableId: number, selected: boolean]
  batchDelete: []
  reserve: [table: TableInfo]
  cancelReservation: [table: TableInfo]
}>()

// 视图模式
const viewMode = ref<'grid' | 'layout'>('grid')

// 布局编辑模式
const layoutEditMode = ref(false)

// 布局数据
const layoutTables = ref<LayoutTable[]>([])
const selectedTableId = ref<number | null>(null)

// 使用传入的selectedTableIds
const localSelectedIds = ref<Set<number>>(new Set())

// 同步外部传入的selectedTableIds
watch(() => props.selectedTableIds, (newIds) => {
  localSelectedIds.value = new Set(newIds)
}, { deep: true })

// 当前分类信息
const currentCategoryInfo = computed(() => {
  if (props.categoryId === 0) return null
  return props.categories.find(c => c.id === props.categoryId)
})

// 编辑桌台名称
const editingTableId = ref<number | null>(null)
const editingTableName = ref('')
const nameInputRef = ref()

// 画布相关
const layoutCanvas = ref<HTMLElement>()
const contextMenuVisible = ref(false)
const contextMenuRef = ref()
const contextMenuTable = ref<TableInfo | null>(null)

// 拖拽状态
const isDragging = ref(false)
const draggedTable = ref<LayoutTable | null>(null)
const dragOffset = ref({ x: 0, y: 0 })

// 快速编辑桌台名称对话框
const showEditNameDialog = ref(false)
const editNameForm = ref({
  id: '',
  name: ''
})
const editNameInputRef = ref()
const savingName = ref(false)
const editingNameTable = ref<TableInfo | null>(null)

// 系统配置
const systemConfig = ref<SystemConfig | null>(null)
const extendTime = ref(30)  // 默认30分钟

// 加载系统配置
const loadSystemConfig = async () => {
  try {
    const configStr = await getSystemConfig()
    if (configStr) {
      const config = JSON.parse(configStr) as SystemConfig
      systemConfig.value = config
      if (config.extendTime) {
        extendTime.value = config.extendTime
      }
    }
  } catch (error) {
    console.error('加载系统配置失败:', error)
  }
}

// 从数据库加载布局配置
const loadLayout = async () => {
  // 如果没有桌台数据，清空布局
  if (!safeTables.value || safeTables.value.length === 0) {
    layoutTables.value = []
    return
  }

  try {
    const response = await getLayoutConfig(props.categoryId)
    if (response && response.config) {
      const layoutConfig: TableLayoutItem[] = JSON.parse(response.config)

      // 检查保存的布局配置是否与当前桌台列表匹配
      const savedIds = new Set(layoutConfig.map(l => l.id))
      const currentIds = new Set(safeTables.value.map(t => t.id))

      // 如果数量不同或ID不匹配，使用默认布局
      if (savedIds.size !== currentIds.size || ![...savedIds].every(id => currentIds.has(id))) {
        initDefaultLayout()
        return
      }

      // 合并桌台数据和布局数据
      layoutTables.value = safeTables.value.map(table => {
        const layout = layoutConfig.find(l => l.id === table.id)
        return {
          ...table,
          x: layout?.x || 0,
          y: layout?.y || 0,
          width: layout?.width || 120,
          height: layout?.height || 120
        }
      })
    } else {
      initDefaultLayout()
    }
  } catch (error) {
    console.error('加载布局配置失败:', error)
    initDefaultLayout()
  }
}

// 初始化默认布局（网格排列）
const initDefaultLayout = () => {
  const cols = Math.ceil(Math.sqrt(safeTables.value.length))
  const spacing = 140
  const padding = 20

  layoutTables.value = safeTables.value.map((table, index) => {
    const row = Math.floor(index / cols)
    const col = index % cols
    return {
      ...table,
      x: padding + col * spacing,
      y: padding + row * spacing,
      width: 120,
      height: 120
    }
  })
}

// 切换到布局视图
const handleSwitchToLayout = () => {
  viewMode.value = 'layout'
}

// 保存布局配置
const saveLayout = async () => {
  try {
    const layoutConfig: TableLayoutItem[] = layoutTables.value.map(table => ({
      id: table.id,
      x: table.x,
      y: table.y,
      width: table.width,
      height: table.height
    }))

    await saveLayoutConfig(props.categoryId, layoutConfig)
    ElMessage.success('布局保存成功')
    // 保存后退出编辑模式
    layoutEditMode.value = false
  } catch (error) {
    console.error('保存布局配置失败:', error)
    ElMessage.error('保存布局配置失败')
  }
}

// 重置布局
const resetLayout = async () => {
  try {
    await ElMessageBox.confirm('确认要重置布局吗？所有自定义位置将丢失。', '重置布局', {
      type: 'warning'
    })
    // 保存空的布局配置来重置
    await saveLayoutConfig(props.categoryId, [])
    initDefaultLayout()
    ElMessage.success('布局已重置')
  } catch {
    // 用户取消
  }
}

// 获取桌台样式
const getTableStyle = (table: LayoutTable) => ({
  left: `${table.x}px`,
  top: `${table.y}px`,
  width: `${table.width}px`,
  height: `${table.height}px`
})

// 格式化时长
const formatDuration = (seconds: number): string => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  const pad = (num: number) => num.toString().padStart(2, '0')
  return `${pad(hours)}:${pad(minutes)}:${pad(secs)}`
}

// 桌台点击处理
const handleTableClick = (table: LayoutTable) => {
  if (viewMode.value === 'layout') {
    selectedTableId.value = table.id
  }
}

// 画布点击处理
const handleCanvasClick = () => {
  selectedTableId.value = null
}

// 桌台鼠标按下（开始拖拽）
const handleTableMouseDown = (table: LayoutTable, event: MouseEvent) => {
  if (viewMode.value !== 'layout') return

  isDragging.value = true
  draggedTable.value = table

  // 计算鼠标点击位置相对于桌台左上角的偏移
  dragOffset.value = {
    x: event.clientX - table.x,
    y: event.clientY - table.y
  }

  // 添加全局鼠标事件监听
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)

  // 阻止默认行为
  event.preventDefault()
}

// 鼠标移动
const handleMouseMove = (event: MouseEvent) => {
  if (!isDragging.value || !draggedTable.value) return

  const canvas = layoutCanvas.value
  if (!canvas) return

  // 计算新位置（相对于画布）
  let newX = event.clientX - dragOffset.value.x
  let newY = event.clientY - dragOffset.value.y

  // 限制在画布范围内
  const maxX = canvas.offsetWidth - draggedTable.value.width
  const maxY = canvas.offsetHeight - draggedTable.value.height

  newX = Math.max(0, Math.min(newX, maxX))
  newY = Math.max(0, Math.min(newY, maxY))

  // 更新位置
  draggedTable.value.x = newX
  draggedTable.value.y = newY
}

// 鼠标松开
const handleMouseUp = () => {
  isDragging.value = false
  draggedTable.value = null

  // 移除全局鼠标事件监听
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
}

// 显示右键菜单
const showContextMenu = (table: LayoutTable, event: MouseEvent) => {
  selectedTableId.value = table.id
  contextMenuTable.value = table
  contextMenuVisible.value = true
}

// 右键菜单命令处理
const handleContextMenuCommand = (command: string) => {
  contextMenuVisible.value = false
  switch (command) {
    case 'edit':
      if (contextMenuTable.value) {
        emit('edit', contextMenuTable.value)
      }
      break
    case 'delete':
      if (contextMenuTable.value) {
        handleDeleteTable(contextMenuTable.value.id)
      }
      break
    case 'duplicate':
      ElMessage.info('复制功能开发中...')
      break
  }
}

// 桌台选择处理（批量选择）
const handleTableSelect = (tableId: number, checked: boolean) => {
  emit('select', tableId, checked)
}

// 清除选择
const clearSelection = () => {
  // 触发父组件清除选择
  for (const id of localSelectedIds.value) {
    emit('select', id, false)
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedTableIds.value.size === 0) {
    ElMessage.warning('请先选择要删除的桌台')
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
    selectedTableIds.value.clear()
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 删除单个桌台
const handleDeleteTable = async (tableId: number) => {
  try {
    await ElMessageBox.confirm('确认要删除该桌台吗？', '删除桌台', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    await deleteTable(tableId)
    ElMessage.success('删除成功')
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 开始编辑桌台名称
const startEditName = (table: LayoutTable) => {
  editingTableId.value = table.id
  editingTableName.value = table.name
  nextTick(() => {
    const input = nameInputRef.value?.[0] as HTMLInputElement
    input?.focus()
    input?.select()
  })
}

// 完成编辑桌台名称
const finishEditName = async (table: LayoutTable) => {
  if (!editingTableName.value.trim()) {
    ElMessage.warning('桌台名称不能为空')
    return
  }

  if (editingTableName.value.trim() === table.name) {
    editingTableId.value = null
    return
  }

  try {
    await updateTable(table.id, { name: editingTableName.value.trim() })
    ElMessage.success('修改成功')
    editingTableId.value = null
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')
  }
}

// 事件处理函数
const handleStart = (table: TableInfo) => emit('start', table)
const handlePause = (table: TableInfo) => emit('pause', table)
const handleResume = (table: TableInfo) => emit('resume', table)
const handleEnd = (table: TableInfo) => emit('end', table)
const handleExtend = (table: TableInfo) => emit('extend', table)
const handleIgnoreRemind = (table: TableInfo) => emit('ignoreRemind', table)
const handleReserve = (table: TableInfo) => emit('reserve', table)
const handleCancelReservation = (table: TableInfo) => emit('cancelReservation', table)

// 快速编辑桌台名称（来自TableCard）
const handleEditTableName = (table: TableInfo) => {
  // 只有空闲状态的桌台可以编辑名称
  if (table.status !== 'idle') {
    ElMessage.warning('只有空闲状态的桌台可以编辑名称')
    return
  }

  editingNameTable.value = table
  editNameForm.value = {
    id: `桌台${table.id}`,
    name: table.name
  }
  showEditNameDialog.value = true

  nextTick(() => {
    editNameInputRef.value?.focus()
    editNameInputRef.value?.select()
  })
}

// 关闭编辑对话框
const handleCloseEditDialog = () => {
  showEditNameDialog.value = false
  editNameForm.value = { id: '', name: '' }
  editingNameTable.value = null
}

// 保存桌台名称
const handleSaveName = async () => {
  if (!editingNameTable.value) return

  if (!editNameForm.value.name.trim()) {
    ElMessage.warning('桌台名称不能为空')
    return
  }

  if (editNameForm.value.name.trim() === editingNameTable.value.name) {
    showEditNameDialog.value = false
    return
  }

  try {
    savingName.value = true
    await updateTable(editingNameTable.value.id, { name: editNameForm.value.name.trim() })
    ElMessage.success('修改成功')
    showEditNameDialog.value = false
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')
  } finally {
    savingName.value = false
  }
}

// 检测桌台列表是否发生了变化（数量或ID）
const hasTableListChanged = (newTables: TableInfo[]): boolean => {
  // 如果 layoutTables 为空，说明需要初始化
  if (layoutTables.value.length === 0) return true
  if (newTables.length !== layoutTables.value.length) return true

  // 检查ID列表是否一致
  const currentIds = new Set(layoutTables.value.map(t => t.id))
  const newIds = new Set(newTables.map(t => t.id))

  if (currentIds.size !== newIds.size) return true

  for (const id of newIds) {
    if (!currentIds.has(id)) return true
  }

  return false
}

// 监听props.tables变化
watch(() => props.tables, (newTables) => {
  if (!newTables || newTables.length === 0) {
    layoutTables.value = []
    return
  }

  // 检测桌台列表是否发生了变化
  if (hasTableListChanged(newTables)) {
    // 切换到网格视图，让用户能立即看到所有桌台
    viewMode.value = 'grid'
    // 重新加载布局（为布局视图准备）
    loadLayout()
  } else {
    // 更新现有桌台的数据（状态、时长等）
    layoutTables.value = layoutTables.value.map(layoutTable => {
      const updatedTable = newTables.find(t => t.id === layoutTable.id)
      if (updatedTable) {
        return {
          ...layoutTable,
          ...updatedTable
        }
      }
      return layoutTable
    })
  }
}, { deep: true })

// 监听categoryId变化，重新加载布局
watch(() => props.categoryId, (newCategoryId) => {
  // 如果切换到"全部"分类，自动切换到网格视图
  if (newCategoryId === 0) {
    viewMode.value = 'grid'
  }
  loadLayout()
})

// 组件挂载时加载布局和系统配置
onMounted(() => {
  loadLayout()
  loadSystemConfig()
})
</script>

<style scoped>
.table-layout-editor {
  width: 100%;
  height: 100%;
}

/* 工具栏 */
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
  border-radius: 8px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.selected-count {
  margin-right: 12px;
  font-size: 14px;
  color: #606266;
}

/* 布局分类徽章 */
.layout-category-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
  padding-left: 16px;
  border-left: 1px solid #e4e7ed;
}

.category-tip {
  font-size: 12px;
  color: #909399;
}

/* 网格视图 */
.grid-view {
  padding: 10px;
}

.table-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.table-grid__item {
  animation: fadeIn 0.3s ease;
  transition: all 0.3s ease;
}

.table-grid__item--selected {
  transform: scale(0.98);
}

.table-card-wrapper {
  position: relative;
}

.table-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.table-card--selected {
  transform: scale(0.98);
}

.table-card--selected :deep(.table-card) {
  border-color: #409eff;
  box-shadow:
    0 0 0 2px rgba(64, 158, 255, 0.2),
    0 4px 20px rgba(64, 158, 255, 0.25);
}

.table-card--selected :deep(.table-card)::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0.03) 100%);
  pointer-events: none;
}

/* 选择器样式 */
.table-card-selector {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  border: 2px solid #dcdfe6;
  background: rgba(255, 255, 255, 0.95);
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(4px);
}

.table-card-selector:hover {
  border-color: #409eff;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.table-card-selector--checked {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.table-card-selector--checked:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
}

.table-card-selector .selector-icon {
  color: #fff;
  font-size: 14px;
  font-weight: bold;
  animation: checkIn 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes checkIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 布局视图 */
.layout-view {
  position: relative;
  height: calc(100vh - 200px);
  min-height: 600px;
}

/* 布局分类提示 */
.layout-category-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 8px;
  padding: 40px;
}

.tip-content {
  margin-top: 20px;
  text-align: center;
}

.tip-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.tip-desc {
  font-size: 14px;
  color: #909399;
  max-width: 300px;
}

.layout-canvas {
  position: relative;
  width: 100%;
  height: 100%;
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  cursor: default;
  transition: all 0.3s;
}

.layout-canvas--edit {
  background: #f5f7fa;
}

/* 网格背景 */
.grid-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(to right, rgba(0, 0, 0, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(0, 0, 0, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
}

/* 桌台元素 */
.table-element {
  position: absolute;
  cursor: default;
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  background: #fff;
  transition: all 0.2s;
  user-select: none;
}

.table-element__checkbox {
  position: absolute;
  top: 4px;
  left: 4px;
  z-index: 5;
  background: #fff;
  border-radius: 4px;
  padding: 2px;
}

/* 布局视图选择器 */
.table-element-selector {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 22px;
  height: 22px;
  border-radius: 5px;
  border: 2px solid #dcdfe6;
  background: rgba(255, 255, 255, 0.95);
  cursor: pointer;
  z-index: 15;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(4px);
}

.table-element-selector:hover {
  border-color: #409eff;
  transform: scale(1.08);
  box-shadow: 0 3px 10px rgba(64, 158, 255, 0.25);
}

.table-element-selector--checked {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-color: #409eff;
  box-shadow: 0 3px 10px rgba(64, 158, 255, 0.45);
}

.table-element-selector--checked:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
}

.table-element-selector .selector-icon {
  color: #fff;
  font-size: 13px;
  font-weight: bold;
  animation: checkIn 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.table-element--editable {
  cursor: move;
}

.table-element--editable:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: scale(1.02);
}

.table-element--selected {
  border-color: #409EFF;
  box-shadow:
    0 0 0 3px rgba(64, 158, 255, 0.25),
    0 4px 16px rgba(64, 158, 255, 0.35);
  z-index: 10;
  transform: scale(1.02);
}

/* 批量选择模式下的选中效果 */
.table-grid__item--selected .table-card,
.table-element--selected {
  position: relative;
}

.table-grid__item--selected .table-card::after,
.table-element--selected::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0.03) 100%);
  pointer-events: none;
}

.table-element--idle {
  border-color: #67C23A;
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f5e9 100%);
}

.table-element--using {
  border-color: #409EFF;
  background: linear-gradient(135deg, #e3f2fd 0%, #e8f4ff 100%);
}

.table-element--paused {
  border-color: #E6A23C;
  background: linear-gradient(135deg, #fff8e6 0%, #fef3e2 100%);
}

/* 桌台内容 */
.table-element__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px;
  height: 100%;
  text-align: center;
}

.table-element__name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.table-element--editable .table-element__name {
  cursor: pointer;
}

.table-element--editable .table-element__name:hover {
  color: #409EFF;
}

.table-element__name-edit {
  width: 100%;
  margin-bottom: 4px;
}

.table-element__status {
  font-size: 12px;
  margin-bottom: 4px;
}

.status-using {
  color: #409EFF;
}

.status-paused {
  color: #E6A23C;
}

.table-element__time {
  font-size: 12px;
  color: #606266;
  margin-bottom: 2px;
  font-family: 'Courier New', monospace;
}

.table-element__overtime {
  color: #F56C6C;
  font-weight: 600;
}

.table-element__extend-info {
  font-size: 11px;
  margin-left: 4px;
}

.table-element__amount {
  font-size: 14px;
  font-weight: bold;
  color: #F56C6C;
}

/* 桌台操作按钮 */
.table-element__actions {
  position: absolute;
  bottom: -35px;
  left: 50%;
  transform: translateX(-50%);
  display: none;
  gap: 4px;
  background: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  white-space: nowrap;
  z-index: 20;
}

.table-element--editable:hover .table-element__actions,
.table-element:hover .table-element__actions {
  display: flex;
}
</style>
