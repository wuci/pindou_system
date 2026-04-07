# 修改记录

## 日期
2026-04-06

## 问题描述
在开始计时界面选择"不限时"套餐时，价格显示不正确。原因是当用户点击不限时套餐时，`selectedRuleIndex` 变为数字索引（如3），而不是字符串 `'unlimited'`，导致模板中的条件判断 `v-if="selectedRuleIndex !== 'unlimited'"` 失效，费用信息仍然显示。

## 修改内容

### 修改文件
`frontend/src/components/StartTimerDialog.vue`

### 详细修改

#### 1. 添加计算属性判断当前选择是否为不限时套餐

```typescript
// 当前选择的规则
const currentSelectedRule = computed(() => {
  if (typeof selectedRuleIndex.value === 'number') {
    return currentRules.value[selectedRuleIndex.value]
  }
  return null
})

// 当前选择的价格
const currentRulePrice = computed(() => {
  if (currentSelectedRule.value) {
    return currentSelectedRule.value.price || 0
  }
  return 0
})

// 原价
const originalPrice = computed(() => {
  return currentRulePrice.value
})

// 判断当前选择是否为不限时套餐
const isSelectedRuleUnlimited = computed(() => {
  return currentSelectedRule.value?.unlimited === true
})
```

#### 2. 更新模板中的条件判断

将所有 `v-if="selectedRuleIndex !== 'unlimited'"` 改为 `v-if="!isSelectedRuleUnlimited"`：

```html
<!-- 套餐时长预览 -->
<el-form-item v-if="!isSelectedRuleUnlimited" label="套餐时长">

<!-- 延长时间 -->
<el-form-item v-if="!isSelectedRuleUnlimited" label="延长时间">

<!-- 总时长预览 -->
<el-form-item v-if="!isSelectedRuleUnlimited" label="总时长">

<!-- 费用信息 -->
<el-form-item v-if="!isSelectedRuleUnlimited" label="费用">
```

#### 3. 更新 `durationPreview` 计算属性

```typescript
const durationPreview = computed(() => {
  if (selectedRuleIndex.value === 'unlimited' || isSelectedRuleUnlimited.value) {
    return '不设时长'
  }
  // ... 其他逻辑
})
```

#### 4. 更新规则选择变化的 watch

```typescript
watch(
  [selectedRuleIndex, customHours, customMinutes],
  () => {
    if (selectedRuleIndex.value === 'unlimited' || isSelectedRuleUnlimited.value) {
      form.value.presetDuration = 0
    }
    // ... 其他逻辑
  }
)
```

#### 5. 更新开始计时提交逻辑

```typescript
// 直接根据选择的规则计算套餐时长
let packageDuration = 0

if (selectedRuleIndex.value === 'unlimited' || isSelectedRuleUnlimited.value) {
  packageDuration = 0
}
// ... 其他逻辑
```

## 修复效果

- ✅ 选择"不限时"套餐时，费用信息正确隐藏
- ✅ 预设时长正确设置为 0（不设时长）
- ✅ 延长时间和总时长预览正确隐藏
- ✅ 价格计算逻辑保持正确

## 技术要点

1. **问题根源**：所有套餐规则（包括不限时）都通过 `v-for` 渲染为 radio 按钮，不限时套餐的索引是数字而非特殊字符串
2. **解决方案**：添加 `isSelectedRuleUnlimited` 计算属性，通过检查当前选择规则的 `unlimited` 属性来判断
3. **兼容性**：同时保留对 `selectedRuleIndex === 'unlimited'` 的判断，以处理没有规则时的默认情况

## 注意事项

- 数据库中的计费规则配置，不限时套餐的字段结构为：`{"price": 68, "unlimited": true, "minutes": null}`
- 不同渠道的不限时套餐价格可能不同（工作日68元，节假日78元等）

@author wuci
