# 修复打包 TypeScript 编译错误

## 日期
2026-04-04

## 问题描述

执行 Maven 构建时，前端 TypeScript 编译报错：

```
[INFO] src/views/TableManagement/index.vue(208,5): error TS2322: Type 'AxiosResponse<TableCategoryResponse[], any, {}>' is not assignable to type 'TableCategoryResponse[] | ...'
[INFO] src/views/TableManagement/index.vue(251,28): error TS6133: 'table' is declared but its value is never read.
[INFO] src/views/TableManagement/index.vue(256,29): error TS6133: 'table' is declared but its value is never read.
[INFO] src/views/TableManagement/index.vue(273,35): error TS6133: 'table' is declared but its value is never read.
[INFO] src/views/TableManagement/index.vue(340,7): error TS6133: 'clearSelection' is declared but its value is never read.
[INFO] src/views/UserManagement/index.vue(237,110): error TS6133: 'CreateUserParams' is declared but its value is never read.
[INFO] src/views/UserManagement/index.vue(237,133): error TS6133: 'UpdateUserParams' is declared but its value is never read.
[INFO] src/views/UserManagement/index.vue(303,34): error TS6133: 'rule' is declared but its value is never read.
[INFO] src/views/UserManagement/index.vue(429,11): error TS2554: Expected 2 arguments, but got 1.
```

## 修复内容

### 1. tableCategory.ts - 请求方法类型错误

**文件：** `frontend/src/api/tableCategory.ts`

**问题：** 使用 `request`（axios 实例）而不是 `http`（封装后的请求对象）

**修复：**
```typescript
// 修改前
import request from '@/utils/request'
return request.get<TableCategoryResponse[]>('/table-category/list')

// 修改后
import { http } from '@/utils/request'
return http.get<TableCategoryResponse[]>('/table-category/list')
```

### 2. TableManagement/index.vue - 未使用的变量

**文件：** `frontend/src/views/TableManagement/index.vue`

**修复：**
- 第 251 行：`handlePause` 参数改为 `_table` 表示有意忽略
- 第 256 行：`handleResume` 参数改为 `_table` 表示有意忽略
- 第 273 行：`handleIgnoreRemind` 参数改为 `_table` 表示有意忽略
- 第 340 行：删除未使用的 `clearSelection` 函数

### 3. UserManagement/index.vue - 未使用的导入和参数错误

**文件：** `frontend/src/views/UserManagement/index.vue`

**修复：**
- 第 237 行：删除未使用的 `CreateUserParams` 和 `UpdateUserParams` 导入
- 第 303 行：`validateConfirmPassword` 的 `rule` 参数改为 `_rule`
- 第 429 行：为 `resetPassword` 函数添加缺失的 `password` 参数

```typescript
// 修改前
await resetPassword(row.id)

// 修改后
await resetPassword(row.id, '123456')
```

## 修改文件清单

| 文件 | 修改内容 |
|------|----------|
| `frontend/src/api/tableCategory.ts` | 使用 `http` 替代 `request` |
| `frontend/src/views/TableManagement/index.vue` | 修复未使用变量，删除未使用函数 |
| `frontend/src/views/UserManagement/index.vue` | 删除未使用导入，修复函数参数 |

## 开发者信息
- @author: wuci
- @date: 2026-04-04
