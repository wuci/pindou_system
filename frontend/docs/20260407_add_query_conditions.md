# 修改记录

## 日期
2026-04-07

## 需求
台台管理新增两个查询条件：
1. 已超时 - 筛选出已超时的台台
2. 剩余多少分钟内 - 输入分钟数，筛选即将到期的台台

## 修改内容

### 后端修改

#### 1. TableController.java - 添加查询参数

```java
@GetMapping
public Result<List<TableInfoResponse>> getTableList(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Boolean isOvertime,
        @RequestParam(required = false) Integer remainingMinutes) {
    // ...
}
```

#### 2. TableService.java - 更新接口

```java
List<TableInfoResponse> getTableList(String status, Long categoryId, String name,
                                      Boolean isOvertime, Integer remainingMinutes);
```

#### 3. TableServiceImpl.java - 实现查询逻辑

**修改 getTableList 方法：**
- 添加新参数 `isOvertime` 和 `remainingMinutes`
- 在内存中根据条件过滤结果

**新增 isOvertime 方法：**
- 判断台台是否已超时
- 只有 using/paused 状态且有预设时长的台台才会超时
- 计算公式：`当前时间 > 开始时间 + 预设时长 + 暂停时长`

**新增 willExpireWithin 方法：**
- 判断台台是否会在指定时间内到期
- 剩余时间 = 结束时间 - 当前时间
- 条件：剩余时间 > 0 且 <= 指定时间

### 前端修改

#### 1. table.ts - 更新API接口

```typescript
export const getTableList = (status?: string, categoryId?: number, name?: string,
                            isOvertime?: boolean, remainingMinutes?: number) => {
  return http.get<TableInfo[]>('/tables', {
    params: { status, categoryId, name, isOvertime, remainingMinutes }
  })
}
```

#### 2. TableManagement/index.vue - 添加查询控件

**添加状态变量：**
```typescript
const isOvertime = ref(false)
const remainingMinutes = ref<number | null>(null)
```

**添加UI控件：**
- `el-checkbox` - 已超时筛选
- `el-input-number` - 剩余分钟数输入框
- 文本提示："分钟内到期"

**修改 loadTables 方法：**
- 传递新参数到API调用

## 效果

- ✅ 勾选"已超时"复选框，只显示已超时的台台
- ✅ 输入分钟数（如30），显示30分钟内即将到期的台台
- ✅ 两个条件可以同时使用，取交集
- ✅ 清除条件后恢复正常显示

## 查询逻辑

**超时判断：**
```
结束时间 = 开始时间 + 预设时长(秒) + 暂停时长(秒)
已超时 = 当前时间 > 结束时间
```

**即将到期判断：**
```
剩余时间 = 结束时间 - 当前时间
即将到期 = 0 < 剩余时间 <= 输入分钟数 × 60 × 1000
```

## 修复记录

**2026-04-07 修复：**
- 将 `response.getPauseAccumulated()` 改为 `response.getPauseDuration()`
- `TableInfoResponse` 中字段名为 `pauseDuration`（Long 类型）
- 添加空值检查：`pauseDuration != null ? pauseDuration : 0L`

## 注意事项

- 不设时长的台台（presetDuration 为 null）不会超时，也不会在即将到期查询中显示
- 空闲状态的台台不会出现在查询结果中（因为没有计时数据）
- 查询过滤在内存中进行，先按基本条件查询数据库，再在结果中过滤

@author wuci
