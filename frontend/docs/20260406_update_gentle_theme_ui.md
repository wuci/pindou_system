# 修改记录

## 日期
2026-04-06

## 修改内容
为"豆屿温柔集"系统应用全局温柔风格UI主题

### 修改文件
1. `frontend/src/layouts/DefaultLayout.vue` - 更新布局样式
2. `frontend/src/assets/styles/gentle-theme.scss` - 新增温柔主题样式
3. `frontend/src/assets/styles/index.scss` - 更新样式入口
4. `frontend/src/main.ts` - 引入全局样式

### 主题配色
| 颜色 | 用途 | 色值 |
|------|------|------|
| 主色 | 按钮、高亮 | #d4a5ff（淡紫） |
| 辅色 | 渐变、装饰 | #ffb6d9（柔粉） |
| 点缀 | 辅助色 | #c5e3ff（淡蓝） |

### 样式特性

#### 1. 渐变效果
- 主渐变：`linear-gradient(135deg, #d4a5ff, #ffb6d9)`
- 背景渐变：`linear-gradient(135deg, #ffeef8, #f3e7ff, #e8f4ff)`
- 卡片渐变：`rgba(255, 255, 255, 0.95)` 半透明

#### 2. 毛玻璃效果
- 头部、侧边栏、对话框使用 `backdrop-filter: blur(10px)`
- 半透明背景营造层次感

#### 3. 柔和阴影
- 小阴影：`0 2px 8px rgba(212, 165, 255, 0.1)`
- 中阴影：`0 4px 16px rgba(212, 165, 255, 0.15)`
- 大阴影：`0 8px 32px rgba(212, 165, 255, 0.2)`

#### 4. 圆角统一
- 小：8px
- 中：12px
- 大：16px
- 超大：24px

### Element Plus 组件样式覆盖

**已优化的组件**：
- Card - 毛玻璃卡片效果
- Button - 渐变按钮 + 悬浮动画
- Input - 柔和边框 + 聚焦效果
- Select - 下拉选项温柔风格
- Table - 表头渐变背景
- Dialog - 毛玻璃对话框
- Message - 半透明消息提示
- Tag - 渐变标签
- Checkbox/Radio - 紫色选中态
- Switch - 紫色开关

### 布局更新

**头部导航栏**：
- 半透明白色背景
- 标题渐变文字效果
- 柔和阴影

**侧边菜单**：
- 渐变背景（上白下淡紫）
- 菜单项圆角卡片样式
- 悬浮/激活态渐变效果

**主内容区**：
- 透明背景，显示页面渐变
- 24px 内边距

### 滚动条样式
- 渐变滚动条轨道
- 紫粉渐变滚动条滑块

### 工具类
- `.text-gradient` - 渐变文字
- `.card-gentle` - 温柔风格卡片

@author wuci
