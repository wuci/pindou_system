import { ref, computed, onUnmounted, type Ref } from 'vue'
import type { TableInfo } from '@/api/table'

/**
 * 桌台计时Hook
 * 用于计算和更新桌台的时长和费用
 */
export function useTableTimer(table: Ref<TableInfo | null>) {
  // 当前时间（用于实时更新）
  const currentTime = ref(Date.now())
  let timer: number | null = null

  // 启动定时器（每秒更新）
  const startTimer = () => {
    if (timer !== null) return
    timer = window.setInterval(() => {
      currentTime.value = Date.now()
    }, 1000)
  }

  // 停止定时器
  const stopTimer = () => {
    if (timer !== null) {
      clearInterval(timer)
      timer = null
    }
  }

  // 计算已用时长（秒）
  const duration = computed(() => {
    if (!table.value || !table.value.startTime) {
      return 0
    }

    const now = currentTime.value
    const startTime = table.value.startTime
    const totalElapsed = Math.floor((now - startTime) / 1000) // 总经过时间（秒）

    // 计算暂停时长
    let pauseDuration = table.value.pauseAccumulated || 0
    if (table.value.status === 'paused' && table.value.lastPauseTime) {
      // 如果当前是暂停状态，加上本次暂停时长
      pauseDuration += Math.floor((now - table.value.lastPauseTime) / 1000)
    }

    // 实际使用时长 = 总时长 - 暂停时长
    return Math.max(0, totalElapsed - pauseDuration)
  })

  // 计算总暂停时长（秒）
  const totalPauseDuration = computed(() => {
    if (!table.value) {
      return 0
    }

    let pauseDuration = table.value.pauseAccumulated || 0
    if (table.value.status === 'paused' && table.value.lastPauseTime) {
      pauseDuration += Math.floor((currentTime.value - table.value.lastPauseTime) / 1000)
    }

    return pauseDuration
  })

  // 计算剩余时长（秒）- 仅当设置了预设时长时
  const remainingDuration = computed(() => {
    if (!table.value || !table.value.presetDuration) {
      return null
    }
    const remaining = table.value.presetDuration - duration.value
    return Math.max(0, remaining)
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

  // 格式化时长为 HH:MM:SS
  const formatDuration = (seconds: number): string => {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60

    const pad = (num: number) => num.toString().padStart(2, '0')
    return `${pad(hours)}:${pad(minutes)}:${pad(secs)}`
  }

  // 格式化的时长显示
  const formattedDuration = computed(() => formatDuration(duration.value))

  // 格式化的剩余时长显示
  const formattedRemainingDuration = computed(() => {
    if (remainingDuration.value === null) {
      return '不设时长'
    }
    return formatDuration(remainingDuration.value)
  })

  // 计算费用（简单按小时计费，每小时30元）
  const amount = computed(() => {
    if (!table.value || table.value.status === 'idle') {
      return 0
    }

    const hours = duration.value / 3600
    const HOURLY_RATE = 30 // 每小时30元
    return Math.round(hours * HOURLY_RATE * 100) / 100
  })

  // 格式化的金额显示
  const formattedAmount = computed(() => {
    return `¥${amount.value.toFixed(2)}`
  })

  // 组件卸载时清理定时器
  onUnmounted(() => {
    stopTimer()
  })

  return {
    currentTime,
    duration,
    totalPauseDuration,
    remainingDuration,
    isNearExpiry,
    isOvertime,
    formattedDuration,
    formattedRemainingDuration,
    amount,
    formattedAmount,
    startTimer,
    stopTimer
  }
}
