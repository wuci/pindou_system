# 修改记录

## 日期
2026-04-06

## 修改内容
将店铺规则相关的建表和初始化数据同步到主SQL脚本目录

### 修改文件

#### 1. backend/sql/20260403/01_建表脚本.sql

**新增表定义**:
- 第15个表：`design_store_rules` (店铺规则表)

**表结构**:
```sql
CREATE TABLE `design_store_rules` (
  `id` VARCHAR(36) NOT NULL COMMENT '规则ID（UUID）',
  `category` VARCHAR(50) NOT NULL COMMENT '规则分类',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '规则标题',
  `content` TEXT DEFAULT NULL COMMENT '规则内容（HTML格式）',
  `rule_type` VARCHAR(50) DEFAULT NULL COMMENT '规则类型',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT DEFAULT NULL COMMENT '更新时间',
  `created_by` VARCHAR(36) DEFAULT NULL COMMENT '创建人ID',
  `updated_by` VARCHAR(36) DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_enabled` (`is_enabled`),
  KEY `idx_sort` (`sort_order`)
)
```

#### 2. backend/sql/20260403/02_初始化数据脚本.sql

**修改内容**:

1. **TRUNCATE表列表** - 添加 `design_store_rules`

2. **权限数据** - 添加店铺规则模块权限：
   - `store_rules` - 店铺规则模块
   - `store_rules_view` - 查看规则
   - `store_rules_create` - 新增规则
   - `store_rules_update` - 编辑规则
   - `store_rules_delete` - 删除规则
   - `store_rules_toggle` - 启用/禁用

3. **角色权限** - 更新店长角色权限，添加 `store:rules:update`

4. **初始规则数据** - 添加15条默认规则：
   - 套餐规则：3条（基础套餐表格、计费说明、优惠与会员）
   - 增值服务：4条（免费服务、付费服务表格、温柔帮烫、时光胶囊）
   - 安全须知：4条（安全警告、高温烫伤、小零件风险、呼吸道健康）
   - 其他规定：4条（预约占座、退改规则、禁止事项、财物保管）

5. **数据统计** - 添加店铺规则表统计

### 删除文件

以下文件已被主SQL脚本替代，可保留作为参考：
- `backend/sql/20260406/02_店铺规则表.sql`
- `backend/sql/20260406/03_店铺规则权限.sql`

### 使用说明

全新部署时，只需执行两个主SQL脚本：
```bash
# 1. 建表
mysql -u root -p < backend/sql/20260403/01_建表脚本.sql

# 2. 初始化数据
mysql -u root -p < backend/sql/20260403/02_初始化数据脚本.sql
```

### 版本兼容性

此更新确保：
- 新数据库直接获得完整表结构和数据
- 现有数据库可通过增量SQL更新
- 所有版本保持同步

@author wuci
