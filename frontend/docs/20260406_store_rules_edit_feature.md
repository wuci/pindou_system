# 修改记录

## 日期
2026-04-06

## 修改内容
店铺规则编辑功能 - 添加编辑按钮和权限控制

### 后端文件

#### 1. 权限SQL脚本
**文件**: `backend/sql/20260406/03_店铺规则权限.sql`

**新增权限**:
| 权限ID | 父级ID | 权限代码 | 权限名称 | 类型 |
|--------|--------|----------|----------|------|
| store_rules | 0 | store:rules:view | 店铺规则 | module |
| store_rules_view | store_rules | store:rules:view | 查看规则 | permission |
| store_rules_create | store_rules | store:rules:create | 新增规则 | permission |
| store_rules_update | store_rules | store:rules:update | 编辑规则 | permission |
| store_rules_delete | store_rules | store:rules:delete | 删除规则 | permission |
| store_rules_toggle | store_rules | store:rules:toggle | 启用/禁用 | permission |

**角色权限更新**:
- 超级管理员: 自动拥有所有权限
- 店长: 添加 `store:rules:update` 权限

### 前端文件

#### 1. 规则编辑对话框组件
**文件**: `frontend/src/components/StoreRuleDialog.vue`

**功能**:
- 新增/编辑规则
- 支持选择规则分类（套餐/服务/安全/其他）
- 支持选择规则类型（表格/列表/警告框/特色服务）
- 双Tab编辑模式：
  - 富文本编辑（HTML）
  - 表格数据（JSON）- 仅表格类型显示
- JSON格式化功能
- 表单验证

**Props**:
- `modelValue`: boolean - 对话框显示状态
- `rule`: StoreRule | null - 要编辑的规则

**Emits**:
- `update:modelValue`: 更新显示状态
- `success`: 保存成功回调

#### 2. Dashboard组件更新
**文件**: `frontend/src/views/Dashboard/index.vue`

**新增功能**:
1. 权限检查 - 只对有编辑权限的用户显示编辑按钮
2. 新增规则按钮 - 位于欢迎标题右侧
3. 每个规则区块的编辑按钮 - 右上角小按钮

**新增状态**:
- `canEditRules`: computed - 是否有编辑权限
- `dialogVisible`: ref - 对话框显示状态
- `currentRule`: ref - 当前编辑的规则

**新增方法**:
- `handleCreateRule()`: 打开新增规则对话框
- `handleEditRule(rule)`: 打开编辑规则对话框
- `handleRuleSaved()`: 规则保存成功回调，重新加载数据

**样式更新**:
1. 新增规则按钮样式:
   - 渐变背景（淡紫→柔粉）
   - 圆角按钮
   - 悬停上移效果
   - 内含加号图标

2. 规则编辑按钮样式:
   - 文字按钮
   - 淡紫色
   - 右上角定位
   - 笔形图标

3. 规则区块样式:
   - 相对定位
   - 为编辑按钮提供定位参考

### 权限控制逻辑

```typescript
// 权限检查
const canEditRules = computed(() => {
  const permissions = userStore.permissions || []
  return permissions.includes('store:rules:update') || permissions.includes('*')
})
```

### 使用说明

1. 执行权限SQL脚本:
```bash
mysql -u root -p pindou_timer < backend/sql/20260406/03_店铺规则权限.sql
```

2. 重启后端服务

3. 前端自动检查权限:
   - 有 `store:rules:update` 权限的用户可以看到编辑按钮
   - 点击"新增规则"创建新规则
   - 点击每条规则右上角的"编辑"按钮修改内容

4. 编辑规则:
   - 选择分类和类型
   - 输入标题
   - 编辑内容（HTML或JSON）
   - 设置排序号和启用状态
   - 点击保存

### 权限分配

| 角色 | 默认权限 | 说明 |
|------|----------|------|
| 超级管理员 | * | 所有权限 |
| 店长 | store:rules:update | 可编辑规则内容 |
| 店员 | - | 无编辑权限，只能查看 |

@author wuci
