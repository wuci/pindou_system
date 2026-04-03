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
-- 2. 权限表 (sys_permission)
-- =============================================
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

-- =============================================
-- 3. 角色表 (sys_role)
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
-- 4. 会员等级配置表 (biz_member_level)
-- =============================================
DROP TABLE IF EXISTS `biz_member_level`;

CREATE TABLE `biz_member_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `min_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最小累计金额',
  `max_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '最大累计金额（null表示无上限）',
  `discount_rate` DECIMAL(4,3) NOT NULL COMMENT '折扣率（0.9表示9折）',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_amount_range` (`min_amount`, `max_amount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级配置表';

-- =============================================
-- 5. 会员表 (biz_member)
-- =============================================
DROP TABLE IF EXISTS `biz_member`;

CREATE TABLE `biz_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `name` VARCHAR(50) NOT NULL COMMENT '会员名称',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '会员余额（元）',
  `level_id` BIGINT NOT NULL COMMENT '会员等级ID',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_total_amount` (`total_amount`),
  KEY `idx_balance` (`balance`),
  CONSTRAINT `fk_member_level` FOREIGN KEY (`level_id`) REFERENCES `biz_member_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- =============================================
-- 6. 桌台分类表 (biz_table_category)
-- =============================================
DROP TABLE IF EXISTS `biz_table_category`;

CREATE TABLE `biz_table_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '分类图标',
  `sort_order` INT(11) DEFAULT 0 COMMENT '排序号',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台分类表';

-- =============================================
-- 7. 桌台布局配置表 (biz_table_layout_config)
-- =============================================
DROP TABLE IF EXISTS `biz_table_layout_config`;

CREATE TABLE `biz_table_layout_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` BIGINT(20) NOT NULL COMMENT '分类ID，0表示全局默认布局',
  `config` TEXT NOT NULL COMMENT '布局配置JSON',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台布局配置表';

-- =============================================
-- 8. 桌台表 (biz_table)
-- =============================================
DROP TABLE IF EXISTS `biz_table`;

CREATE TABLE `biz_table` (
  `id` INT NOT NULL COMMENT '桌台编号（主键）',
  `name` VARCHAR(20) NOT NULL COMMENT '桌台名称',
  `category_id` BIGINT(20) DEFAULT NULL COMMENT '分类ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'idle' COMMENT '状态：idle=空闲 using=使用中 paused=暂停',
  `current_order_id` VARCHAR(36) DEFAULT NULL COMMENT '当前订单ID',
  `start_time` BIGINT DEFAULT NULL COMMENT '开始时间（毫秒时间戳）',
  `preset_duration` INT DEFAULT NULL COMMENT '预设时长（秒），null表示不设时长',
  `pause_accumulated` INT NOT NULL DEFAULT 0 COMMENT '累计暂停时长（秒）',
  `last_pause_time` BIGINT DEFAULT NULL COMMENT '最后暂停时间（毫秒时间戳）',
  `reminded` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已提醒：1=是 0=否',
  `remind_ignored` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '提醒是否被忽略：1=是 0=否',
  `reservation_status` VARCHAR(20) DEFAULT 'none' COMMENT '预定状态：none-未预定，reserved-已预定',
  `reservation_end_time` BIGINT DEFAULT NULL COMMENT '预定截止时间（毫秒时间戳）',
  `reservation_name` VARCHAR(100) DEFAULT NULL COMMENT '预订人姓名',
  `reservation_phone` VARCHAR(20) DEFAULT NULL COMMENT '预订人手机号',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_current_order` (`current_order_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_reservation_status` (`reservation_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='桌台表';

-- =============================================
-- 9. 订单表 (biz_order)
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
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active=进行中 completed=已完成 cancelled=已作废',
  `payment_method` VARCHAR(20) DEFAULT 'offline' COMMENT '支付方式:offline-线下,online-线上,balance-余额,combined-组合',
  `balance_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '余额支付金额',
  `other_payment_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '其他方式支付金额',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
  `amount_detail` JSON NOT NULL COMMENT '金额明细',
  `member_id` BIGINT DEFAULT NULL COMMENT '会员ID',
  `member_discount` DECIMAL(10,2) DEFAULT 1.00 COMMENT '会员折扣金额',
  `operator_id` VARCHAR(36) DEFAULT NULL COMMENT '操作员ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
  `paid_at` BIGINT DEFAULT NULL COMMENT '支付时间（毫秒时间戳）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_table_id` (`table_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_payment_method` (`payment_method`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 10. 充值记录表 (biz_recharge_record)
-- =============================================
DROP TABLE IF EXISTS `biz_recharge_record`;

CREATE TABLE `biz_recharge_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '充值记录ID',
  `member_id` BIGINT NOT NULL COMMENT '会员ID',
  `member_name` VARCHAR(50) NOT NULL COMMENT '会员姓名（冗余）',
  `member_phone` VARCHAR(20) NOT NULL COMMENT '会员手机号（冗余）',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '充值金额',
  `balance_before` DECIMAL(10,2) NOT NULL COMMENT '充值前余额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '充值后余额',
  `payment_method` VARCHAR(20) NOT NULL DEFAULT 'cash' COMMENT '支付方式：cash-现金,wechat-微信,alipay-支付宝,card-刷卡',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `operator_id` VARCHAR(36) DEFAULT NULL COMMENT '操作员ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_recharge_member` FOREIGN KEY (`member_id`) REFERENCES `biz_member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员充值记录表';

-- =============================================
-- 11. 消费记录表 (biz_consumption_record)
-- =============================================
DROP TABLE IF EXISTS `biz_consumption_record`;

CREATE TABLE `biz_consumption_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消费记录ID',
  `member_id` BIGINT NOT NULL COMMENT '会员ID',
  `member_name` VARCHAR(50) NOT NULL COMMENT '会员姓名（冗余）',
  `member_phone` VARCHAR(20) NOT NULL COMMENT '会员手机号（冗余）',
  `order_id` VARCHAR(50) DEFAULT NULL COMMENT '关联订单ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '消费金额',
  `balance_before` DECIMAL(10,2) NOT NULL COMMENT '消费前余额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '消费后余额',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_consumption_member` FOREIGN KEY (`member_id`) REFERENCES `biz_member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员余额消费记录表';

-- =============================================
-- 12. 操作日志表 (sys_operation_log)
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
-- 13. 系统配置表 (sys_config)
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
-- 用户表外键
ALTER TABLE `sys_user` ADD CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`);

-- =============================================
-- 索引创建完成
-- =============================================
SET FOREIGN_KEY_CHECKS = 1;

SELECT '=====================================' AS '';
SELECT '建表脚本执行完成！' AS '';
SELECT '=====================================' AS '';
