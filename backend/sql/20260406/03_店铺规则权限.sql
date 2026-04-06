-- =============================================
-- 店铺规则管理权限初始化
-- @author wuci
-- @date 2026-04-06
-- =============================================

-- =============================================
-- 1. 新增店铺规则管理模块权限
-- =============================================
INSERT INTO `sys_permission` (id, parent_id, permission_key, permission_name, permission_type, icon, `path`, sort, status, is_built_in, description, created_at, updated_at, deleted_at) VALUES
('store_rules', '0', 'store:rules:view', '店铺规则', 'module', 'Notebook', '/store-rules', 11, 1, 1, '店铺规则管理模块', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_view', 'store_rules', 'store:rules:view', '查看规则', 'permission', '', '', 1, 1, 1, '查看店铺规则列表', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_create', 'store_rules', 'store:rules:create', '新增规则', 'permission', '', '', 2, 1, 1, '创建新规则', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_update', 'store_rules', 'store:rules:update', '编辑规则', 'permission', '', '', 3, 1, 1, '编辑规则内容', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_delete', 'store_rules', 'store:rules:delete', '删除规则', 'permission', '', '', 4, 1, 1, '删除规则', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_toggle', 'store_rules', 'store:rules:toggle', '启用/禁用', 'permission', '', '', 5, 1, 1, '切换规则启用状态', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0);

-- =============================================
-- 2. 更新超级管理员权限 (已经是*，无需更新)
-- =============================================
-- 超级管理员拥有所有权限，无需更新

-- =============================================
-- 3. 更新店长角色权限
-- =============================================
UPDATE `sys_role`
SET `permissions` = JSON_ARRAY_PREPEND(`permissions`, 'store:rules:update')
WHERE `code` = 'manager' AND JSON_SEARCH(`permissions`, 'one', 'store:rules:update') IS NULL;

-- =============================================
-- 执行完成提示
-- =============================================
SELECT '=====================================' AS '';
SELECT '店铺规则权限初始化完成！' AS '';
SELECT '=====================================' AS '';

-- 数据统计
SELECT
  '新增模块' AS '数据类型',
  COUNT(*) AS '数量'
FROM sys_permission
WHERE id = 'store_rules'
UNION ALL
SELECT
  '新增权限',
  COUNT(*)
FROM sys_permission
WHERE parent_id = 'store_rules'
UNION ALL
SELECT
  '店长角色已添加权限',
  CASE
    WHEN JSON_SEARCH((SELECT permissions FROM sys_role WHERE code = 'manager'), 'one', 'store:rules:update') IS NOT NULL
    THEN 1
    ELSE 0
  END;
