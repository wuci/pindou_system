# 2026-03-29 Day 12 WebSocket 实时通信 + 提醒功能优化

## 后端修改

### 1. 新增模块
- **pdsystem-dto**: 新增 DTO 模块，用于存放数据传输对象
  - `pom.xml`: 模块配置文件
  - `WebSocketMessage.java`: WebSocket 消息 DTO

### 2. WebSocket 相关
- **WebsocketService.java**: WebSocket 服务接口
  - `broadcastTableStatusChange()`: 广播桌台状态变更
  - `broadcastRemind()`: 广播提醒消息
  - `getOnlineCount()`: 获取在线用户数

- **WebSocketServiceImpl.java**: WebSocket 服务实现
- **TableWebSocketHandler.java**: WebSocket 消息处理器
- **WebSocketConfig.java**: WebSocket 配置类

### 3. 定时任务
- **RemindTask.java**: 提醒定时任务
  - 每5秒检查一次提醒状态
  - 通过 WebSocket 推送提醒消息

### 4. 模块依赖调整
- `pom.xml`: 添加 pdsystem-dto 模块
- `pdsystem-server/pom.xml`: 添加 pdsystem-dto 依赖

## 前端修改

### 1. 提醒面板组件 (RemindPanel.vue)
**重新设计为小型下拉框 + 跑马灯**

#### 新增功能：
- **跑马灯提醒条**:
  - 顶部显示超时提醒
  - 自动滚动效果
  - 可关闭

- **小型下拉框**:
  - 宽度 320px，最大高度 300px
  - 紧凑的提醒列表布局
  - 点击提醒项快速忽略

- **Web Audio API 提示音**:
  - 无需外部音频文件
  - 生成"叮咚"提示音

#### 样式优化：
- 下拉动画效果
- 脉冲背景动画（超时提醒）
- 铃铛图标抖动动画

### 2. 布局组件 (DefaultLayout.vue)
- 添加顶部铃铛图标
- 显示未读提醒数量徽标
- 新提醒时铃铛抖动

### 3. 文件变更
- `frontend/src/components/RemindPanel.vue`: 完全重写
- `frontend/src/layouts/DefaultLayout.vue`: 更新

## 功能说明

### 提醒流程
1. 后端定时任务每5秒检查桌台状态
2. 发现即将到期/超时的桌台时：
   - 记录到数据库
   - 通过 WebSocket 推送到前端
3. 前端收到提醒：
   - 更新铃铛徽标数量
   - 触发抖动动画
   - 播放提示音
   - 显示跑马灯（超时）
   - 自动打开下拉框

### 跑马灯效果
- 位置：顶部导航栏下方
- 样式：黄色渐变背景
- 内容：超时桌台信息
- 动画：从右向左滚动
- 可通过关闭按钮隐藏

## API 接口

### WebSocket
- 连接地址: `ws://localhost:9026/api/ws`
- 消息格式: JSON
  ```json
  {
    "type": "table_status|remind|order_status|system",
    "data": {},
    "timestamp": 1234567890
  }
  ```

### REST API
- `GET /remind`: 获取提醒列表
- `POST /remind/ignore/{tableId}`: 忽略提醒
