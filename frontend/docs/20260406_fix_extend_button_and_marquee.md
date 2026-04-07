# 修改记录

## 日期
2026-04-06

## 问题1：不设时长的台桌续费按钮没有隐藏

### 问题原因
前端判断不限时套餐的条件 `presetDuration === null` 不够全面，需要同时判断 `null`、`0` 和 `undefined`。

### 修改内容

#### `TableCard.vue` - 完善不限时套餐判断

```typescript
// 判断是否为不限时套餐（presetDuration 为 null、0 或 undefined 表示不限时）
const isUnlimited = computed(() => {
  return props.table.presetDuration === null || props.table.presetDuration === 0 || props.table.presetDuration === undefined
})
```

## 问题2：跑马灯循环播放

### 问题说明
用户希望跑马灯播放完所有超时内容后，再从头开始播放，而不是无缝循环。

### 修改内容

#### `RemindPanel.vue` - 动态计算动画时长

```typescript
// 根据内容数量动态计算跑马灯动画时长（每个提醒约5秒）
const marqueeAnimationDuration = computed(() => {
  const count = marqueeReminds.value.length
  if (count === 0) return 0
  // 基础时长 + 每个提醒5秒
  return Math.max(30, count * 5)
})
```

#### `RemindPanel.vue` - 应用动态动画时长

```html
<div class="marquee-text" :class="{ animating: marqueeReminds.length > 0 }" :style="{ animationDuration: marqueeAnimationDuration + 's' }">
```

#### `RemindPanel.vue` - 简化动画关键帧

```css
@keyframes marquee {
  0% { transform: translateX(100%); }
  100% { transform: translateX(-100%); }
}
```

## 效果

**问题1修复效果**：
- ✅ 不设时长（presetDuration 为 0/null）的台桌不显示续费按钮
- ✅ 限时台台正常显示续费按钮

**问题2修复效果**：
- ✅ 跑马灯完整播放所有超时提醒
- ✅ 播放完毕后从头开始，循环播放
- ✅ 动画时长根据内容数量自动调整（每个提醒约5秒，最少30秒）

## 计算逻辑

```
动画时长 = Math.max(30, 提醒数量 × 5秒)
```

例如：
- 5条超时 → 25秒（取最小30秒）
- 10条超时 → 50秒
- 20条超时 → 100秒

@author wuci
