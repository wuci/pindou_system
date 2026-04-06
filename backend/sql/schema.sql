-- =============================================
-- 豆屿温柔集管理系统 - 数据库建表脚本
-- 版本: 1.0
-- 数据库: MySQL 8.0+
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `pindou_timer` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `pindou_timer`;

-- =============================================
-- 1. 用户表 (sys_user)
-- =============================================
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` VARCHAR(36) NOT NULL COMMENT '用户ID（UUID）',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `role_id` VARCHAR(36) NOT NULL COMMENT '角色ID',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=禁用',
  `last_login_at` BIGINT DEFAULT NULL COMMENT '最后登录时间（毫秒时间戳）',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `deleted_at` BIGINT DEFAULT NULL COMMENT '删除时间（软删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 角色表 (sys_role)
-- =============================================
DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` VARCHAR(36) NOT NULL COMMENT '角色ID',
  `name` VARCHAR(200) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `permissions` JSON NOT NULL COMMENT '权限列表',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_built_in` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否内置：0-否，1-是',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  `deleted_at` BIGINT DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`),
  KEY `idx_is_built_in` (`is_built_in`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =============================================
-- 3. 桌台表 (biz_table)
-- =============================================
DROP TABLE IF EXISTS `biz_table`;

CREATE TABLE `biz_table` (
  `id` INT NOT NULL COMMENT '桌台编号（主键）',
  `name` VARCHAR(20) NOT NULL COMMENT '桌台名称',
  `status` VARCHAR(20) NOT NULL DEFAULT 'idle' COMMENT '状态：idle=空闲 using=使用中 paused=暂停',
  `current_order_id` VARCHAR(36) DEFAULT NULL COMMENT '当前订单ID',
  `start_time` BIGINT DEFAULT NULL COMMENT '开始时间（毫秒时间戳）',
  `preset_duration` INT DEFAULT NULL COMMENT '预设时长（秒），null表示不设时长',
  `pause_accumulated` INT NOT NULL DEFAULT 0 COMMENT '累计暂停时长（秒）',
  `last_pause_time` BIGINT DEFAULT NULL COMMENT '最后暂停时间（毫秒时间戳）',
  `reminded` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已提醒：1=是 0=否',
  `remind_ignored` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '提醒是否被忽略：1=是 0=否',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_current_order` (`current_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='桌台表';

-- =============================================
-- 4. 订单表 (biz_order)
-- =============================================
DROP TABLE IF EXISTS `biz_order`;

CREATE TABLE `biz_order` (
  `id` VARCHAR(36) NOT NULL COMMENT '订单ID（UUID）',
  `table_id` INT NOT NULL COMMENT '桌台ID',
  `table_name` VARCHAR(20) NOT NULL COMMENT '桌台名称',
  `start_time` BIGINT NOT NULL COMMENT '开始时间（毫秒时间戳）',
  `end_time` BIGINT DEFAULT NULL COMMENT '结束时间（毫秒时间戳）',
  `duration` INT NOT NULL DEFAULT 0 COMMENT '总时长（秒）',
  `pause_duration` INT NOT NULL DEFAULT 0 COMMENT '暂停总时长（秒）',
  `preset_duration` INT DEFAULT NULL COMMENT '预设时长（秒）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active=进行中 completed=已完成 cancelled=已作废（使用时长<5分钟）',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
  `amount_detail` JSON NOT NULL COMMENT '金额明细',
  `operator_id` VARCHAR(36) DEFAULT NULL COMMENT '操作员ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
  `paid_at` BIGINT DEFAULT NULL COMMENT '支付时间（毫秒时间戳）',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_table_id` (`table_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 5. 操作日志表 (sys_operation_log)
-- =============================================
DROP TABLE IF EXISTS `sys_operation_log`;

CREATE TABLE `sys_operation_log` (
  `id` VARCHAR(36) NOT NULL COMMENT '日志ID（UUID）',
  `user_id` VARCHAR(36) NOT NULL COMMENT '操作用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `action` VARCHAR(50) NOT NULL COMMENT '操作编码',
  `action_name` VARCHAR(50) NOT NULL COMMENT '操作名称',
  `content` TEXT NOT NULL COMMENT '操作内容描述',
  `target_type` VARCHAR(20) DEFAULT NULL COMMENT '目标类型：table/order/user/role/config',
  `target_id` VARCHAR(36) DEFAULT NULL COMMENT '目标ID',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT '操作IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `execute_time` BIGINT DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_action` (`action`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 6. 系统配置表 (sys_config)
-- =============================================
DROP TABLE IF EXISTS `sys_config`;

CREATE TABLE `sys_config` (
  `id` VARCHAR(36) NOT NULL COMMENT '配置ID（UUID）',
  `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值（JSON字符串）',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `updated_by` VARCHAR(36) DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- =============================================
-- 创建外键约束
-- =============================================
ALTER TABLE `sys_user` ADD CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`);

-- =============================================
-- 索引创建完成
-- =============================================
