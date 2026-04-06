# 修改记录

## 日期
2026-04-06

## 修改内容
店铺规则管理功能 - 支持从数据库动态获取和修改规则内容

### 后端文件

#### 1. 数据库表结构
**文件**: `backend/sql/20260406/02_店铺规则表.sql`

创建 `design_store_rules` 表，包含字段：
- `id` - 规则ID（UUID）
- `category` - 规则分类（packages/services/safety/other）
- `title` - 规则标题
- `content` - 规则内容（HTML格式或JSON数组）
- `rule_type` - 规则类型（table/list/warning/special）
- `sort_order` - 排序号
- `is_enabled` - 是否启用
- 时间戳和创建人字段

#### 2. 实体类
**文件**: `backend/pdsystem-entity/src/main/java/com/pindou/timer/entity/DesignStoreRules.java`

#### 3. DTO类
**文件**:
- `backend/pdsystem-dto/src/main/java/com/pindou/timer/dto/DesignStoreRulesRequest.java`
- `backend/pdsystem-dto/src/main/java/com/pindou/timer/dto/DesignStoreRulesResponse.java`

#### 4. Mapper接口
**文件**: `backend/pdsystem-service/src/main/java/com/pindou/timer/mapper/DesignStoreRulesMapper.java`

#### 5. Service层
**文件**:
- `backend/pdsystem-service/src/main/java/com/pindou/timer/service/DesignStoreRulesService.java`
- `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/DesignStoreRulesServiceImpl.java`

提供方法：
- `getRulesByCategory(category)` - 根据分类获取规则列表
- `getRuleById(id)` - 根据ID获取规则
- `createRule(request)` - 创建规则
- `updateRule(request)` - 更新规则
- `deleteRule(id)` - 删除规则
- `toggleRuleEnabled(id)` - 切换启用状态

#### 6. Controller
**文件**: `backend/pdsystem-server/src/main/java/com/pindou/timer/controller/DesignStoreRulesController.java`

API接口：
- `GET /api/store-rules` - 获取规则列表
- `GET /api/store-rules/{id}` - 获取单个规则
- `POST /api/store-rules` - 创建规则
- `PUT /api/store-rules` - 更新规则
- `DELETE /api/store-rules/{id}` - 删除规则
- `PUT /api/store-rules/{id}/toggle` - 切换启用状态

### 前端文件

#### 1. API文件
**文件**: `frontend/src/api/storeRules.ts`

导出：
- `RuleCategory` 枚举
- `RuleType` 枚举
- `StoreRule` 接口
- `StoreRuleRequest` 接口
- API调用方法

#### 2. Dashboard组件更新
**文件**: `frontend/src/views/Dashboard/index.vue`

**主要变更**：
- 引入 `getRulesByCategory` API
- 添加 `rulesData` 响应式数据存储规则
- 使用 `computed` 解析表格数据（套餐、服务）
- 动态渲染规则内容（根据 `ruleType` 判断显示方式）
- 添加 `html-content` 样式类支持HTML内容渲染
- 添加 `loading` 状态和 `v-loading` 指令

### 规则类型说明

| rule_type | 说明 | 渲染方式 | 示例 |
|-----------|------|----------|------|
| table | 表格数据 | content为JSON数组，用el-table渲染 | 基础套餐、付费服务 |
| list | 列表 | content为HTML的ul/li，用v-html渲染 | 计费说明、免费服务 |
| warning | 警告框 | 特殊样式的警告框 | 安全警告 |
| special | 特色服务 | 带图标和特殊样式的卡片 | 温柔帮烫、时光胶囊 |

### 规则分类说明

| category | 中文名称 | 选项卡标签 |
|----------|----------|------------|
| packages | 套餐规则 | 套餐规则 |
| services | 增值服务 | 增值服务 |
| safety | 安全须知 | 安全须知 |
| other | 其他规定 | 其他规定 |

### 初始数据

SQL文件包含15条初始规则数据：
- 套餐规则：3条（基础套餐表格、计费说明、优惠与会员）
- 增值服务：4条（免费服务、付费服务表格、温柔帮烫、时光胶囊）
- 安全须知：4条（安全警告、高温烫伤、小零件风险、呼吸道健康）
- 其他规定：4条（预约与占座、退改规则、禁止事项、财物保管）

### 使用说明

#### 管理员修改规则
1. 通过API调用修改规则内容
2. 支持富文本HTML格式
3. 支持JSON数组格式（表格数据）
4. 可以启用/禁用规则
5. 可以调整排序

#### 前端展示
1. Dashboard组件在mounted时加载规则
2. 根据ruleType自动选择渲染方式
3. HTML内容使用v-html渲染
4. 表格数据先JSON.parse再用el-table渲染

### 后续扩展

可在此基础上创建：
- 规则管理页面（CRUD操作界面）
- 富文本编辑器
- 拖拽排序功能
- 版本历史记录

@author wuci
