-- =============================================
-- 添加会员管理权限到角色
-- 版本: 1.0
-- 日期: 2026-03-31
-- =============================================

USE `pindou_timer`;

-- 更新店长角色 - 添加会员管理权限
UPDATE `sys_role`
SET `permissions` = '["table:view","table:start","table:pause","table:end","table:config","order:view","order:detail","order:export","report:view","system:config","user:view","user:create","user:edit","role:manage","log:view","remind:ignore","member:view","member:create","member:edit","member:delete","member:level"]',
    `updated_at` = UNIX_TIMESTAMP() * 1000
WHERE `code` = 'manager';

-- 更新店员角色 - 添加会员查看权限
UPDATE `sys_role`
SET `permissions` = '["table:view","table:start","table:pause","table:end","order:view","order:detail","report:view","log:view","remind:ignore","member:view"]',
    `updated_at` = UNIX_TIMESTAMP() * 1000
WHERE `code` = 'staff';

-- =============================================
-- 验证更新
-- =============================================
SELECT '=== 更新后的角色权限 ===' AS '';
SELECT `code`, `name`, `permissions`
FROM `sys_role`
WHERE `code` IN ('manager', 'staff')
ORDER BY `sort`;
