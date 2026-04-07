# 修改记录

## 日期
2026-04-06

## 问题描述
超时的桌台有20多条，但跑马灯只显示3条。

## 问题原因
`RemindPanel.vue` 中的代码限制了跑马灯最多显示3条提醒：
```typescript
marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout').slice(0, 3)
```

## 解决方案
移除 `.slice(0, 3)` 限制，显示所有超时提醒。

### 修改内容

#### `RemindPanel.vue` - 移除跑马灯数量限制

**修改前（3处）：**
```typescript
// 第150行
marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout').slice(0, 3)

// 第171行
marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout').slice(0, 3)

// 第204行
marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout').slice(0, 3)
```

**修改后：**
```typescript
// 第150行
marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout')

// 第171行
marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout')

// 第204行
marqueeReminds.value = remindList.value.filter(r => r.remindType === 'timeout')
```

## 修复效果

- ✅ 跑马灯显示所有超时桌台提醒（不再限制3条）
- ✅ 只显示超时类型的提醒（即将到期、已超时等）
- ✅ 跑马灯动画正常滚动显示

## 注意事项

由于超时桌台数量较多（20多条），跑马灯滚动时间可能需要较长才能完整显示一遍。如需调整滚动速度，可修改动画时长：

```css
.marquee-text.animating {
  animation: marquee 20s linear infinite;  /* 调整此处的时间 */
}
```

@author wuci
