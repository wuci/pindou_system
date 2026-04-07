<template>
  <div class="remind-panel-container">
    <!-- 跑马灯提醒条 -->
    <div v-if="marqueeReminds.length > 0" class="marquee-bar">
      <div class="marquee-content">
        <el-icon class="bell-icon"><Bell /></el-icon>
        <div class="marquee-wrapper">
          <div class="marquee-text" :class="{ animating: marqueeReminds.length > 0 }" :style="{ animationDuration: marqueeAnimationDuration + 's' }">
            <span v-for="(remind, index) in displayReminds" :key="index" class="marquee-item">
              【{{ remind.remindTypeDesc }}】{{ remind.tableName }} - 超时{{ formatDuration(remind.overtimeDuration) }}
            </span>
          </div>
        </div>
      </div>
      <el-icon class="close-icon" @click="closeMarquee"><Close /></el-icon>
    </div>

    <!-- 下拉提醒框 -->
    <transition name="dropdown">
      <div v-if="isOpen" class="remind-dropdown">
        <div class="dropdown-header">
          <span>提醒通知</span>
          <el-badge :value="remindList.length" :max="99" type="danger" />
        </div>

        <div class="dropdown-content">
          <div v-if="remindList.length === 0" class="empty-state">
            <el-icon><Bell /></el-icon>
            <p>暂无提醒</p>
          </div>

          <div v-else class="remind-list">
            <div
              v-for="remind in remindList"
              :key="remind.tableId"
              class="remind-item"
              :class="remind.remindType"
              @click="handleIgnore(remind)"
            >
              <div class="item-left">
                <el-icon class="status-icon">
                  <Warning v-if="remind.remindType === 'expiring'" />
                  <WarningFilled v-else />
                </el-icon>
                <div class="item-info">
                  <span class="table-name">{{ remind.tableName }}</span>
                  <span class="remind-desc">{{ remind.remindTypeDesc }}</span>
                </div>
              </div>
              <div class="item-right">
                <span class="time-text">超时{{ formatDuration(remind.overtimeDuration) }}</span>
                <el-icon class="close-btn"><Close /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <div v-if="remindList.length > 0" class="dropdown-footer">
          <el-button size="small" text @click="clearAll">全部已阅</el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Close, Warning, WarningFilled } from '@element-plus/icons-vue'
import { getReminders, ignoreRemind, type RemindInfo } from '@/api/remind'
import { useWebSocketStore } from '@/stores/websocket'
import type { RemindData } from '@/utils/websocket'

const props = defineProps<{
  soundEnabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:count', count: number): void
  (e: 'remind', reminds: RemindInfo[]): void
}>()

// 数据
const isOpen = ref(false)
const remindList = ref<RemindInfo[]>([])
const marqueeReminds = ref<RemindInfo[]>([])
const isInitialLoad = ref(true) // 标记是否是首次加载
const existingTableIds = ref<Set<number>>(new Set()) // 记录已处理的桌台ID

// WebSocket store
const webSocketStore = useWebSocketStore()

let refreshTimer: number | null = null
let unsubscribeRemind: (() => void) | null = null
let useWebSocket = ref(false)

// 用于跑马灯显示的提醒列表
const displayReminds = computed(() => {
  return marqueeReminds.value
})

// 根据内容数量动态计算跑马灯动画时长（每个提醒约5秒）
const marqueeAnimationDuration = computed(() => {
  const count = marqueeReminds.value.length
  if (count === 0) return 0
  // 基础时长 + 每个提醒5秒
  return Math.max(30, count * 5)
})

// 打开面板
const open = () => {
  isOpen.value = true
}

// 关闭面板
const close = () => {
  isOpen.value = false
}

// 切换面板
const toggle = () => {
  isOpen.value = !isOpen.value
}

// 关闭跑马灯
const closeMarquee = () => {
  marqueeReminds.value = []
}

