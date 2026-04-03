-- =====================================================
-- 权限配置表初始化SQL
-- 创建日期：2026-04-03
-- 说明：根据实际功能模块重新设计的权限配置
-- =====================================================

-- 清空现有权限配置（开发环境使用）
TRUNCATE TABLE sys_permission;

-- =====================================================
-- 1. 工作台模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('dashboard', '0', 'dashboard:view', '工作台', 'module', 'Odometer', '/dashboard', 1, 1, 1, '工作台模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 2. 桌台管理模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('table', '0', 'table:view', '桌台管理', 'module', 'Grid', '/tables', 2, 1, 1, '桌台管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('table_batch_delete', 'table', 'table:delete', '批量删除', 'permission', '', '', 1, 1, 1, '批量删除桌台', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('table_category', 'table', 'table:category', '分类管理', 'permission', '', '', 2, 1, 1, '管理桌台分类', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('table_config', 'table', 'table:config', '桌台配置', 'permission', '', '', 3, 1, 1, '配置桌台布局', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('table_start', 'table', 'table:start', '开始计时', 'permission', '', '', 4, 1, 1, '开始桌台计时', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('table_end', 'table', 'table:end', '结束计时', 'permission', '', '', 5, 1, 1, '结束桌台计时', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('table_reserve', 'table', 'table:reserve', '预定', 'permission', '', '', 6, 1, 1, '预定桌台', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('table_bill', 'table', 'table:bill', '结账', 'permission', '', '', 7, 1, 1, '桌台结账', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('table_extend', 'table', 'table:extend', '续费', 'permission', '', '', 8, 1, 1, '续费计时', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('table_pause', 'table', 'table:pause', '暂停', 'permission', '', '', 9, 1, 1, '暂停计时', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 3. 订单管理模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('order', '0', 'order:view', '订单管理', 'module', 'List', '/orders', 3, 1, 1, '订单管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('order_detail', 'order', 'order:detail', '详情查看', 'permission', '', '', 1, 1, 1, '查看订单详情', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('order_export', 'order', 'order:export', '导出订单', 'permission', '', '', 2, 1, 1, '导出订单数据', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 4. 数据统计模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('statistics', '0', 'statistics:view', '数据统计', 'module', 'TrendCharts', '/statistics', 4, 1, 1, '数据统计模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 5. 用户管理模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('user', '0', 'user:view', '用户管理', 'module', 'User', '/users', 5, 1, 1, '用户管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('user_create', 'user', 'user:create', '新增用户', 'permission', '', '', 1, 1, 1, '创建新用户', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('user_update', 'user', 'user:update', '编辑', 'permission', '', '', 2, 1, 1, '编辑用户信息', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('user_delete', 'user', 'user:delete', '删除', 'permission', '', '', 3, 1, 1, '删除用户', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('user_reset_password', 'user', 'user:resetPassword', '重置密码', 'permission', '', '', 4, 1, 1, '重置用户密码', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 6. 角色管理模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('role', '0', 'role:view', '角色管理', 'module', 'Avatar', '/roles', 6, 1, 1, '角色管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('role_create', 'role', 'role:create', '新增角色', 'permission', '', '', 1, 1, 1, '创建新角色', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('role_update', 'role', 'role:update', '编辑', 'permission', '', '', 2, 1, 1, '编辑角色信息', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('role_delete', 'role', 'role:delete', '删除', 'permission', '', '', 3, 1, 1, '删除角色', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 7. 操作日志模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('log', '0', 'log:view', '操作日志', 'module', 'Document', '/logs', 7, 1, 1, '操作日志模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('log_detail', 'log', 'log:detail', '详情查看', 'permission', '', '', 1, 1, 1, '查看日志详情', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('log_export', 'log', 'log:export', '导出日志', 'permission', '', '', 2, 1, 1, '导出日志数据', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 8. 会员管理模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('member', '0', 'member:view', '会员管理', 'module', 'UserFilled', '/members', 8, 1, 1, '会员管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('member_create', 'member', 'member:create', '新增会员', 'permission', '', '', 1, 1, 1, '创建新会员', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('member_update', 'member', 'member:update', '编辑', 'permission', '', '', 2, 1, 1, '编辑会员信息', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('member_delete', 'member', 'member:delete', '删除', 'permission', '', '', 3, 1, 1, '删除会员', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('member_recharge', 'member', 'member:recharge', '充值', 'permission', '', '', 4, 1, 1, '会员充值', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('member_record', 'member', 'member:record', '查看记录', 'permission', '', '', 5, 1, 1, '查看会员交易记录', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 9. 会员等级模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('member_level', '0', 'member:level:view', '会员等级', 'module', 'Star', '/member-levels', 9, 1, 1, '会员等级模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('member_level_init', 'member_level', 'member:level:init', '初始化默认等级', 'permission', '', '', 1, 1, 1, '初始化默认会员等级', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('member_level_create', 'member_level', 'member:level:create', '新增等级', 'permission', '', '', 2, 1, 1, '创建新等级', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('member_level_update', 'member_level', 'member:level:update', '编辑', 'permission', '', '', 3, 1, 1, '编辑等级信息', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('member_level_delete', 'member_level', 'member:level:delete', '删除', 'permission', '', '', 4, 1, 1, '删除等级', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 10. 系统设置模块
-- =====================================================
INSERT INTO sys_permission (id, parent_id, permission_key, permission_name, permission_type, icon, path, sort, status, is_built_in, description, created_at, updated_at) VALUES
('system', '0', 'system:view', '系统设置', 'module', 'Setting', '/settings', 10, 1, 1, '系统设置模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('system_rule', 'system', 'system:rule', '计费规则', 'permission', '', '', 1, 1, 1, '配置计费规则', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('system_remind', 'system', 'system:remind', '提醒设置', 'permission', '', '', 2, 1, 1, '配置提醒规则', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('system_param', 'system', 'system:param', '系统参数设置', 'permission', '', '', 3, 1, 1, '系统参数配置', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- =====================================================
-- 验证数据
-- =====================================================
SELECT
    permission_type AS '权限类型',
    permission_key AS '权限编码',
    permission_name AS '权限名称',
    COUNT(*) AS '数量'
FROM sys_permission
GROUP BY permission_type, permission_key, permission_name
ORDER BY permission_type, sort;

-- 统计
SELECT
    '模块权限' AS '类型',
    COUNT(*) AS '数量'
FROM sys_permission
WHERE permission_type = 'module'
UNION ALL
SELECT
    '操作权限' AS '类型',
    COUNT(*) AS '数量'
FROM sys_permission
WHERE permission_type = 'permission';
