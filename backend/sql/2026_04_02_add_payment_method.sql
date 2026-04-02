-- 为订单表添加支付方式相关字段
USE `pindou_timer`;

ALTER TABLE `biz_order`
ADD COLUMN `payment_method` VARCHAR(20) DEFAULT 'offline' COMMENT '支付方式:offline-线下,online-线上,balance-余额,combined-组合' AFTER `status`,
ADD COLUMN `balance_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '余额支付金额' AFTER `payment_method`,
ADD COLUMN `other_payment_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '其他方式支付金额' AFTER `balance_amount`;

-- 添加索引
ALTER TABLE `biz_order`
ADD KEY `idx_payment_method` (`payment_method`);

-- 查看表结构确认
DESCRIBE `biz_order`;
