# 修改记录

## 日期
2026-04-06

## 修改内容
修复 npm run build 中的 TypeScript 未使用变量错误

### 修改文件

#### 1. frontend/src/components/TableCard.vue

**修改内容**：移除未使用的 `CircleCheck` 图标导入

```typescript
// 修改前
import { Clock, CircleCheck, Edit, Calendar } from '@element-plus/icons-vue'

// 修改后
import { Clock, Edit, Calendar } from '@element-plus/icons-vue'
```

#### 2. frontend/src/router/index.ts

**修改内容**：将路由守卫中未使用的 `from` 参数标记为未使用

```typescript
// 修改前
router.beforeEach(async (to, from, next) => {

// 修改后
router.beforeEach(async (to, _from, next) => {
```

#### 3. frontend/src/utils/websocket.ts

**修改内容**：将未使用的事件参数标记为未使用

```typescript
// 修改前
private handleError(event: Event): void {
private handleClose(event: CloseEvent): void {

// 修改后
private handleError(_event: Event): void {
private handleClose(_event: CloseEvent): void {
```

**注意**：`handleMessage` 方法的 `event` 参数保留，因为该方法内部使用了 `event.data`

#### 4. frontend/src/views/Login/index.vue

**修改内容**：将 `getParticleStyle` 函数中未使用的 `index` 参数标记为未使用

```typescript
// 修改前
const getParticleStyle = (index: number) => {

// 修改后
const getParticleStyle = (_index: number) => {
```

#### 5. frontend/src/components/StartTimerDialog.vue

**修改内容**：移除未使用的 `extendDuration` 和 `totalDisplayDuration` 变量

```typescript
// 修改前
// 延长时间仅用于前端展示，不传给后端
const extendDuration = extendTime.value * 60
const totalDisplayDuration = packageDuration + extendDuration

// 修改后
// 延长时间仅用于前端展示，不传给后端
const extendDuration = extendTime.value * 60

// 最终修改：完全移除 extendDuration
// （因为实际也不使用）
```

#### 6. frontend/src/views/Dashboard/index.vue

**修改内容**：移除未使用的 `specialServices` 计算属性和 `getRuleContent` 函数

```typescript
// 移除：
const specialServices = computed(() => {
  return rulesData.value.services.filter(r => r.ruleType === RuleType.SPECIAL)
})

const getRuleContent = (category: string, ruleType?: string) => {
  const rules = rulesData.value[category] || []
  if (ruleType) {
    const rule = rules.find(r => r.ruleType === ruleType)
    return rule?.content || ''
  }
  return rules
}
```

#### 7. frontend/src/views/Report/index.vue

**修改内容**：移除未使用的 Element Plus 图标导入

```typescript
// 修改前
import { Money, Document, OfficeBuilding, TrendCharts } from '@element-plus/icons-vue'

// 修改后
// (完全移除该行，因为页面使用自定义 SVG 图标)
```

### 构建结果

✅ 所有 TypeScript 错误已修复
✅ 构建成功，产物输出到 `backend/pdsystem-server/src/main/resources/static/`

### 注意事项

- 使用 `_` 前缀标记参数为"故意未使用"，避免 TypeScript 警告
- 对于真正不需要的代码，直接删除比标记更好
- 移除未使用的导入可以减小最终打包体积

@author wuci
