/**
 * WebSocket 消息类型
 */
export enum WebSocketMessageType {
  TABLE_STATUS = 'table_status',
  REMIND = 'remind',
  ORDER_STATUS = 'order_status',
  SYSTEM = 'system'
}

/**
 * WebSocket 消息结构
 */
export interface WebSocketMessage<T = any> {
  type: string
  data: T
  timestamp: number
}

/**
 * 桌台状态数据
 */
export interface TableStatusData {
  id: number
  name: string
  status: string
  currentOrderId: string | null
  startTime: number | null
  presetDuration: number | null
  pauseAccumulated: number
  lastPauseTime: number | null
  reminded: number
  remindIgnored: number
}

/**
 * 提醒数据
 */
export interface RemindData {
  tableId: number
  tableName: string
  remindType: string
  remindTypeDesc: string
  startTime: number
  presetDuration: number | null
  usedDuration: number
  remainingDuration: number
  overtimeDuration: number
}

/**
 * WebSocket 消息处理器类型
 */
export type MessageHandler = (message: WebSocketMessage) => void

/**
 * WebSocket 连接状态
 */
export enum WebSocketStatus {
  CONNECTING = 'connecting',
  CONNECTED = 'connected',
  DISCONNECTED = 'disconnected',
  ERROR = 'error'
}

/**
 * WebSocket 配置选项
 */
export interface WebSocketOptions {
  /** WebSocket 服务器地址 */
  url?: string
  /** 用户 ID */
  userId?: string
  /** 是否自动重连 */
  autoReconnect?: boolean
  /** 重连间隔（毫秒） */
  reconnectInterval?: number
  /** 最大重连次数 */
  maxReconnectAttempts?: number
  /** 心跳间隔（毫秒） */
  heartbeatInterval?: number
}

/**
 * WebSocket 管理类
 */
export class WebSocketManager {
  private ws: WebSocket | null = null
  private status: WebSocketStatus = WebSocketStatus.DISCONNECTED
  private options: Required<WebSocketOptions>
  private messageHandlers: Map<string, Set<MessageHandler>> = new Map()
  private reconnectTimer: number | null = null
  private heartbeatTimer: number | null = null
  private reconnectAttempts = 0
  private manualClose = false

  // 默认配置
  private static DEFAULT_OPTIONS: Required<WebSocketOptions> = {
    url: '',
    userId: '',
    autoReconnect: true,
    reconnectInterval: 3000,
    maxReconnectAttempts: 10,
    heartbeatInterval: 30000
  }

  constructor(options: WebSocketOptions = {}) {
    this.options = { ...WebSocketManager.DEFAULT_OPTIONS, ...options }
  }

  /**
   * 设置用户 ID
   */
  setUserId(userId: string): void {
    this.options.userId = userId
  }