// 加载提醒列表
const loadReminders = async () => {
  try {
    const data = await getReminders()

    if (isInitialLoad.value) {
      // 首次加载，直接使用服务器数据
      remindList.value = data
      data.forEach(r => existingTableIds.value.add(r.tableId))
      isInitialLoad.value = false
    } else {
      // 后续刷新，只添加新的提醒
      const newReminders = data.filter(r => !existingTableIds.value.has(r.tableId))
      if (newReminders.length > 0) {
        remindList.value = [...remindList.value, ...newReminders]
        newReminders.forEach(r => existingTableIds.value.add(r.tableId))

        // 新提醒时播放提示音
        if (props.soundEnabled) {
          playSound()
        }
      }
    }

    // 更新未读数量
    emit('update:count', remindList.value.length)

    // 更新跑马灯数据（只显示超时的）
    marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout')

    // 通知父组件
    emit('remind', remindList.value)
  } catch (error) {
  }
}

// 处理 WebSocket 提醒消息
const handleWebSocketRemind = (data: RemindData) => {
  // 检查是否已在列表中
  const exists = remindList.value.some(r => r.tableId === data.tableId && r.remindType === data.remindType)
  if (!exists) {
    // 添加到列表
    remindList.value = [...remindList.value, data as RemindInfo]

    // 更新未读数量
    emit('update:count', remindList.value.length)

    // 更新跑马灯数据（超时优先）
    if (data.remindType === 'timeout') {
      marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout').slice(0, 3)
    }

    // 播放提示音
    if (props.soundEnabled) {
      playSound()
    }

    // 通知父组件
    emit('remind', remindList.value)

    // 自动打开面板
    if (!isOpen.value) {
      open()
    }
  }
}

// 忽略提醒
const handleIgnore = async (remind: RemindInfo) => {
  try {
    await ignoreRemind(remind.tableId)

    // 从列表中移除
    const index = remindList.value.findIndex(r => r.tableId === remind.tableId)
    if (index > -1) {
      remindList.value.splice(index, 1)
    }

    // 从已处理集合中移除，允许再次提醒
    existingTableIds.value.delete(remind.tableId)

    // 更新跑马灯数据
    marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout').slice(0, 3)

    // 更新未读数量
    emit('update:count', remindList.value.length)

    ElMessage.success(`已忽略 ${remind.tableName} 的提醒`)

    // 如果没有提醒了，关闭面板
    if (remindList.value.length === 0) {
      close()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 全部已阅
const clearAll = () => {
  remindList.value = []
  marqueeReminds.value = []
  existingTableIds.value.clear()
  emit('update:count', 0)
  close()
}

// 播放提示音（使用 Web Audio API）
const playSound = () => {
  if (!props.soundEnabled) return

  try {
    const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()

    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)

    // 设置音调（叮咚声）
    oscillator.frequency.setValueAtTime(800, audioContext.currentTime)
    oscillator.frequency.exponentialRampToValueAtTime(600, audioContext.currentTime + 0.1)

    // 设置音量包络
    gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3)

    // 播放
    oscillator.start(audioContext.currentTime)
    oscillator.stop(audioContext.currentTime + 0.3)
  } catch (e) {
  }
}

// 格式化时长
const formatDuration = (seconds: number) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  if (hours > 0) return `${hours}小时${minutes}分`
  if (minutes > 0) return `${minutes}分钟`
  return `${seconds}秒`
}

// 定时刷新
const startRefresh = () => {
  if (useWebSocket.value) return

  refreshTimer = window.setInterval(() => {
    loadReminders()
  }, 5000)
}

const stopRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 初始化 WebSocket
const initWebSocket = () => {
  if (webSocketStore.isInitialized && webSocketStore.isConnected) {
    unsubscribeRemind = webSocketStore.onRemind(handleWebSocketRemind)
    useWebSocket.value = true
  }
}

const cleanupWebSocket = () => {
  if (unsubscribeRemind) {
    unsubscribeRemind()
    unsubscribeRemind = null
  }
}

