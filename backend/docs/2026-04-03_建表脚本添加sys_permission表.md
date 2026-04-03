# 建表脚本添加sys_permission表

**日期：** 2026-04-03
**操作人：** wuci
**文件：** backend/sql/20260403/01_建表脚本.sql

## 问题描述

初始化数据脚本中包含 `sys_permission` 表的数据插入（48条权限记录），但建表脚本中缺少该表的定义。

## 修复内容

### 添加 sys_permission 表

```sql
DROP TABLE IF EXISTS `sys_permission`;

CREATE TABLE `sys_permission` (
  `id` VARCHAR(36) NOT NULL COMMENT '权限ID',
  `parent_id` VARCHAR(36) DEFAULT '0' COMMENT '父权限ID',
  `permission_key` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `permission_type` VARCHAR(20) NOT NULL COMMENT '权限类型：module-模块，permission-权限',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_built_in` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否内置：0-否，1-是',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `deleted_at` BIGINT DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_permission_key` (`permission_key`),
  KEY `idx_permission_type` (`permission_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';
```

### 调整表序号

由于在角色表前新增了权限表，将后续所有表的序号依次后移：
- 原2-12号表 → 3-13号表

## 最终数据库表结构（共13张表）

### 系统管理表（5张）
1. **sys_user** - 用户表
2. **sys_permission** - 权限表
3. **sys_role** - 角色表
4. **sys_operation_log** - 操作日志表
5. **sys_config** - 系统配置表

### 业务表（8张）
6. **biz_member_level** - 会员等级配置表
7. **biz_member** - 会员表
8. **biz_table_category** - 桌台分类表
9. **biz_table_layout_config** - 桌台布局配置表
10. **biz_table** - 桌台表
11. **biz_order** - 订单表
12. **biz_recharge_record** - 充值记录表
13. **biz_consumption_record** - 消费记录表

## 验证

执行建表脚本后，数据库应包含完整的13张表，且初始化数据脚本能成功执行。

---
**整理人：** wuci
**日期：** 2026-04-03
