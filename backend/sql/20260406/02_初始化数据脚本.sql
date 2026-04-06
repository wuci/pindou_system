-- =============================================
-- 折扣设置功能 - 初始化数据脚本
-- 版本: 1.0
-- 日期: 2026-04-06
-- 数据库: MySQL 8.0+
-- =============================================

USE `pindou_timer`;

-- =============================================
-- 1. 插入权限配置 (sys_permission)
-- =============================================
INSERT INTO `sys_permission` (id, parent_id, permission_key, permission_name, permission_type, icon, `path`, sort, status, is_built_in, description, created_at, updated_at, deleted_at) VALUES
('system_discount', 'system', 'system:discount', '折扣设置', 'permission', '', '', 4, 1, 1, '配置折扣规则', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0);

-- =============================================
-- 2. 插入默认折扣数据 (biz_discount)
-- =============================================
INSERT INTO `biz_discount` (`id`, `name`, `type`, `discount_rate`, `min_amount`, `status`, `sort`, `description`, `created_at`, `updated_at`, `deleted_at`) VALUES
(UUID(), '全场9折', 1, 0.900, NULL, 1, 1, '全场通用9折优惠', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
(UUID(), '全场8折', 1, 0.800, NULL, 0, 2, '全场通用8折优惠', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0);

-- =============================================
-- 3. 更新店长角色权限 (sys_role)
-- =============================================
UPDATE `sys_role`
SET `permissions` = JSON_ARRAY_PREPEND(`permissions`, 'system:discount')
WHERE `code` = 'manager' AND JSON_SEARCH(`permissions`, 'one', 'system:discount') IS NULL;

-- =============================================
-- 执行完成提示
-- =============================================
SELECT '=====================================' AS '';
SELECT '折扣设置功能初始化数据完成！' AS '';
SELECT '=====================================' AS '';

-- 数据统计
SELECT
  '新增权限' AS '数据类型',
  COUNT(*) AS '数量'
FROM sys_permission
WHERE permission_key = 'system:discount'
UNION ALL
SELECT
  '默认折扣',
  COUNT(*)
FROM biz_discount;
