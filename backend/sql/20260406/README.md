# 折扣设置功能 - 数据库更新脚本

## 版本信息
- **版本**: 1.0
- **日期**: 2026-04-06
- **数据库**: MySQL 8.0+

## 更新说明
本次更新新增折扣设置功能，支持添加、编辑、删除和启用/禁用折扣，并可设置固定折扣、会员折扣和活动折扣三种类型。

## 脚本列表

### 1. 建表脚本
- **文件**: `01_建表脚本.sql`
- **说明**: 创建折扣设置表 `biz_discount`

### 2. 初始化数据脚本
- **文件**: `02_初始化数据脚本.sql`
- **说明**:
  - 添加 `system:discount` 权限到 `sys_permission` 表
  - 插入默认折扣数据（全场9折、全场8折）
  - 更新店长角色权限，添加折扣设置权限

## 执行顺序
1. 先执行 `01_建表脚本.sql`
2. 再执行 `02_初始化数据脚本.sql`

## 表结构说明

### biz_discount (折扣设置表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) | 折扣ID（UUID） |
| name | VARCHAR(50) | 折扣名称 |
| type | TINYINT(1) | 折扣类型：1-固定折扣 2-会员折扣 3-活动折扣 |
| discount_rate | DECIMAL(4,3) | 折扣率（0.9表示9折） |
| min_amount | DECIMAL(10,2) | 最低消费金额 |
| max_discount | DECIMAL(10,2) | 最高优惠金额 |
| member_level_id | BIGINT | 会员等级ID |
| start_time | BIGINT | 开始时间（毫秒时间戳） |
| end_time | BIGINT | 结束时间（毫秒时间戳） |
| status | TINYINT(1) | 状态：0-禁用，1-启用 |
| sort | INT | 排序 |
| description | VARCHAR(200) | 描述 |
| created_at | BIGINT | 创建时间 |
| updated_at | BIGINT | 更新时间 |
| deleted_at | BIGINT | 删除标记 |

## 权限说明
- **权限编码**: `system:discount`
- **权限名称**: 折扣设置
- **所属模块**: 系统设置 (system)
- **默认分配**: 店长角色

## 回滚说明
如需回滚，执行以下SQL：
```sql
DELETE FROM sys_permission WHERE permission_key = 'system:discount';
UPDATE sys_role SET permissions = JSON_REMOVE(permissions, JSON_UNQUOTE(JSON_SEARCH(permissions, 'one', 'system:discount'))) WHERE code = 'manager';
DROP TABLE IF EXISTS biz_discount;
```
