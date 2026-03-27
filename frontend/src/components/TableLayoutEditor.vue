<template>
  <div class="table-layout-editor">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-left">
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
      </div>
      <div class="toolbar-right">
        <template v-if="viewMode === 'layout'">
          <el-button
            v-if="!layoutEditMode"
            type="primary"
            @click="layoutEditMode = true"
            :icon="Edit"
          >
            编辑布局
          </el-button>
          <template v-else>
            <el-button @click="layoutEditMode = false" :icon="Close">取消编辑</el-button>
            <el-button @click="resetLayout" :icon="RefreshLeft">重置</el-button>
            <el-button type="primary" @click="saveLayout" :icon="Check">保存布局</el-button>
          </template>
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
        >
          <TableCard
            :table="table"
            @start="handleStart"
            @pause="handlePause"
            @resume="handleResume"
            @end="handleEnd"
            @ignoreRemind="handleIgnoreRemind"
          />
        </div>
      </div>
    </div>

    <!-- 布局视图 -->
    <div v-show="viewMode === 'layout'" class="layout-view">
      <div
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
            'table-element--selected': selectedTableId === table.id,
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
          <!-- 桌台内容 -->
          <div class="table-element__content">
            <div class="table-element__name">{{ table.name }}</div>
            <div class="table-element__status">
              <span v-if="table.status === 'idle'">空闲</span>
              <span v-else-if="table.status === 'using'" class="status-using">使用中</span>
              <span v-else class="status-paused">暂停</span>
            </div>
            <div v-if="table.status !== 'idle'" class="table-element__time">
              {{ formatDuration(table.duration) }}
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Grid, Position, RefreshLeft, Check, Edit, Close } from '@element-plus/icons-vue'
import TableCard from './TableCard.vue'
import type { TableInfo } from '@/api/table'
import { getTableList } from '@/api/table'

interface LayoutTable extends TableInfo {
  x: number
  y: number
  width: number
  height: number
}

interface Props {
  tables: TableInfo[]
}

const props = withDefaults(defineProps<Props>(), {
  tables: () => []
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
  ignoreRemind: [table: TableInfo]
  refresh: []
}>()

// 视图模式
const viewMode = ref<'grid' | 'layout'>('grid')

// 布局编辑模式
const layoutEditMode = ref(false)

// 布局数据
const layoutTables = ref<LayoutTable[]>([])
const selectedTableId = ref<number | null>(null)

// 画布相关
const layoutCanvas = ref<HTMLElement>()
const contextMenuVisible = ref(false)
const contextMenuRef = ref()

// 拖拽状态
const isDragging = ref(false)
const draggedTable = ref<LayoutTable | null>(null)
const dragOffset = ref({ x: 0, y: 0 })

// 从localStorage加载布局配置
const loadLayout = () => {
  // 如果没有桌台数据，清空布局
  if (!safeTables.value || safeTables.value.length === 0) {
    layoutTables.value = []
    return
  }

  const savedLayout = localStorage.getItem('table_layout_config')
  if (savedLayout) {
    try {
      const layoutConfig = JSON.parse(savedLayout)

      // 检查保存的布局配置是否与当前桌台列表匹配
      const savedIds = new Set(layoutConfig.map((l: LayoutTable) => l.id))
      const currentIds = new Set(safeTables.value.map(t => t.id))

      // 如果数量不同或ID不匹配，清除旧配置并使用默认布局
      if (savedIds.size !== currentIds.size || ![...savedIds].every(id => currentIds.has(id))) {
        localStorage.removeItem('table_layout_config')
        initDefaultLayout()
        return
      }

      // 合并桌台数据和布局数据
      layoutTables.value = safeTables.value.map(table => {
        const layout = layoutConfig.find((l: LayoutTable) => l.id === table.id)
        return {
          ...table,
          x: layout?.x || 0,
          y: layout?.y || 0,
          width: 120,
          height: 120
        }
      })
    } catch (error) {
      console.error('加载布局配置失败:', error)
      initDefaultLayout()
    }
  } else {
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

// 保存布局配置
const saveLayout = () => {
  const layoutConfig = layoutTables.value.map(table => ({
    id: table.id,
    x: table.x,
    y: table.y,
    width: table.width,
    height: table.height
  }))
  localStorage.setItem('table_layout_config', JSON.stringify(layoutConfig))
  ElMessage.success('布局保存成功')
  // 保存后退出编辑模式
  layoutEditMode.value = false
}

// 重置布局
const resetLayout = async () => {
  try {
    await ElMessageBox.confirm('确认要重置布局吗？所有自定义位置将丢失。', '重置布局', {
      type: 'warning'
    })
    localStorage.removeItem('table_layout_config')
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
  contextMenuVisible.value = true
}

// 右键菜单命令处理
const handleContextMenuCommand = (command: string) => {
  contextMenuVisible.value = false
  switch (command) {
    case 'edit':
      ElMessage.info('编辑功能开发中...')
      break
    case 'delete':
      ElMessage.info('删除功能开发中...')
      break
    case 'duplicate':
      ElMessage.info('复制功能开发中...')
      break
  }
}

// 事件处理函数
const handleStart = (table: TableInfo) => emit('start', table)
const handlePause = (table: TableInfo) => emit('pause', table)
const handleResume = (table: TableInfo) => emit('resume', table)
const handleEnd = (table: TableInfo) => emit('end', table)
const handleIgnoreRemind = (table: TableInfo) => emit('ignoreRemind', table)

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
    // 清除旧的布局配置
    localStorage.removeItem('table_layout_config')
    // 切换到网格视图，让用户能立即看到所有桌台
    viewMode.value = 'grid'
    // 重新初始化默认布局（为布局视图准备）
    initDefaultLayout()
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

// 组件挂载时加载布局
onMounted(() => {
  loadLayout()
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

.table-element--editable {
  cursor: move;
}

.table-element--editable:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: scale(1.02);
}

.table-element--selected {
  border-color: #409EFF;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.3);
  z-index: 10;
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
