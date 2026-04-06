# 修改记录

## 日期
2026-04-06

## 修改内容
为系统图标添加温柔主题样式

### 修改文件
1. `frontend/src/components/GentleIcon.vue` - 新增温柔主题图标组件
2. `frontend/src/assets/styles/gentle-theme.scss` - 添加图标样式
3. `frontend/src/components/TableCard.vue` - 更新空闲状态图标和卡片颜色

### 新增内容

#### 1. 温柔图标组件 (GentleIcon.vue)
- 岛屿形状图标
- 白色光点装饰
- 连接线条（渡船意象）

#### 2. 图标样式 (gentle-theme.scss)
新增以下样式类：

| 样式类 | 作用 |
|--------|------|
| `.el-icon` | 基础图标过渡效果 |
| `.el-menu-item .el-icon` | 菜单图标悬停放大 |
| `.icon-idle` | 空闲图标温柔绿 |
| `.icon-using` | 使用中图标温柔紫 |
| `.icon-paused` | 暂停图标温柔粉 |
| `.gentle-pulse` | 温柔脉动动画 |
| `.gentle-float` | 温柔浮动动画 |
| `.icon-gentle-wrapper` | 图标容器温柔背景 |
| `.table-card__idle .el-icon` | 空闲图标特殊效果 |
| `.table-card__timer .el-icon` | 计时图标温柔蓝 |
| `.table-card__reserved .el-icon` | 预定图标温柔橙 |

#### 3. 桌台卡片更新
- 空闲状态：改为岛屿SVG图标（带光点和连接）
- 空闲颜色：温柔绿 #98d4bb
- 使用中颜色：温柔紫 #d4a5ff
- 暂停颜色：温柔粉 #ffb6d9
- 卡片背景：对应温柔渐变

### 动画效果

**温柔浮动**（空闲图标）：
- 3秒循环
- 上下浮动4px
- 柔和阴影

**温柔脉动**：
- 2秒循环
- 轻微缩放
- 透明度变化

**温柔铃铛摇晃**：
- 0.6秒动画
- 左右摇晃+轻微缩放
- 柔和粉色

### 颜色对应

| 状态 | 颜色 | 寓意 |
|------|------|------|
| 空闲 | #98d4bb | 温柔绿 - 安静等待 |
| 使用中 | #d4a5ff | 温柔紫 - 温暖陪伴 |
| 暂停 | #ffb6d9 | 温柔粉 - 休息时光 |
| 预定 | #ffd993 | 温柔橙 - 预约期待 |

@author wuci
