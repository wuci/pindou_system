-- =============================================
-- 订单表增加会员字段
-- 版本: 1.0
-- 日期: 2026-03-31
-- =============================================

USE `pindou_timer`;

-- 为 biz_order 表增加会员相关字段
ALTER TABLE `biz_order`
ADD COLUMN `member_id` BIGINT DEFAULT NULL COMMENT '会员ID' AFTER `operator_name`,
ADD COLUMN `original_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '原价（折扣前）' AFTER `amount`;

-- 添加会员ID索引
ALTER TABLE `biz_order`
ADD INDEX `idx_member_id` (`member_id`);
