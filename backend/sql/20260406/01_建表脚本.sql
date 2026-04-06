-- =============================================
-- 折扣设置表 - 建表脚本
-- 版本: 1.0
-- 日期: 2026-04-06
-- 数据库: MySQL 8.0+
-- =============================================

USE `pindou_timer`;

-- =============================================
-- 1. 折扣设置表 (biz_discount)
-- =============================================
DROP TABLE IF EXISTS `biz_discount`;

CREATE TABLE `biz_discount` (
  `id` VARCHAR(36) NOT NULL COMMENT '折扣ID（UUID）',
  `name` VARCHAR(50) NOT NULL COMMENT '折扣名称',
  `type` TINYINT(1) NOT NULL COMMENT '折扣类型：1-固定折扣 2-会员折扣 3-活动折扣',
  `discount_rate` DECIMAL(4,3) NOT NULL COMMENT '折扣率（0.9表示9折，1.0表示不打折）',
  `min_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '最低消费金额（null表示无限制）',
  `max_discount` DECIMAL(10,2) DEFAULT NULL COMMENT '最高优惠金额（null表示无限制）',
  `member_level_id` BIGINT DEFAULT NULL COMMENT '会员等级ID（null表示适用于所有会员）',
  `start_time` BIGINT DEFAULT NULL COMMENT '开始时间（毫秒时间戳，null表示立即生效）',
  `end_time` BIGINT DEFAULT NULL COMMENT '结束时间（毫秒时间戳，null表示永久有效）',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序（数值越小越靠前）',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `deleted_at` BIGINT DEFAULT NULL COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_member_level_id` (`member_level_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='折扣设置表';

-- =============================================
-- 执行完成提示
-- =============================================
SELECT '=====================================' AS '';
SELECT '折扣设置表创建完成！' AS '';
SELECT '=====================================' AS '';
