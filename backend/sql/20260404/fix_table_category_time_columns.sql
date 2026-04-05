-- =============================================
-- 修复桌台分类和桌台布局配置表时间字段类型
-- 问题：created_at 和 updated_at 字段类型应为 DATETIME 而非 BIGINT
-- 执行日期: 2026-04-04
-- =============================================

USE `pindou_timer`;

-- 1. 修复 biz_table_category 表的时间字段类型
-- 临时重命名列以避免数据丢失
ALTER TABLE `biz_table_category`
  CHANGE COLUMN `created_at` `created_at_old` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）- 临时',
  CHANGE COLUMN `updated_at` `updated_at_old` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）- 临时';

-- 添加新的 DATETIME 类型的列
ALTER TABLE `biz_table_category`
  ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `remark`,
  ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;

-- 将毫秒时间戳转换为 DATETIME（如果表中有数据）
-- 注意：如果表是新建的，created_at_old 可能是 0，需要特殊处理
UPDATE `biz_table_category`
SET
  `created_at` = IF(`created_at_old` = 0, NOW(), FROM_UNIXTIME(`created_at_old` / 1000)),
  `updated_at` = IF(`updated_at_old` = 0, NOW(), FROM_UNIXTIME(`updated_at_old` / 1000));

-- 删除临时列
ALTER TABLE `biz_table_category`
  DROP COLUMN `created_at_old`,
  DROP COLUMN `updated_at_old`;

-- 2. 修复 biz_table_layout_config 表的时间字段类型
ALTER TABLE `biz_table_layout_config`
  CHANGE COLUMN `created_at` `created_at_old` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）- 临时',
  CHANGE COLUMN `updated_at` `updated_at_old` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）- 临时';

-- 添加新的 DATETIME 类型的列
ALTER TABLE `biz_table_layout_config`
  ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `config`,
  ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;

-- 将毫秒时间戳转换为 DATETIME
UPDATE `biz_table_layout_config`
SET
  `created_at` = IF(`created_at_old` = 0, NOW(), FROM_UNIXTIME(`created_at_old` / 1000)),
  `updated_at` = IF(`updated_at_old` = 0, NOW(), FROM_UNIXTIME(`updated_at_old` / 1000));

-- 删除临时列
ALTER TABLE `biz_table_layout_config`
  DROP COLUMN `created_at_old`,
  DROP COLUMN `updated_at_old`;

-- =============================================
-- 验证表结构
-- =============================================
SELECT '=== biz_table_category 表结构 ===' AS '';
DESCRIBE `biz_table_category`;

SELECT '=== biz_table_layout_config 表结构 ===' AS '';
DESCRIBE `biz_table_layout_config`;

SELECT '=====================================' AS '';
SELECT '修复完成！' AS '';
SELECT '=====================================' AS '';
