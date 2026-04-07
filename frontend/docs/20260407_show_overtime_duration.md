# 修改记录

## 日期
2026-04-07

## 问题描述
跑马灯和提醒面板中显示的时间是总使用时长（usedDuration），用户希望显示超时时长（overtimeDuration）。

## 解决方案
将 `usedDuration` 改为 `overtimeDuration`，并添加"超时"前缀。

### 修改内容

#### `RemindPanel.vue` - 跑马灯显示

**修改前：**
```html
<span v-for="(remind, index) in displayReminds" :key="index" class="marquee-item">
  【{{ remind.remindTypeDesc }}】{{ remind.tableName }} - {{ formatDuration(remind.usedDuration) }}
</span>
```

**修改后：**
```html
<span v-for="(remind, index) in displayReminds" :key="index" class="marquee-item">
  【{{ remind.remindTypeDesc }}】{{ remind.tableName }} - 超时{{ formatDuration(remind.overtimeDuration) }}
</span>
```

#### `RemindPanel.vue` - 下拉提醒面板显示

**修改前：**
```html
<div class="item-right">
  <span class="time-text">{{ formatDuration(remind.usedDuration) }}</span>
  <el-icon class="close-btn"><Close /></el-icon>
</div>
```

**修改后：**
```html
<div class="item-right">
  <span class="time-text">超时{{ formatDuration(remind.overtimeDuration) }}</span>
  <el-icon class="close-btn"><Close /></el-icon>
</div>
```

## 效果

- ✅ 跑马灯显示"超时X分钟"而不是总时长
- ✅ 下拉提醒面板也统一显示超时时长
- ✅ 用户可以更直观地看到每个台台的超时情况

## 显示对比

**修改前：**
- 跑马灯：【已超时】A桌 - 2小时30分
- 提醒面板：2小时30分

**修改后：**
- 跑马灯：【已超时】A桌 - 超时30分钟
- 提醒面板：超时30分钟

@author wuci
