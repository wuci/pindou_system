-- =============================================
-- 添加会员余额和充值功能
-- 版本: 1.0
-- 日期: 2026-03-31
-- =============================================

USE `pindou_timer`;

-- 1. 为会员表添加余额字段
ALTER TABLE `biz_member`
ADD COLUMN `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '会员余额（元）' AFTER `total_amount`,
ADD INDEX `idx_balance` (`balance`);

-- 2. 创建充值记录表
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
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_recharge_member` FOREIGN KEY (`member_id`) REFERENCES `biz_member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员充值记录表';

-- 3. 创建消费记录表（记录使用余额消费的记录）
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
-- 验证表结构
-- =============================================
SELECT '=== biz_member 表结构（新增balance字段）===' AS '';
DESCRIBE `biz_member`;

SELECT '=== biz_recharge_record 表结构 ===' AS '';
DESCRIBE `biz_recharge_record`;

SELECT '=== biz_consumption_record 表结构 ===' AS ';
DESCRIBE `biz_consumption_record`;
