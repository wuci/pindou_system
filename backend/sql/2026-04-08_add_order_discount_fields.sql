-- 为订单表添加活动折扣字段
-- @author wuci
-- @date 2026-04-08
-- 说明：开始计时时如果选择了活动折扣，将折扣信息存储到订单表中

USE `pindou_timer`;

-- 添加活动折扣相关字段
ALTER TABLE `biz_order`
ADD COLUMN `discount_id` VARCHAR(36) DEFAULT NULL COMMENT '活动折扣ID' AFTER `member_id`,
ADD COLUMN `discount_name` VARCHAR(100) DEFAULT NULL COMMENT '活动折扣名称' AFTER `discount_id`,
ADD COLUMN `discount_rate` DECIMAL(4,3) DEFAULT NULL COMMENT '活动折扣率（0.9表示9折）' AFTER `discount_name`,
ADD COLUMN `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '活动折扣金额' AFTER `discount_rate`;

-- 添加索引以便查询
ALTER TABLE `biz_order`
ADD KEY `idx_discount_id` (`discount_id`);

-- 执行完成提示
SELECT '=====================================' AS '';
SELECT '订单表折扣字段添加完成！' AS '';
SELECT '=====================================' AS '';
