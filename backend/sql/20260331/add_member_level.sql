-- =============================================
-- 会员等级配置表
-- 版本: 1.0
-- 日期: 2026-03-31
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