  /**
   * 连接 WebSocket
   */
  connect(url?: string): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.warn('[WebSocket] 已连接，无需重复连接')
      return
    }

    // 构建WebSocket URL
    const wsUrl = url || this.options.url || this.buildWsUrl()

    console.log('[WebSocket] 正在连接:', wsUrl)
    this.status = WebSocketStatus.CONNECTING
    this.manualClose = false

    try {
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = this.handleOpen.bind(this)
      this.ws.onmessage = this.handleMessage.bind(this)
      this.ws.onerror = this.handleError.bind(this)
      this.ws.onclose = this.handleClose.bind(this)
    } catch (error) {
      console.error('[WebSocket] 连接失败:', error)
      this.status = WebSocketStatus.ERROR
      this.scheduleReconnect()
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.manualClose = true
    this.stopHeartbeat()
    this.clearReconnectTimer()

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }

    this.status = WebSocketStatus.DISCONNECTED
    console.log('[WebSocket] 已断开连接')
  }

  /**
   * 发送消息
   */
  send(data: string | object): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message = typeof data === 'string' ? data : JSON.stringify(data)
      this.ws.send(message)
    } else {
      console.warn('[WebSocket] 未连接，无法发送消息:', data)
    }
  }

  /**
   * 注册消息处理器
   */
  on(type: string, handler: MessageHandler): () => void {
    if (!this.messageHandlers.has(type)) {
      this.messageHandlers.set(type, new Set())
    }
    this.messageHandlers.get(type)!.add(handler)

    // 返回取消订阅函数
    return () => {
      this.off(type, handler)
    }
  }

  /**
   * 取消消息处理器
   */
  off(type: string, handler: MessageHandler): void {
    const handlers = this.messageHandlers.get(type)
    if (handlers) {
      handlers.delete(handler)
      if (handlers.size === 0) {
        this.messageHandlers.delete(type)
      }
    }
  }

  /**
   * 获取连接状态
   */
  getStatus(): WebSocketStatus {
    return this.status
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.status === WebSocketStatus.CONNECTED
  }

  /**
   * 处理连接打开
   */
  private handleOpen(): void {
    console.log('[WebSocket] 连接成功')
    this.status = WebSocketStatus.CONNECTED
    this.reconnectAttempts = 0
    this.startHeartbeat()
  }

  /**
   * 处理收到消息
   */
  private handleMessage(event: MessageEvent): void {
    try {
      const message: WebSocketMessage = JSON.parse(event.data)

      // 处理心跳响应
      if (event.data === 'pong') {
        return
      }

      console.debug('[WebSocket] 收到消息:', message)

      // 触发特定类型的处理器
      if (message.type) {
        const handlers = this.messageHandlers.get(message.type)
        if (handlers) {
          handlers.forEach(handler => {
            try {
              handler(message)
            } catch (error) {
              console.error('[WebSocket] 消息处理错误:', error)
            }
          })
        }
      }

      // 触发全局处理器
      const allHandlers = this.messageHandlers.get('*')
      if (allHandlers) {
        allHandlers.forEach(handler => handler(message))
      }
    } catch (error) {
      console.error('[WebSocket] 消息解析错误:', error)
    }
  }

  /**
   * 处理连接错误
   */
  private handleError(event: Event): void {
    console.error('[WebSocket] 连接错误:', event)
    this.status = WebSocketStatus.ERROR
  }

  /**
   * 处理连接关闭
   */
  private handleClose(event: CloseEvent): void {
    console.log('[WebSocket] 连接关闭:', event.code, event.reason)
    this.status = WebSocketStatus.DISCONNECTED
    this.stopHeartbeat()

    if (!this.manualClose && this.options.autoReconnect) {
      this.scheduleReconnect()
    }
  }

  /**
   * 安排重连
   */
  private scheduleReconnect(): void {
    if (this.reconnectAttempts >= this.options.maxReconnectAttempts) {
      console.error('[WebSocket] 达到最大重连次数，停止重连')
      return
    }

    this.clearReconnectTimer()

    const delay = this.options.reconnectInterval * Math.pow(1.5, this.reconnectAttempts)
    console.log(`[WebSocket] ${delay}ms 后进行第 ${this.reconnectAttempts + 1} 次重连`)

    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectAttempts++
      this.connect()
    }, delay)
  }

  /**
   * 清除重连定时器
   */
  private clearReconnectTimer(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  /**
   * 开始心跳
   */
  private startHeartbeat(): void {
    this.stopHeartbeat()

    this.heartbeatTimer = window.setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send('ping')
      }
    }, this.options.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 构建 WebSocket URL
   */
  private buildWsUrl(): string {
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = location.host
    const userId = this.options.userId || 'anonymous'

    return `${protocol}//${host}/ws?userId=${userId}`
  }

  /**
   * 销毁
   */
  destroy(): void {
    this.disconnect()
    this.messageHandlers.clear()
  }
}

/**
 * 全局 WebSocket 实例
 */
let globalWsInstance: WebSocketManager | null = null

/**
 * 获取全局 WebSocket 实例
 */
export function getWebSocket(): WebSocketManager {
  if (!globalWsInstance) {
    globalWsInstance = new WebSocketManager()
  }
  return globalWsInstance
}

/**
 * 初始化 WebSocket
 */
export function initWebSocket(options: WebSocketOptions): WebSocketManager {
  if (globalWsInstance) {
    globalWsInstance.destroy()
  }
  globalWsInstance = new WebSocketManager(options)
  globalWsInstance.connect()
  return globalWsInstance
}
