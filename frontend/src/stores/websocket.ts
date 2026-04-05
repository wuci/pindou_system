import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUserStore } from './user'
import {
  initWebSocket,
  WebSocketManager,
  WebSocketStatus,
  WebSocketMessage,
  WebSocketMessageType,
  TableStatusData,
  RemindData
} from '@/utils/websocket'

export const useWebSocketStore = defineStore('websocket', () => {
  // WebSocket 管理器
  const wsManager = ref<WebSocketManager | null>(null)

  // 连接状态
  const status = ref<WebSocketStatus>(WebSocketStatus.DISCONNECTED)

  // 在线用户数
  const onlineCount = ref(0)

  // 是否已初始化
  const isInitialized = ref(false)

  // 计算属性
  const isConnected = computed(() => status.value === WebSocketStatus.CONNECTED)
  const isConnecting = computed(() => status.value === WebSocketStatus.CONNECTING)

  /**
   * 初始化 WebSocket 连接
   */
  function init(userId?: string) {
    if (isInitialized.value) {
      console.warn('[WebSocketStore] 已经初始化，无需重复初始化')
      return
    }

    // 获取用户 ID
    const userStore = useUserStore()
    const currentUserId = userId || userStore.userInfo?.id || ''

    // 初始化 WebSocket
    wsManager.value = initWebSocket({
      userId: currentUserId,
      autoReconnect: true,
      reconnectInterval: 3000,
      maxReconnectAttempts: 10,
      heartbeatInterval: 30000
    })

    // 监听连接状态变化
    wsManager.value.on('*', (message: WebSocketMessage) => {
      if (message.type === WebSocketMessageType.SYSTEM) {
        // 系统消息可以在这里处理
        console.log('[WebSocketStore] 系统消息:', message.data)
      }
    })

    // 更新连接状态
    updateStatus()

    isInitialized.value = true
    console.log('[WebSocketStore] 初始化完成')
  }

  /**
   * 断开连接
   */
  function disconnect() {
    if (wsManager.value) {
      wsManager.value.disconnect()
    }
    isInitialized.value = false
    status.value = WebSocketStatus.DISCONNECTED
  }

  /**
   * 重新连接
   */
  function reconnect() {
    if (wsManager.value) {
      wsManager.value.connect()
    }
  }

  /**
   * 更新连接状态
   */
  function updateStatus() {
    if (wsManager.value) {
      status.value = wsManager.value.getStatus()
    }
  }

  /**
   * 监听桌台状态变更
   */
  function onTableStatusChange(callback: (data: TableStatusData) => void) {
    if (!wsManager.value) {
      console.warn('[WebSocketStore] WebSocket 未初始化')
      return () => {}
    }

    return wsManager.value.on(WebSocketMessageType.TABLE_STATUS, (message: WebSocketMessage) => {
      callback(message.data as TableStatusData)
    })
  }

  /**
   * 监听提醒消息
   */
  function onRemind(callback: (data: RemindData) => void) {
    if (!wsManager.value) {
      console.warn('[WebSocketStore] WebSocket 未初始化')
      return () => {}
    }

    return wsManager.value.on(WebSocketMessageType.REMIND, (message: WebSocketMessage) => {
      callback(message.data as RemindData)
    })
  }

  /**
   * 监听所有消息
   */
  function onMessage(callback: (message: WebSocketMessage) => void) {
    if (!wsManager.value) {
      console.warn('[WebSocketStore] WebSocket 未初始化')
      return () => {}
    }

    return wsManager.value.on('*', callback)
  }

  /**
   * 发送消息
   */
  function send(data: string | object) {
    if (wsManager.value) {
      wsManager.value.send(data)
    }
  }

  return {
    // 状态
    status,
    onlineCount,
    isInitialized,

    // 计算属性
    isConnected,
    isConnecting,

    // 方法
    init,
    disconnect,
    reconnect,
    updateStatus,
    onTableStatusChange,
    onRemind,
    onMessage,
    send
  }
})
