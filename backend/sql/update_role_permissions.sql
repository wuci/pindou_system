-- =============================================
-- 更新店长角色权限 - 添加角色管理权限
-- 版本: 1.0
-- @author wuci
-- @date 2026-03-30
-- =============================================

USE `pindou_timer`;

-- 更新店长角色权限，添加 role:manage 权限
UPDATE `sys_role`
SET `permissions` = '["table:view","table:start","table:pause","table:end","table:config","order:view","order:detail","order:export","report:view","system:config","user:view","user:create","user:edit","role:manage","log:view","remind:ignore"]',
    `updated_at` = UNIX_TIMESTAMP() * 1000
WHERE `code` = 'manager';

-- 验证更新结果
SELECT `id`, `name`, `code`, `permissions`
FROM `sys_role`
WHERE `code` = 'manager';

SELECT '=====================================' AS '';
SELECT '店长角色权限更新完成！' AS '';
SELECT '=====================================' AS '';
