<template>
  <div
    class="table-card"
    :class="[
      `table-card--${table.status}`,
      { 'table-card--near-expiry': isNearExpiry && !remindIgnored },
      { 'table-card--overtime': isOvertime && !remindIgnored }
    ]"
    @click="$emit('click', table)"
  >
    <!-- 桌台头部 -->
    <div class="table-card__header">
      <div class="table-card__name-wrapper">
        <div class="table-card__name" @click.stop="$emit('edit', table)" :title="'点击编辑桌台名称'">
          {{ table.name }}
        </div>
        <el-icon class="table-card__edit-icon" @click.stop="$emit('edit', table)">
          <Edit />
        </el-icon>
      </div>
      <div class="table-card__status">
        <el-tag :type="statusType" size="small">{{ statusText }}</el-tag>
      </div>
    </div>

    <!-- 桌台内容 -->
    <div class="table-card__body">
      <!-- 空闲状态 -->
      <template v-if="table.status === 'idle'">
        <div class="table-card__idle">
          <el-icon :size="48" color="#67C23A">
            <CircleCheck />
          </el-icon>
          <div class="table-card__idle-text">空闲中</div>
        </div>
      </template>

      <!-- 使用中/暂停状态 -->
      <template v-else>
        <!-- 计时和费用 - 横向排列 -->
        <div class="table-card__timer-amount">
          <!-- 计时显示 -->
          <div class="table-card__timer">
            <div class="table-card__timer-icon">
              <el-icon :size="24">
                <Clock />
              </el-icon>
            </div>
            <div class="table-card__timer-value">{{ formattedDuration }}</div>
            <div class="table-card__timer-label">已用时长</div>
          </div>

          <!-- 费用显示 -->
          <div class="table-card__amount">
            <span class="table-card__amount-value">{{ formattedAmount }}</span>
            <span class="table-card__amount-label">当前费用</span>
          </div>
        </div>

        <!-- 预设时长信息 -->
        <div v-if="table.presetDuration" class="table-card__preset">
          <span>预设：{{ formatPresetDuration(table.presetDuration) }}</span>
          <span v-if="remainingDuration !== null" class="table-card__remaining">
            剩余：{{ formattedRemainingDuration }}
          </span>
        </div>

        <!-- 时间信息（开始时间、到点时间） -->
        <div v-if="table.status !== 'idle'" class="table-card__times">
          <div v-if="table.startTime" class="table-card__time-item" :title="formatFullDateTime(table.startTime)">
            <span class="table-card__time-label">开始</span>
            <span class="table-card__time-value">{{ formattedStartTime }}</span>
          </div>
          <div v-if="table.endTime" class="table-card__time-item" :title="formatFullDateTime(table.endTime)">
            <span class="table-card__time-label">到点</span>
            <span class="table-card__time-value">{{ formattedEndTime }}</span>
          </div>
        </div>

        <!-- 暂停信息和提醒状态 - 合并显示 -->
        <div class="table-card__status-info">
          <div v-if="table.status === 'paused'" class="table-card__pause-info">
            <el-tag type="warning" size="small">已暂停</el-tag>
            <span class="table-card__pause-duration">{{ formattedPauseDuration }}</span>
          </div>

          <div v-if="showRemindStatus" class="table-card__remind">
            <el-tag v-if="isNearExpiry" type="warning" size="small">
              即将到期
            </el-tag>
            <el-tag v-else-if="isOvertime" type="danger" size="small">
              已超时
            </el-tag>
          </div>
        </div>
      </template>
    </div>

    <!-- 桌台操作按钮 -->
    <div class="table-card__actions">
      <template v-if="table.status === 'idle'">
        <el-button type="primary" size="small" @click.stop="$emit('start', table)">
          开始计时
        </el-button>
      </template>
      <template v-else-if="table.status === 'using'">
        <el-button size="small" @click.stop="$emit('pause', table)">
          暂停
        </el-button>
        <el-button type="success" size="small" @click.stop="$emit('end', table)">
          结账
        </el-button>
      </template>
      <template v-else-if="table.status === 'paused'">
        <el-button type="primary" size="small" @click.stop="$emit('resume', table)">
          继续
        </el-button>
        <el-button type="success" size="small" @click.stop="$emit('end', table)">
          结账
        </el-button>
      </template>
    </div>

    <!-- 忽略提醒按钮 -->
    <div v-if="showRemindStatus && !remindIgnored" class="table-card__ignore">
      <el-button text size="small" @click.stop="$emit('ignoreRemind', table)">
        忽略提醒
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Clock, CircleCheck, Edit } from '@element-plus/icons-vue'
import type { TableInfo } from '@/api/table'

