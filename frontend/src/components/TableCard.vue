<template>
  <div
    class="table-card"
    :class="[
      `table-card--${table.status}`,
      { 'table-card--near-expiry': isNearExpiry && !remindIgnored && !isOvertime },
      { 'table-card--overtime': isOverExtendedTime }
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
        <!-- 预定按钮（放在名称右边，使用图标） -->
        <template v-if="table.status === 'idle' && permissions.canReserve">
          <el-tooltip
            v-if="table.reservationStatus === 'reserved'"
            content="取消预定"
            placement="top"
          >
            <el-icon
              class="table-card__reserve-icon reserved"
              @click.stop="$emit('cancelReservation', table)"
            >
              <Calendar />
            </el-icon>
          </el-tooltip>
          <el-tooltip
            v-else
            content="预定"
            placement="top"
          >
            <el-icon
              class="table-card__reserve-icon"
              @click.stop="$emit('reserve', table)"
            >
              <Calendar />
            </el-icon>
          </el-tooltip>
        </template>
      </div>
      <div class="table-card__status">
        <el-tag :type="statusType" size="small">{{ statusText }}</el-tag>
      </div>
    </div>

    <!-- 桌台内容 -->
    <div class="table-card__body">
      <!-- 空闲状态 -->
      <template v-if="table.status === 'idle'">
        <!-- 已预定状态 -->
        <template v-if="table.reservationStatus === 'reserved'">
          <div class="table-card__reserved">
            <div class="table-card__reserved-info">
              <div class="table-card__reserved-header">
                <el-icon :size="28" color="#E6A23C">
                  <Clock />
                </el-icon>
                <span class="table-card__reserved-label">已预定</span>
              </div>
              <div class="table-card__reserved-details">
                <div class="table-card__reserved-item">
                  <span class="table-card__reserved-item-label">预订人：</span>
                  <span class="table-card__reserved-item-value">{{ table.reservationName }}</span>
                </div>
                <div class="table-card__reserved-item">
                  <span class="table-card__reserved-item-label">手机号：</span>
                  <span class="table-card__reserved-item-value">{{ table.reservationPhone }}</span>
                </div>
                <div class="table-card__reserved-item" v-if="table.reservationEndTime">
                  <span class="table-card__reserved-item-label">截止时间：</span>
                  <span class="table-card__reserved-item-value">{{ formattedReservationEndTime }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>
        <!-- 纯空闲状态 -->
        <template v-else>
          <div class="table-card__idle">
            <!-- 温柔岛屿图标 -->
            <svg class="idle-icon" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
              <!-- 岛屿 -->
              <ellipse cx="24" cy="28" rx="14" ry="10" fill="url(#idleIslandGradient)" opacity="0.9"/>
              <!-- 光点 -->
              <circle cx="20" cy="28" r="2" fill="#fff" opacity="0.9"/>
              <circle cx="28" cy="26" r="2.5" fill="#fff" opacity="0.95"/>
              <circle cx="24" cy="32" r="1.5" fill="#fff" opacity="0.85"/>
              <!-- 连接 -->
              <line x1="20" y1="28" x2="28" y2="26" stroke="#fff" stroke-width="0.8" opacity="0.4"/>
              <line x1="20" y1="28" x2="24" y2="32" stroke="#fff" stroke-width="0.8" opacity="0.3"/>
              <!-- 水波 -->
              <path d="M12 38 Q18 36 24 38 Q30 40 36 38" fill="none" stroke="#98d4bb" stroke-width="1.5" opacity="0.4"/>
              <defs>
                <linearGradient id="idleIslandGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#b8e8d8"/>
                  <stop offset="100%" stop-color="#98d4bb"/>
                </linearGradient>
              </defs>
            </svg>
            <div class="table-card__idle-text">空闲中</div>
          </div>
        </template>
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
            <div class="table-card__timer-value">{{ formattedRemainingDuration }}</div>
            <div class="table-card__timer-label">{{ isInExtendTime ? '延长剩余' : '剩余时长' }}</div>
          </div>

          <!-- 费用显示 -->
          <div class="table-card__amount">
            <div v-if="table.originalAmount && table.originalAmount > table.amount" class="table-card__amount-with-original">
              <span class="table-card__amount-original">¥{{ (table.originalAmount || 0).toFixed(2) }}</span>
              <span class="table-card__amount-value">¥{{ (table.amount || 0).toFixed(2) }}</span>
            </div>
            <div v-else class="table-card__amount-single">
              <span class="table-card__amount-value">¥{{ (table.amount || 0).toFixed(2) }}</span>
            </div>
            <span class="table-card__amount-label">当前费用</span>
            <span v-if="table.memberDiscountRate" class="table-card__member-discount">
              {{ (table.memberDiscountRate * 10).toFixed(1) }}折
            </span>
          </div>
        </div>

        <!-- 预设时长信息 -->
        <div v-if="table.presetDuration" class="table-card__preset">
          <!-- 显示已用时长 -->
          <span class="table-card__remaining">
            已用：{{ formattedDuration }}
          </span>
          <span>预设：{{ formatPresetDuration(table.presetDuration) }}</span>
          <!-- 延长配置：始终显示 -->
          <span class="table-card__extended">
            延长：{{ formattedExtendedDuration }}
          </span>
          <!-- 超过延长配置时显示真正超时时长 -->
          <span v-if="isOverExtendedTime" :class="['table-card__overtime']">
            超时：{{ formattedOvertimeDuration }}
          </span>
          <!-- 在延长倒计时时显示剩余延长时长 -->
          <span v-else-if="isInExtendTime" class="table-card__extend-countdown">
            延长剩余：{{ formattedExtendRemaining }}
          </span>
        </div>

        <!-- 不限时时的延长时长显示 -->
        <div v-else class="table-card__preset">
          <span class="table-card__remaining">
            已用：{{ formattedDuration }}
          </span>
          <span class="table-card__extended">
            延长：{{ formattedExtendedDuration }}
          </span>
        </div>

        <!-- 时间信息（开始时间、到点时间） -->
        <div v-if="table.status === 'using' || table.status === 'paused'" class="table-card__times">
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

          <div v-if="showRemindStatus && !isOvertime" class="table-card__remind">
            <el-tag v-if="isNearExpiry" type="warning" size="small">
              即将到期
            </el-tag>
          </div>
        </div>
      </template>
    </div>

    <!-- 桌台操作按钮 -->
    <div class="table-card__actions">
      <template v-if="table.status === 'idle'">
        <!-- 已预订状态不显示开始计时按钮 -->
        <el-button v-if="table.reservationStatus !== 'reserved' && permissions.canStart" type="primary" size="small" @click.stop="$emit('start', table)">
          开始计时
        </el-button>
      </template>
      <template v-else-if="table.status === 'using'">
        <el-button v-if="permissions.canPause" size="small" @click.stop="$emit('pause', table)">
          暂停
        </el-button>
        <el-button v-if="canShowExtend" type="warning" size="small" @click.stop="$emit('extend', table)">
          续费
        </el-button>
        <el-button v-if="permissions.canBill" type="success" size="small" @click.stop="$emit('end', table)">
          结账
        </el-button>
      </template>
      <template v-else-if="table.status === 'paused'">
        <el-button v-if="permissions.canPause" type="primary" size="small" @click.stop="$emit('resume', table)">
          继续
        </el-button>
        <el-button v-if="canShowExtend" type="warning" size="small" @click.stop="$emit('extend', table)">
          续费
        </el-button>
        <el-button v-if="permissions.canBill" type="success" size="small" @click.stop="$emit('end', table)">
          结账
        </el-button>
      </template>
    </div>

    <!-- 忽略提醒按钮 -->
    <div v-if="showRemindStatus && !remindIgnored && !isOvertime" class="table-card__ignore">
      <el-button text size="small" @click.stop="$emit('ignoreRemind', table)">
        忽略提醒
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Clock, Edit, Calendar } from '@element-plus/icons-vue'
import type { TableInfo } from '@/api/table'
import { useUserStore } from '@/stores/user'

