-- =====================================================
-- 修复店长角色权限配置 - 使其与前端菜单匹配
-- 创建日期：2026-04-03
-- 说明：修复店长角色权限，确保前端侧边栏菜单能正常显示
-- =====================================================

-- 查看当前店长角色的权限配置
SELECT
    id,
    name,
    code,
    JSON_PRETTY(permissions) as permissions,
    status
FROM sys_role
WHERE code = 'role_manager';

-- 更新店长角色权限，包含前端菜单所需的所有权限
UPDATE sys_role
SET permissions = JSON_ARRAY(
    -- 工作台
    'dashboard:view',
    -- 桌台管理（菜单显示 + 操作权限）
    'table:view',
    'table:category',
    'table:config',
    'table:start',
    'table:end',
    'table:reserve',
    'table:bill',
    'table:extend',
    'table:pause',
    'table:delete',
    -- 订单管理
    'order:view',
    'order:detail',
    'order:export',
    -- 数据统计（前端使用 statistics:view）
    'statistics:view',
    -- 用户管理
    'user:view',
    -- 角色管理
    'role:view',
    -- 操作日志
    'log:view',
    -- 会员管理
    'member:view',
    'member:create',
    'member:update',
    'member:delete',
    'member:recharge',
    'member:record',
    -- 会员等级
    'member:level:view',
    'member:level:create',
    'member:level:update',
    'member:level:delete',
    -- 系统设置
    'system:view',
    'system:rule'
),
updated_at = UNIX_TIMESTAMP() * 1000
WHERE code = 'role_manager';

-- 验证更新结果
SELECT
    id,
    name,
    code,
    JSON_PRETTY(permissions) as permissions,
    status
FROM sys_role
WHERE code = 'role_manager';

-- 验证店长账号能获取到正确的权限
SELECT
    u.id as user_id,
    u.username,
    u.nickname,
    r.name as role_name,
    r.code as role_code,
    JSON_PRETTY(r.permissions) as permissions
FROM sys_user u
LEFT JOIN sys_role r ON u.role_id = r.id
WHERE u.username = 'manager';