interface Props {
  table: TableInfo
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [table: TableInfo]
  start: [table: TableInfo]
  pause: [table: TableInfo]
  resume: [table: TableInfo]
  end: [table: TableInfo]
  ignoreRemind: [table: TableInfo]
  edit: [table: TableInfo]
}>()

// 状态类型
const statusType = computed(() => {
  const statusMap = {
    idle: 'success',
    using: 'primary',
    paused: 'warning'
  }
  return statusMap[props.table.status] || 'info'
})

// 状态文本
const statusText = computed(() => {
  const textMap = {
    idle: '空闲',
    using: '使用中',
    paused: '暂停'
  }
  return textMap[props.table.status] || '未知'
})

// 格式化时长为 HH:MM:SS
const formatDuration = (seconds: number): string => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  const pad = (num: number) => num.toString().padStart(2, '0')
  return `${pad(hours)}:${pad(minutes)}:${pad(secs)}`
}

// 格式化的时长显示
const formattedDuration = computed(() => {
  return formatDuration(props.table.duration || 0)
})

// 格式化暂停时长
const formattedPauseDuration = computed(() => {
  return formatDuration(props.table.pauseDuration || 0)
})

// 格式化的金额显示
const formattedAmount = computed(() => {
  return `¥${(props.table.amount || 0).toFixed(2)}`
})