// 用户状态和权限
const userStore = useUserStore()

// 判断是否为不限时套餐（presetDuration 为 null、0 或 undefined 表示不限时）
const isUnlimited = computed(() => {
  return props.table.presetDuration === null || props.table.presetDuration === 0 || props.table.presetDuration === undefined
})

// 是否有续费权限
const hasExtendPermission = computed(() => {
  return userStore.hasPermission('table:extend')
})

const permissions = computed(() => ({
  canStart: userStore.hasPermission('table:start'),
  canPause: userStore.hasPermission('table:pause'),
  canEnd: userStore.hasPermission('table:end'),
  canExtend: hasExtendPermission.value,
  canReserve: userStore.hasPermission('table:reserve'),
  canBill: userStore.hasPermission('table:bill')
}))

// 是否允许续费（有权限且不是不限时套餐）
const canShowExtend = computed(() => {
  return hasExtendPermission.value && !isUnlimited.value
})

interface Props {
  table: TableInfo
  systemExtendTime?: number  // 系统配置的延长时间（分钟）
}

const props = withDefaults(defineProps<Props>(), {
  systemExtendTime: 30  // 默认30分钟
})

defineEmits<{
  click: [table: TableInfo]
  start: [table: TableInfo]
  pause: [table: TableInfo]
  resume: [table: TableInfo]
  end: [table: TableInfo]
  ignoreRemind: [table: TableInfo]
  edit: [table: TableInfo]
  extend: [table: TableInfo]
  reserve: [table: TableInfo]
  cancelReservation: [table: TableInfo]
}>()

