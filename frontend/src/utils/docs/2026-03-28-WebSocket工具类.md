# WebSocket 工具类开发文档

## 开发日期
2026-03-28

## 开发者
wuci

## 模块概述
`websocket.ts` 提供了完整的 WebSocket 封装，支持连接管理、消息订阅、自动重连等功能。

## 类和接口

### WebSocketManager
WebSocket 连接管理类

#### 构造函数
```typescript
constructor(options?: WebSocketOptions)
```

#### 配置选项
```typescript
interface WebSocketOptions {
  url?: string              // WebSocket 服务器地址
  userId?: string           // 用户 ID
  autoReconnect?: boolean   // 是否自动重连，默认 true
  reconnectInterval?: number // 重连间隔（毫秒），默认 3000
  maxReconnectAttempts?: number // 最大重连次数，默认 10
  heartbeatInterval?: number    // 心跳间隔（毫秒），默认 30000
}
```

#### 方法

| 方法 | 说明 |
|------|------|
| `connect(url?)` | 连接 WebSocket |
| `disconnect()` | 断开连接 |
| `send(data)` | 发送消息 |
| `on(type, handler)` | 注册消息处理器 |
| `off(type, handler)` | 取消消息处理器 |
| `getStatus()` | 获取连接状态 |
| `isConnected()` | 是否已连接 |
| `setUserId(userId)` | 设置用户 ID |
| `destroy()` | 销毁实例 |

#### 事件类型
```typescript
enum WebSocketMessageType {
  TABLE_STATUS = 'table_status',
  REMIND = 'remind',
  ORDER_STATUS = 'order_status',
  SYSTEM = 'system'
}
```

#### 连接状态
```typescript
enum WebSocketStatus {
  CONNECTING = 'connecting',
  CONNECTED = 'connected',
  DISCONNECTED = 'disconnected',
  ERROR = 'error'
}
```

## 使用示例

### 基本使用
```typescript
import { WebSocketManager } from '@/utils/websocket'

const ws = new WebSocketManager({
  userId: 'user123',
  autoReconnect: true
})

ws.connect()

// 订阅消息
ws.on('table_status', (message) => {
  console.log('桌台状态变更:', message.data)
})
```

### 使用全局实例
```typescript
import { getWebSocket, initWebSocket } from '@/utils/websocket'

// 初始化全局实例
const ws = initWebSocket({
  userId: 'user123'
})

// 获取全局实例
const ws = getWebSocket()
```

### 与 Pinia Store 配合
```typescript
import { useWebSocketStore } from '@/stores/websocket'

const webSocketStore = useWebSocketStore()

// 初始化
webSocketStore.init('user123')

// 订阅桌台状态
const unsubscribe = webSocketStore.onTableStatusChange((data) => {
  console.log('桌台状态:', data)
})

// 取消订阅
unsubscribe()
```

## 数据类型

### WebSocketMessage
```typescript
interface WebSocketMessage<T = any> {
  type: string
  data: T
  timestamp: number
}
```

### TableStatusData
```typescript
interface TableStatusData {
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
```

### RemindData
```typescript
interface RemindData {
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
```

## 重连机制
- 指数退避算法：每次重连间隔乘以 1.5
- 最大重连次数：10 次
- 手动断开不会触发重连

## 心跳机制
- 客户端每 30 秒发送 "ping"
- 服务器自动回复 "pong"
- 保持连接活跃
