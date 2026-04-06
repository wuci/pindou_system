# 修改记录

## 日期
2026-04-06

## 修改内容

### 1. 移除登录界面默认账号密码提示
- **文件**: `frontend/src/views/Login/index.vue`
- **变更**: 删除了显示默认账号密码的提示区域
- **原内容**:
  ```html
  <div class="tips">
    <p>默认账号：admin / 123456</p>
    <p>店长账号：manager / 123456</p>
    <p>店员账号：staff / 123456</p>
  </div>
  ```

### 2. 清理所有 console 语句
清理了以下约120+处 console 语句：

#### 主要文件
- `src/App.vue` - 7处
- `src/components/BillDialog.vue` - 9处
- `src/components/ExtendDialog.vue` - 30处
- `src/components/StartTimerDialog.vue` - 30处
- `src/utils/storage.ts` - 4处
- `src/utils/websocket.ts` - 8处
- `src/views/OrderManagement/index.vue` - 11处

#### 其他文件
- `src/components/MemberSelectDialog.vue`
- `src/components/OrderDetailDrawer.vue`
- `src/components/PermissionTree.vue`
- `src/components/RemindPanel.vue`
- `src/components/TableLayoutEditor.vue`
- `src/composables/useChannelTranslation.ts`
- `src/router/index.ts`
- `src/stores/user.ts`
- `src/stores/websocket.ts`
- `src/views/Dashboard/index.vue`
- `src/views/MemberLevelManagement/index.vue`
- `src/views/MemberManagement/index.vue`
- `src/views/OperationLog/index.vue`
- `src/views/Report/index.vue`
- `src/views/RoleManagement/index.vue`
- `src/views/Settings/index.vue`
- `src/views/TableManagement/index.vue`
- `src/views/UserManagement/index.vue`

## 清理类型
- console.log()
- console.error()
- console.warn()
- console.info()
- console.debug()

## 原因
生产环境不需要显示调试信息和默认账号密码，提高安全性和专业性。

@author wuci