// 格式化预设时长
const formatPresetDuration = (seconds: number): string => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  if (hours > 0 && minutes > 0) {
    return `${hours}小时${minutes}分`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${minutes}分钟`
  }
}

// 计算剩余时长（秒）
const remainingDuration = computed(() => {
  if (!props.table.presetDuration) {
    return null
  }
  const remaining = props.table.presetDuration - props.table.duration
  return Math.max(0, remaining)
})

// 格式化的剩余时长显示
const formattedRemainingDuration = computed(() => {
  if (remainingDuration.value === null) {
    return '不设时长'
  }
  return formatDuration(remainingDuration.value)
})

// 判断是否即将到期（剩余5分钟）
const isNearExpiry = computed(() => {
  if (remainingDuration.value === null) {
    return false
  }
  return remainingDuration.value <= 300 && remainingDuration.value > 0
})

// 判断是否已超时
const isOvertime = computed(() => {
  if (remainingDuration.value === null) {
    return false
  }
  return remainingDuration.value === 0
})

// 是否显示提醒状态
const showRemindStatus = computed(() => {
  return (isNearExpiry.value || isOvertime.value) && props.table.status !== 'idle'
})

// 提醒是否被忽略
const remindIgnored = computed(() => {
  return props.table.remindIgnored === 1
})

// 格式化开始时间
const formattedStartTime = computed(() => {
  if (!props.table.startTime) return ''
  return formatDateTime(props.table.startTime)
})

// 格式化到点时间
const formattedEndTime = computed(() => {
  if (!props.table.endTime) return ''
  return formatDateTime(props.table.endTime)
})

// 格式化日期时间
const formatDateTime = (timestamp: number): string => {
  const date = new Date(timestamp)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 格式化完整日期时间（用于提示）
const formatFullDateTime = (timestamp: number): string => {
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const seconds = date.getSeconds().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
</script>

<style scoped>
.table-card {
  position: relative;
  border-radius: 12px;
  border: 2px solid #e4e7ed;
  background: #ffffff;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  height: 280px;
  display: flex;
  flex-direction: column;
}

.table-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 状态颜色 */
.table-card--idle {
  border-color: #67C23A;
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f5e9 100%);
}

.table-card--using {
  border-color: #409EFF;
  background: linear-gradient(135deg, #e3f2fd 0%, #e8f4ff 100%);
}

.table-card--paused {
  border-color: #E6A23C;
  background: linear-gradient(135deg, #fff8e6 0%, #fef3e2 100%);
}

/* 即将到期 - 黄色闪烁 */
.table-card--near-expiry {
  animation: pulse-yellow 2s infinite;
  border-color: #E6A23C;
}

@keyframes pulse-yellow {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(230, 162, 60, 0.4);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(230, 162, 60, 0);
  }
}

/* 已超时 - 红色闪烁 */
.table-card--overtime {
  animation: pulse-red 1s infinite;
  border-color: #F56C6C;
}

@keyframes pulse-red {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(245, 108, 108, 0);
  }
}

/* 头部 */
.table-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}

.table-card__name-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.table-card__name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 4px;
  padding: 2px 6px;
  margin: -2px -6px;
}

.table-card__name:hover {
  background: rgba(64, 158, 255, 0.08);
  color: #409eff;
}

.table-card__edit-icon {
  color: #909399;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
  opacity: 0;
  transform: scale(0.9);
}

.table-card__header:hover .table-card__edit-icon {
  opacity: 1;
}

.table-card__edit-icon:hover {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  transform: scale(1);
}

/* 内容区域 */
.table-card__body {
  padding: 12px 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 空闲状态 */
.table-card__idle {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.table-card__idle-text {
  margin-top: 10px;
  font-size: 15px;
  color: #67C23A;
  font-weight: 500;
}

/* 计时和费用 - 横向排列 */
.table-card__timer-amount {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin-bottom: 8px;
  gap: 8px;
}

/* 计时显示 */
.table-card__timer {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.table-card__timer-icon {
  color: #409EFF;
  margin-bottom: 4px;
}

.table-card__timer-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}

.table-card__timer-label {
  font-size: 11px;
  color: #909399;
}

/* 费用显示 */
.table-card__amount {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.table-card__amount-value {
  font-size: 20px;
  font-weight: bold;
  color: #F56C6C;
}

.table-card__amount-label {
  font-size: 11px;
  color: #909399;
}

/* 预设时长 */
.table-card__preset {
  display: flex;
  justify-content: center;
  gap: 8px;
  font-size: 11px;
  color: #606266;
  background: rgba(0, 0, 0, 0.04);
  padding: 4px 8px;
  border-radius: 4px;
  margin-bottom: 6px;
}

.table-card__remaining {
  color: #E6A23C;
  font-weight: 500;
}

/* 时间信息 */
.table-card__times {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  margin-top: 6px;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 4px;
}

.table-card__time-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  cursor: help;
  transition: all 0.2s;
}

.table-card__time-item:hover {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.table-card__time-label {
  font-size: 10px;
  color: #909399;
  margin-bottom: 1px;
}

.table-card__time-value {
  font-size: 12px;
  font-weight: 500;
  color: #606266;
  font-family: 'Courier New', monospace;
}

/* 暂停和提醒信息 - 合并显示 */
.table-card__status-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
  min-height: 24px;
}

.table-card__pause-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.table-card__pause-duration {
  font-size: 11px;
  color: #E6A23C;
}

/* 提醒状态 */
.table-card__remind {
  display: flex;
}

.table-card__remind .el-tag {
  display: flex;
  align-items: center;
}

/* 操作按钮 */
.table-card__actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 10px 14px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(0, 0, 0, 0.02);
  flex-shrink: 0;
}

.table-card__actions .el-button {
  flex: 1;
}

/* 忽略提醒按钮 */
.table-card__ignore {
  position: absolute;
  bottom: 60px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
}
</style>