// 状态类型
const statusType = computed(() => {
  const statusMap: Record<string, 'success' | 'primary' | 'info' | 'warning' | 'danger' | undefined> = {
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

// 计算延长时长配置（秒）- 系统配置的延长时间
const extendedDuration = computed(() => {
  // 系统配置的延长时间（分钟转换为秒）
  const configExtendTimeSeconds = props.systemExtendTime * 60
  return configExtendTimeSeconds
})

// 判断是否在延长时间内（超过预设但未超过延长配置）
const isInExtendTime = computed(() => {
  if (!props.table.presetDuration) {
    return false
  }
  const totalAllowedTime = props.table.presetDuration + extendedDuration.value
  return props.table.duration > props.table.presetDuration && props.table.duration <= totalAllowedTime
})

// 计算延长倒计时（延长时间剩余多少秒）
const extendRemaining = computed(() => {
  if (!props.table.presetDuration) {
    return 0
  }
  const totalAllowedTime = props.table.presetDuration + extendedDuration.value
  const remaining = totalAllowedTime - props.table.duration
  return Math.max(0, remaining)
})

// 格式化的剩余时长显示（在延长时间内时显示延长剩余时长）
const formattedRemainingDuration = computed(() => {
  // 如果在延长时间内，显示延长剩余时长
  if (isInExtendTime.value) {
    return formatDuration(extendRemaining.value)
  }
  // 否则显示正常的剩余时长
  if (remainingDuration.value === null) {
    return '不设时长'
  }
  return formatDuration(remainingDuration.value)
})

// 格式化的延长时长显示
const formattedExtendedDuration = computed(() => {
  return formatDuration(extendedDuration.value)
})

// 判断是否即将到期（剩余5分钟）
const isNearExpiry = computed(() => {
  if (remainingDuration.value === null) {
    return false
  }
  return remainingDuration.value <= 300 && remainingDuration.value > 0
})

// 判断是否已超时（超过预设时长）
const isOvertime = computed(() => {
  if (!props.table.presetDuration) {
    return false
  }
  return props.table.duration > props.table.presetDuration
})

// 判断是否超过延长配置时间
const isOverExtendedTime = computed(() => {
  if (!props.table.presetDuration) {
    return false
  }
  const totalAllowedTime = props.table.presetDuration + extendedDuration.value
  return props.table.duration > totalAllowedTime
})

// 格式化的延长倒计时显示
const formattedExtendRemaining = computed(() => {
  return formatDuration(extendRemaining.value)
})

// 计算真正超时时长（超过预设+延长的部分）
const overtimeDuration = computed(() => {
  if (!props.table.presetDuration) {
    return 0
  }
  const totalAllowedTime = props.table.presetDuration + extendedDuration.value
  if (props.table.duration <= totalAllowedTime) {
    return 0
  }
  // 返回超过延长配置的时长
  return props.table.duration - totalAllowedTime
})

// 格式化的超时时长显示
const formattedOvertimeDuration = computed(() => {
  return formatDuration(overtimeDuration.value)
})

// 是否显示提醒状态
const showRemindStatus = computed(() => {
  return isNearExpiry.value && !isOvertime.value && props.table.status !== 'idle'
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

// 格式化预定截止时间
const formattedReservationEndTime = computed(() => {
  if (!props.table.reservationEndTime) return ''
  return formatDateTime(props.table.reservationEndTime)
})
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
  border-color: #98d4bb;
  background: linear-gradient(135deg, #f0fdfa 0%, #e8f5e9 100%);
}

.table-card--using {
  border-color: #d4a5ff;
  background: linear-gradient(135deg, #f3e7ff 0%, #ffeef8 100%);
}

.table-card--paused {
  border-color: #ffb6d9;
  background: linear-gradient(135deg, #fff0f5 0%, #fef3f2 100%);
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

/* 预定图标（放在名称右边） */
.table-card__reserve-icon {
  color: #909399;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
  margin-left: 8px;
  opacity: 1;
  font-size: 22px;
  transform: scale(0.9);
}

.table-card__reserve-icon:hover {
  background: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
  transform: scale(1.1);
}

.table-card__reserve-icon.reserved {
  color: #F56C6C;
}

.table-card__reserve-icon.reserved:hover {
  background: rgba(245, 108, 108, 0.1);
  color: #F56C6C;
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

.table-card__idle .idle-icon {
  width: 56px;
  height: 56px;
  animation: gentleIdleFloat 3s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(152, 216, 187, 0.3));
}

@keyframes gentleIdleFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

.table-card__idle-text {
  margin-top: 12px;
  font-size: 15px;
  color: #67C23A;
  font-weight: 500;
}

/* 预定状态 */
.table-card__reserved {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 8px;
}

.table-card__reserved-info {
  width: 100%;
  background: rgba(230, 162, 60, 0.1);
  border: 1px dashed #E6A23C;
  border-radius: 8px;
  padding: 12px;
}

.table-card__reserved-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #E6A23C;
}

.table-card__reserved-label {
  font-size: 14px;
  font-weight: 600;
  color: #E6A23C;
}

.table-card__reserved-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.table-card__reserved-item {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.table-card__reserved-item-label {
  color: #909399;
  min-width: 70px;
}

.table-card__reserved-item-value {
  color: #303133;
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

.table-card__amount-with-original {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.table-card__amount-single {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.table-card__amount-original {
  font-size: 11px;
  color: #909399;
  text-decoration: line-through;
}

.table-card__amount-value {
  font-size: 20px;
  font-weight: bold;
  color: #F56C6C;
}

.table-card__amount-label {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.table-card__member-discount {
  font-size: 10px;
  color: #67c23a;
  font-weight: 500;
  margin-top: 2px;
}

/* 预设时长 */
.table-card__preset {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 11px;
  color: #606266;
  background: rgba(0, 0, 0, 0.04);
  padding: 6px 8px;
  border-radius: 4px;
  margin-bottom: 6px;
  min-height: 32px;
}

.table-card__remaining {
  color: #E6A23C;
  font-weight: 500;
}

.table-card__extended {
  color: #909399;
  font-weight: 500;
}

.table-card__extend-countdown {
  color: #E6A23C;
  font-weight: 500;
}

.table-card__overtime {
  color: #F56C6C;
  font-weight: 600;
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
