-- 更新订单表状态字段注释
-- 添加 cancelled 状态说明
ALTER TABLE `biz_order` MODIFY COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active=进行中 completed=已完成 cancelled=已作废（使用时长<5分钟）';
