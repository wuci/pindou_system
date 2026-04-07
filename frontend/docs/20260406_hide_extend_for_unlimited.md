# 修改记录

## 日期
2026-04-06

## 问题描述
不限时的桌台仍然显示续费按钮，但根据业务逻辑，不限时套餐不应该支持续费。

## 解决方案

在 `TableCard.vue` 中添加不限时套餐判断，隐藏续费按钮。

### 修改内容

#### `TableCard.vue` - 添加不限时套餐判断

```typescript
// 判断是否为不限时套餐
const isUnlimited = computed(() => {
  return props.table.presetDuration === null
})

// 是否有续费权限
const hasExtendPermission = computed(() => {
  return userStore.hasPermission('table:extend')
})

// 是否允许续费（有权限且不是不限时套餐）
const canShowExtend = computed(() => {
  return hasExtendPermission.value && !isUnlimited.value
})
```

#### `TableCard.vue` - 修改续费按钮显示条件

```html
<!-- 使用中状态 -->
<template v-else-if="table.status === 'using'">
  <el-button v-if="permissions.canPause" size="small">
    暂停
  </el-button>
  <el-button v-if="canShowExtend" type="warning" size="small">
    续费
  </el-button>
  <el-button v-if="permissions.canBill" type="success" size="small">
    结账
  </el-button>
</template>

<!-- 暂停状态 -->
<template v-else-if="table.status === 'paused'">
  <el-button v-if="permissions.canPause" type="primary" size="small">
    继续
  </el-button>
  <el-button v-if="canShowExtend" type="warning" size="small">
    续费
  </el-button>
  <el-button v-if="permissions.canBill" type="success" size="small">
    结账
  </el-button>
</template>
```

## 修复效果

- ✅ 不限时桌台不显示续费按钮
- ✅ 限时桌台正常显示续费按钮
- ✅ 权限控制仍然有效（需要 `table:extend` 权限）

## 判断逻辑

不限时套餐的判断条件：`table.presetDuration === null`

- 限时套餐：`presetDuration` 为具体的秒数（如 3600、7200 等）
- 不限时套餐：`presetDuration` 为 `null`

@author wuci
