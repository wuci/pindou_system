-- 桌台布局配置表
CREATE TABLE IF NOT EXISTS `biz_table_layout_config` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `category_id` BIGINT(20) NOT NULL COMMENT '分类ID，0表示全局默认布局',
    `config` TEXT NOT NULL COMMENT '布局配置JSON',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台布局配置表';
