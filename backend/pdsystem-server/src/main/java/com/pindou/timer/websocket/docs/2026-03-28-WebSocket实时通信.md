# WebSocket 实时通信功能开发

## 开发日期
2026-03-28

## 开发者
wuci

## 功能概述
实现 WebSocket 实时通信，支持桌台状态变更推送、提醒消息推送，替代前端轮询，提升用户体验。

## 涉及文件

### 后端新增
1. `dto/WebSocketMessage.java` - WebSocket 消息 DTO
2. `config/WebSocketConfig.java` - WebSocket 配置类
3. `websocket/TableWebSocketHandler.java` - WebSocket 消息处理器
4. `service/WebSocketService.java` - WebSocket 服务接口
5. `websocket/WebSocketServiceImpl.java` - WebSocket 服务实现

### 后端修改
1. `service/impl/TableServiceImpl.java` - 集成状态变更推送
2. `task/RemindTask.java` - 集成提醒消息推送

### 前端新增
1. `utils/websocket.ts` - WebSocket 封装工具类
2. `stores/websocket.ts` - WebSocket Pinia Store

### 前端修改
1. `layouts/DefaultLayout.vue` - 初始化 WebSocket
2. `views/TableManagement/index.vue` - 集成桌台状态实时更新
3. `components/RemindPanel.vue` - 集成提醒消息实时推送

## 功能特性

### 1. WebSocket 连接管理
- 连接建立/断开处理
- 心跳机制（30秒间隔）
- 断线自动重连（指数退避）
- 在线用户管理

### 2. 消息类型
| 类型 | 说明 | 数据结构 |
|------|------|----------|
| table_status | 桌台状态变更 | TableStatusData |
| remind | 提醒通知 | RemindData |
| order_status | 订单状态变更 | OrderStatusData |
| system | 系统消息 | String |

### 3. 消息推送
- **桌台状态推送**: 开始计时、暂停、恢复、结束结账时推送
- **提醒消息推送**: 检测到即将到期/超时时推送
- **广播模式**: 消息发送给所有在线用户
- **单播模式**: 消息发送给指定用户

### 4. 前端集成
- WebSocket 管理类封装
- Pinia Store 状态管理
- 组件级消息订阅
- 自动重连机制

## 接口说明

### WebSocket 连接
```
URL: ws://host/ws?userId={userId}
```

### 心跳机制
```
Client -> Server: "ping"
Server -> Client: "pong"
```

### 消息格式
```json
{
  "type": "table_status",
  "data": {
    "id": 1,
    "name": "1号桌",
    "status": "using",
    ...
  },
  "timestamp": 1711622400000
}
```

## 使用示例

### 后端推送消息
```java
@Resource
private WebSocketService webSocketService;

// 广播桌台状态变更
webSocketService.broadcastTableStatusChange(tableId);

// 广播提醒消息
webSocketService.broadcastRemind(remindInfo);
```

### 前端接收消息
```typescript
import { useWebSocketStore } from '@/stores/websocket'

const webSocketStore = useWebSocketStore()

// 订阅桌台状态变更
const unsubscribe = webSocketStore.onTableStatusChange((data) => {
  console.log('桌台状态变更:', data)
  // 更新本地状态
})

// 取消订阅
unsubscribe()
```

## 配置参数

### WebSocketConfig
```java
WEB_SOCKET_ENDPOINT = "/ws"
ALLOWED_ORIGINS = "*"
```

### WebSocketOptions
```typescript
{
  url: '',                    // 服务器地址
  userId: '',                 // 用户ID
  autoReconnect: true,        // 自动重连
  reconnectInterval: 3000,    // 重连间隔（毫秒）
  maxReconnectAttempts: 10,   // 最大重连次数
  heartbeatInterval: 30000    // 心跳间隔（毫秒）
}
```

## 优势
1. **实时性**: 消息即时推送，无需轮询
2. **性能**: 减少服务器压力和带宽消耗
3. **体验**: 用户状态同步更及时
4. **可靠性**: 自动重连，断线恢复

## 注意事项
1. 生产环境需配置正确的 ALLOWED_ORIGINS
2. 心跳间隔可根据网络环境调整
3. 重连次数和间隔需根据实际情况配置
4. WebSocket 连接需在用户登录后建立
