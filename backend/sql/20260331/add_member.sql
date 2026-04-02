-- =============================================
-- 会员表
-- 版本: 1.0
-- 日期: 2026-03-31
-- =============================================

DROP TABLE IF EXISTS `biz_member`;

CREATE TABLE `biz_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `name` VARCHAR(50) NOT NULL COMMENT '会员名称',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
  `level_id` BIGINT NOT NULL COMMENT '会员等级ID',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_total_amount` (`total_amount`),
  CONSTRAINT `fk_member_level` FOREIGN KEY (`level_id`) REFERENCES `biz_member_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';
