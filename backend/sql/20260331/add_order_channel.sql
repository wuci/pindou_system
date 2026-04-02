-- 添加订单渠道字段
-- 用于支持不同渠道的计费规则

-- 添加 channel 字段到 biz_order 表
ALTER TABLE `biz_order` ADD COLUMN `channel` VARCHAR(50) NULL COMMENT '订餐渠道' AFTER `preset_duration`;

-- 为已存在的订单设置默认渠道值
UPDATE `biz_order` SET `channel` = 'store' WHERE `channel` IS NULL;

-- 添加索引以提高查询性能
ALTER TABLE `biz_order` ADD INDEX `idx_channel` (`channel`);