// 暴露方法供父组件调用
defineExpose({
  open,
  close,
  toggle,
  loadReminders,
  stopRefresh
})

// 监听 WebSocket 连接状态变化
watch(() => webSocketStore.isConnected, (connected) => {
  if (connected && !unsubscribeRemind) {
    initWebSocket()
  }
})

// 生命周期
onMounted(() => {
  loadReminders()
  initWebSocket()
  if (!useWebSocket.value) {
    startRefresh()
  }
})

onUnmounted(() => {
  stopRefresh()
  cleanupWebSocket()
})
</script>

<script lang="ts">
export default {
  name: 'RemindPanel'
}
</script>

<style scoped>
.remind-panel-container {
  position: relative;
}

/* 跑马灯样式 */
.marquee-bar {
  position: fixed;
  top: 60px;
  left: 200px;
  right: 0;
  height: 36px;
  background: linear-gradient(90deg, #fff4e5 0%, #ffecd9 100%);
  border-bottom: 1px solid #ffe4c4;
  display: flex;
  align-items: center;
  padding: 0 16px;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.2);
}

.marquee-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
}

.bell-icon {
  color: #e6a23c;
  font-size: 18px;
  flex-shrink: 0;
  animation: ring 1s ease-in-out infinite;
}

@keyframes ring {
  0%, 100% { transform: rotate(0deg); }
  10%, 30% { transform: rotate(-10deg); }
  20%, 40% { transform: rotate(10deg); }
}

.marquee-wrapper {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
}

.marquee-text {
  display: inline-block;
  white-space: nowrap;
}

.marquee-text.animating {
  animation: marquee 60s linear infinite;
}

@keyframes marquee {
  0% { transform: translateX(100%); }
  100% { transform: translateX(-100%); }
}

.marquee-item {
  display: inline-block;
  margin-right: 60px;
  color: #e6a23c;
  font-size: 14px;
  font-weight: 500;
}

.close-icon {
  cursor: pointer;
  color: #909399;
  transition: color 0.3s;
  flex-shrink: 0;
}

.close-icon:hover {
  color: #606266;
}

/* 下拉框样式 */
.remind-dropdown {
  position: fixed;
  top: 60px;
  right: 20px;
  width: 320px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  overflow: hidden;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.dropdown-content {
  max-height: 300px;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 20px;
  color: #909399;
}

.empty-state .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.empty-state p {
  margin: 0;
  font-size: 13px;
}

.remind-list {
  padding: 8px;
}

.remind-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fafafa;
}

.remind-item:hover {
  background: #f0f0f0;
}

.remind-item.expiring {
  border-left: 3px solid #e6a23c;
  background: linear-gradient(to right, rgba(230, 162, 60, 0.05), #fafafa);
}

.remind-item.timeout {
  border-left: 3px solid #f56c6c;
  background: linear-gradient(to right, rgba(245, 108, 108, 0.05), #fafafa);
  animation: pulse-bg 2s infinite;
}

@keyframes pulse-bg {
  0%, 100% { background: linear-gradient(to right, rgba(245, 108, 108, 0.05), #fafafa); }
  50% { background: linear-gradient(to right, rgba(245, 108, 108, 0.1), #fafafa); }
}

.item-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.status-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.remind-item.expiring .status-icon {
  color: #e6a23c;
}

.remind-item.timeout .status-icon {
  color: #f56c6c;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.table-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.remind-desc {
  font-size: 12px;
  color: #909399;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.time-text {
  font-size: 13px;
  color: #606266;
}

.close-btn {
  font-size: 14px;
  color: #c0c4cc;
  transition: color 0.2s;
}

.remind-item:hover .close-btn {
  color: #909399;
}

.dropdown-footer {
  padding: 8px 16px;
  border-top: 1px solid #ebeef5;
  text-align: center;
  background: #fafafa;
}

.dropdown-footer .el-button {
  font-size: 13px;
}
</style>
