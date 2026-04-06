-- 订单表删除折扣字段
-- @author wuci
-- @date 2026-04-06
-- 说明：改用Redis存储折扣记录，不再在数据库中存储

-- 删除订单表中的折扣相关字段
ALTER TABLE `biz_order`
DROP COLUMN `discount_ids`,
DROP COLUMN `discount_count`,
DROP COLUMN `average_discount_rate`,
DROP COLUMN `total_discount_amount`;
