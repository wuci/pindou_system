-- 为桌台表添加预定相关字段
-- 执行前请备份数据！

USE `pindou_timer`;

-- 添加预定相关字段到 biz_table 表
ALTER TABLE `biz_table`
ADD COLUMN `reservation_status` VARCHAR(20) DEFAULT 'none' COMMENT '预定状态：none-未预定，reserved-已预定' AFTER `remind_ignored`,
ADD COLUMN `reservation_end_time` BIGINT DEFAULT NULL COMMENT '预定截止时间（毫秒时间戳）' AFTER `reservation_status`,
ADD COLUMN `reservation_name` VARCHAR(100) DEFAULT NULL COMMENT '预订人姓名' AFTER `reservation_end_time`,
ADD COLUMN `reservation_phone` VARCHAR(20) DEFAULT NULL COMMENT '预订人手机号' AFTER `reservation_name`;

-- 添加索引
ALTER TABLE `biz_table`
ADD KEY `idx_reservation_status` (`reservation_status`);

-- 查看表结构确认
DESCRIBE `biz_table`;
