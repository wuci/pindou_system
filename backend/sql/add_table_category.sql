-- 桌台分类功能 SQL 迁移脚本
-- 执行日期: 2026-03-29

-- 1. 创建桌台分类表
CREATE TABLE IF NOT EXISTS `biz_table_category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '分类图标',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序号',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台分类表';

-- 2. 修改桌台表，添加分类字段
ALTER TABLE `biz_table`
ADD COLUMN `category_id` BIGINT(20) DEFAULT NULL COMMENT '分类ID' AFTER `name`,
ADD KEY `idx_category_id` (`category_id`);

-- 3. 插入默认分类数据
INSERT INTO `biz_table_category` (`name`, `icon`, `sort_order`, `remark`) VALUES
('大厅', 'grid', 1, '大厅区域桌台'),
('包间', 'office-building', 2, '包间桌台'),
('VIP区', 'star', 3, 'VIP专区桌台');

-- 4. 将现有桌台默认分配到"大厅"分类（ID=1）
UPDATE `biz_table` SET `category_id` = 1 WHERE `category_id` IS NULL;
