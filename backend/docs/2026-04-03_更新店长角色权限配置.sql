-- =====================================================
-- 更新店长角色权限配置
-- 创建日期：2026-04-03
-- 说明：根据新的权限结构更新店长角色的权限配置
-- =====================================================

-- 查看当前店长角色的权限配置
SELECT
    id,
    name,
    code,
    permissions,
    status
FROM sys_role
WHERE code = 'role_manager';

-- 更新店长角色权限（包含工作台、桌台管理、订单管理、数据统计、会员管理、操作日志等）
-- 根据新的权限结构配置
UPDATE sys_role
SET permissions = JSON_ARRAY(
    -- 工作台
    'dashboard:view',
    -- 桌台管理
    'table:view',
    'table:delete',
    'table:category',
    'table:config',
    'table:start',
    'table:end',
    'table:reserve',
    'table:bill',
    'table:extend',
    'table:pause',
    -- 订单管理
    'order:view',
    'order:detail',
    'order:export',
    -- 数据统计
    'statistics:view',
    -- 会员管理
    'member:view',
    'member:create',
    'member:update',
    'member:delete',
    'member:recharge',
    'member:record',
    -- 会员等级
    'member:level:view',
    'member:level:init',
    'member:level:create',
    'member:level:update',
    'member:level:delete',
    -- 操作日志
    'log:view',
    'log:detail',
    'log:export'
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
