-- =============================================
-- 拼豆店计时管理系统 - 初始化数据脚本
-- 版本: 1.0
-- 日期: 2026-04-03
-- 数据库: MySQL 8.0+
-- 说明: 支持重复执行（先清空表再插入数据）
-- =============================================

USE `pindou_timer`;

-- 禁用外键检查，避免删除数据时因外键约束失败
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 清空所有表数据（按依赖关系顺序）
-- =============================================
TRUNCATE TABLE `biz_consumption_record`;
TRUNCATE TABLE `biz_recharge_record`;
TRUNCATE TABLE `biz_order`;
TRUNCATE TABLE `biz_table`;
TRUNCATE TABLE `biz_member`;
TRUNCATE TABLE `biz_table_layout_config`;
TRUNCATE TABLE `biz_table_category`;
TRUNCATE TABLE `biz_member_level`;
TRUNCATE TABLE `sys_operation_log`;
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `sys_role`;
TRUNCATE TABLE `sys_permission`;
TRUNCATE TABLE `sys_config`;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 1. 系统配置表 (sys_config)
-- =============================================
INSERT INTO `sys_config` (id, config_key, config_value, description, updated_at, updated_by) VALUES
('15e71bbe-2991-11f1-ad08-9c2dcd0c822f', 'table_count', '20', '桌台总数', 1774583853000, NULL),
('15efb8e4-2991-11f1-ad08-9c2dcd0c822f', 'billing_rule', '{"channels":[{"channel":"work_meal","channelName":"工作日套餐-线下","rules":[{"minutes":30,"price":7,"unlimited":false},{"minutes":60,"price":15,"unlimited":false},{"minutes":120,"price":28,"unlimited":false},{"minutes":240,"price":50,"unlimited":false},{"price":68,"unlimited":true}]},{"channel":"holiday_meal","channelName":"节假日套餐-线下","rules":[{"minutes":30,"price":9.9,"unlimited":false},{"minutes":60,"price":19,"unlimited":false},{"minutes":120,"price":35,"unlimited":false},{"minutes":240,"price":68,"unlimited":false},{"price":78,"unlimited":true}]},{"channel":"work_meal_meituan","channelName":"工作日套餐-美团","rules":[{"minutes":30,"price":6.5,"unlimited":false},{"minutes":60,"price":14.5,"unlimited":false},{"minutes":120,"price":25,"unlimited":false},{"minutes":240,"price":45,"unlimited":false},{"price":52,"unlimited":true}]},{"channel":"holiday_meal_meituan","channelName":"节假日套餐-美团","rules":[{"minutes":30,"price":8.8,"unlimited":false},{"minutes":60,"price":17,"unlimited":false},{"minutes":120,"price":30,"unlimited":false},{"minutes":240,"price":58,"unlimited":false},{"price":68,"unlimited":true}]},{"channel":"open_business","channelName":"开业大酬宾","rules":[{"minutes":60,"price":0,"unlimited":false},{"minutes":120,"price":0,"unlimited":false},{"price":0,"unlimited":true}]}]}', '计费规则', 1775200715261, NULL),
('15f7cbd5-2991-11f1-ad08-9c2dcd0c822f', 'remind_config', '{"threshold":5,"soundEnabled":true,"repeatInterval":30,"expiringCloseTime":3,"timeoutCloseTime":5}', '提醒配置', 1774583854000, NULL),
('1601f07b-2991-11f1-ad08-9c2dcd0c822f', 'session_timeout', '1800', '会话超时时间（秒）', 1774583854000, NULL),
('a66d514bcdfb45afb8a670b30d32aae0', 'system_config', '{"extendTime":15,"invalidOrderTime":5}', NULL, 1775195885461, NULL);

-- =============================================
-- 2. 权限表 (sys_permission) - 模块权限
-- =============================================
INSERT INTO `sys_permission` (id, parent_id, permission_key, permission_name, permission_type, icon, `path`, sort, status, is_built_in, description, created_at, updated_at, deleted_at) VALUES
('dashboard', '0', 'dashboard:view', '工作台', 'module', 'Odometer', '/dashboard', 1, 1, 1, '工作台模块', 1775205986000, 1775205986000, 0),
('table', '0', 'table:view', '桌台管理', 'module', 'Grid', '/tables', 2, 1, 1, '桌台管理模块', 1775205986000, 1775205986000, 0),
('order', '0', 'order:view', '订单管理', 'module', 'List', '/orders', 3, 1, 1, '订单管理模块', 1775205986000, 1775205986000, 0),
('statistics', '0', 'statistics:view', '数据统计', 'module', 'TrendCharts', '/statistics', 4, 1, 1, '数据统计模块', 1775205986000, 1775205986000, 0),
('user', '0', 'user:view', '用户管理', 'module', 'User', '/users', 5, 1, 1, '用户管理模块', 1775205986000, 1775205986000, 0),
('role', '0', 'role:view', '角色管理', 'module', 'Avatar', '/roles', 6, 1, 1, '角色管理模块', 1775205986000, 1775205986000, 0),
('log', '0', 'log:view', '操作日志', 'module', 'Document', '/logs', 7, 1, 1, '操作日志模块', 1775205986000, 1775205986000, 0),
('member', '0', 'member:view', '会员管理', 'module', 'UserFilled', '/members', 8, 1, 1, '会员管理模块', 1775205986000, 1775205986000, 0),
('member_level', '0', 'member:level:view', '会员等级', 'module', 'Star', '/member-levels', 9, 1, 1, '会员等级模块', 1775205986000, 1775205986000, 0),
('system', '0', 'system:view', '系统设置', 'module', 'Setting', '/settings', 10, 1, 1, '系统设置模块', 1775205986000, 1775205986000, 0),
('table_batch_delete', 'table', 'table:delete', '批量删除', 'permission', '', '', 1, 1, 1, '批量删除桌台', 1775205986000, 1775205986000, 0),
('table_category', 'table', 'table:category', '分类管理', 'permission', '', '', 2, 1, 1, '管理桌台分类', 1775205986000, 1775205986000, 0),
('table_config', 'table', 'table:config', '桌台配置', 'permission', '', '', 3, 1, 1, '配置桌台布局', 1775205986000, 1775205986000, 0),
('table_start', 'table', 'table:start', '开始计时', 'permission', '', '', 4, 1, 1, '开始桌台计时', 1775205986000, 1775205986000, 0),
('table_end', 'table', 'table:end', '结束计时', 'permission', '', '', 5, 1, 1, '结束桌台计时', 1775205986000, 1775205986000, 0),
('table_reserve', 'table', 'table:reserve', '预定', 'permission', '', '', 6, 1, 1, '预定桌台', 1775205986000, 1775205986000, 0),
('table_bill', 'table', 'table:bill', '结账', 'permission', '', '', 7, 1, 1, '桌台结账', 1775205986000, 1775205986000, 0),
('table_extend', 'table', 'table:extend', '续费', 'permission', '', '', 8, 1, 1, '续费计时', 1775205986000, 1775205986000, 0),
('table_pause', 'table', 'table:pause', '暂停', 'permission', '', '', 9, 1, 1, '暂停计时', 1775205986000, 1775205986000, 0),
('order_detail', 'order', 'order:detail', '详情查看', 'permission', '', '', 1, 1, 1, '查看订单详情', 1775205986000, 1775205986000, 0),
('order_export', 'order', 'order:export', '导出订单', 'permission', '', '', 2, 1, 1, '导出订单数据', 1775205986000, 1775205986000, 0),
('user_create', 'user', 'user:create', '新增用户', 'permission', '', '', 1, 1, 1, '创建新用户', 1775205986000, 1775205986000, 0),
('user_update', 'user', 'user:update', '编辑', 'permission', '', '', 2, 1, 1, '编辑用户信息', 1775205986000, 1775205986000, 0),
('user_delete', 'user', 'user:delete', '删除', 'permission', '', '', 3, 1, 1, '删除用户', 1775205986000, 1775205986000, 0),
('user_reset_password', 'user', 'user:resetPassword', '重置密码', 'permission', '', '', 4, 1, 1, '重置用户密码', 1775205986000, 1775205986000, 0),
('role_create', 'role', 'role:create', '新增角色', 'permission', '', '', 1, 1, 1, '创建新角色', 1775205986000, 1775205986000, 0),
('role_update', 'role', 'role:update', '编辑', 'permission', '', '', 2, 1, 1, '编辑角色信息', 1775205986000, 1775205986000, 0),
('role_delete', 'role', 'role:delete', '删除', 'permission', '', '', 3, 1, 1, '删除角色', 1775205986000, 1775205986000, 0),
('log_detail', 'log', 'log:detail', '详情查看', 'permission', '', '', 1, 1, 1, '查看日志详情', 1775205986000, 1775205986000, 0),
('log_export', 'log', 'log:export', '导出日志', 'permission', '', '', 2, 1, 1, '导出日志数据', 1775205986000, 1775205986000, 0),
('member_create', 'member', 'member:create', '新增会员', 'permission', '', '', 1, 1, 1, '创建新会员', 1775205986000, 1775205986000, 0),
('member_update', 'member', 'member:update', '编辑', 'permission', '', '', 2, 1, 1, '编辑会员信息', 1775205986000, 1775205986000, 0),
('member_delete', 'member', 'member:delete', '删除', 'permission', '', '', 3, 1, 1, '删除会员', 1775205986000, 1775205986000, 0),
('member_recharge', 'member', 'member:recharge', '充值', 'permission', '', '', 4, 1, 1, '会员充值', 1775205986000, 1775205986000, 0),
('member_record', 'member', 'member:record', '查看记录', 'permission', '', '', 5, 1, 1, '查看会员交易记录', 1775205986000, 1775205986000, 0),
('member_level_init', 'member_level', 'member:level:init', '初始化默认等级', 'permission', '', '', 1, 1, 1, '初始化默认会员等级', 1775205986000, 1775205986000, 0),
('member_level_create', 'member_level', 'member:level:create', '新增等级', 'permission', '', '', 2, 1, 1, '创建新等级', 1775205986000, 1775205986000, 0),
('member_level_update', 'member_level', 'member:level:update', '编辑', 'permission', '', '', 3, 1, 1, '编辑等级信息', 1775205986000, 1775205986000, 0),
('member_level_delete', 'member_level', 'member:level:delete', '删除', 'permission', '', '', 4, 1, 1, '删除等级', 1775205986000, 1775205986000, 0),
('system_rule', 'system', 'system:rule', '计费规则', 'permission', '', '', 1, 1, 1, '配置计费规则', 1775205986000, 1775205986000, 0),
('system_remind', 'system', 'system:remind', '提醒设置', 'permission', '', '', 2, 1, 1, '配置提醒规则', 1775205986000, 1775205986000, 0),
('system_param', 'system', 'system:param', '系统参数设置', 'permission', '', '', 3, 1, 1, '系统参数配置', 1775205986000, 1775205986000, 0);

-- =============================================
-- 3. 角色表 (sys_role)
-- =============================================
INSERT INTO `sys_role` (id, name, code, permissions, sort, status, is_built_in, description, created_at, updated_at, deleted_at) VALUES
('a0000000-0000-0000-0000-000000000001', '超级管理员', 'super_admin', '["*"]', 1, 1, 1, '系统最高权限', 1774583841000, 1774583841000, 0),
('a0000000-0000-0000-0000-000000000002', '店长', 'manager', '["dashboard:view", "table:category", "table:config", "table:start", "table:end", "table:reserve", "table:bill", "table:extend", "table:pause", "order:view", "order:detail", "order:export", "statistics:view", "member:create", "member:update", "member:recharge", "member:record", "system:rule"]', 2, 1, 1, '管理权限', 1774583841000, 1774962920000, 0),
('a0000000-0000-0000-0000-000000000003', '店员', 'staff', '["dashboard-module", "dashboard:view", "table:view", "table:start", "table:pause", "table:resume", "table:extend", "table:end", "table:ignore", "member:view", "member:create", "member:update", "member:recharge"]', 3, 1, 1, '操作权限', 1774583842000, 1774962920000, 0),
('a0000000-0000-0000-0000-000000000004', '只读用户', 'readonly', '["table:view", "order:view", "order:detail", "report:view"]', 4, 1, 1, '查看权限', 1774583842000, 1774583842000, 0);

-- =============================================
-- 4. 用户表 (sys_user)
-- 说明：默认密码为 123456（BCrypt加密）
-- =============================================
INSERT INTO `sys_user` (id, username, password, nickname, role_id, status, last_login_at, last_login_ip, created_at, updated_at, deleted_at) VALUES
('11b27ce8-2991-11f1-ad08-9c2dcd0c822f', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 'a0000000-0000-0000-0000-000000000001', 1, NULL, NULL, 1774583846000, 1774583846000, 0),
('11be7a22-2991-11f1-ad08-9c2dcd0c822f', 'manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '店长', 'a0000000-0000-0000-0000-000000000002', 1, NULL, NULL, 1774583846000, 1774583846000, 0),
('11c930ea-2991-11f1-ad08-9c2dcd0c822f', 'staff', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '店员', 'a0000000-0000-0000-0000-000000000003', 1, NULL, NULL, 1774583847000, 1774583847000, 0);

-- =============================================
-- 5. 桌台分类表 (biz_table_category)
-- =============================================
INSERT INTO `biz_table_category` (id, name, icon, sort_order, remark, created_at, updated_at) VALUES
(1, '大厅', 'grid', 1, '大厅区域桌台', 1774596555000, 1774596555000),
(2, '包间', 'office-building', 2, '包间桌台', 1774596555000, 1774596555000);

-- =============================================
-- 6. 会员等级配置表 (biz_member_level)
-- =============================================
INSERT INTO `biz_member_level` (id, name, min_amount, max_amount, discount_rate, sort, created_at, updated_at) VALUES
(1, '豆豆萌新', 0.00, 300.00, 0.950, 1, 1774959221249, 1774959221249),
(2, '熨烫能手', 300.01, 1000.00, 0.900, 2, 1774959221249, 1774959221249),
(3, '像素匠人', 1000.01, 3000.00, 0.850, 3, 1774959221249, 1774959221249),
(4, '熔豆典藏', 3000.01, NULL, 0.800, 4, 1774959221249, 1774959221249);

-- =============================================
-- 7. 桌台表 (biz_table)
-- 说明：默认创建20个桌台，分配到"大厅"分类
-- =============================================
INSERT INTO `biz_table` (id, name, category_id, status, current_order_id, start_time, preset_duration, pause_accumulated, last_pause_time, reminded, remind_ignored, created_at, updated_at, reservation_status, reservation_end_time, reservation_name, reservation_phone) VALUES
(1, '桌台1', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556028, 1774596556028, 'none', NULL, NULL, NULL),
(2, '桌台2', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556041, 1774596556041, 'none', NULL, NULL, NULL),
(3, '桌台3', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556042, 1774596556042, 'none', NULL, NULL, NULL),
(4, '桌台4', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556043, 1774596556043, 'none', NULL, NULL, NULL),
(5, '桌台5', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556044, 1774596556044, 'none', NULL, NULL, NULL),
(6, '桌台6', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556045, 1774596556045, 'none', NULL, NULL, NULL),
(7, '桌台7', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556046, 1774596556046, 'none', NULL, NULL, NULL),
(8, '桌台8', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556048, 1774596556048, 'none', NULL, NULL, NULL),
(9, '桌台9', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556051, 1774596556051, 'none', NULL, NULL, NULL),
(10, '桌台10', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556052, 1774596556052, 'none', NULL, NULL, NULL),
(11, '桌台11', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556053, 1774596556053, 'none', NULL, NULL, NULL),
(12, '桌台12', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556054, 1774596556054, 'none', NULL, NULL, NULL),
(13, '桌台13', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556055, 1774596556055, 'none', NULL, NULL, NULL),
(14, '桌台14', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556056, 1774596556056, 'none', NULL, NULL, NULL),
(15, '桌台15', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556057, 1774596556057, 'none', NULL, NULL, NULL),
(16, '桌台16', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556058, 1774596556058, 'none', NULL, NULL, NULL),
(17, '桌台17', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556059, 1774596556059, 'none', NULL, NULL, NULL),
(18, '桌台18', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556060, 1774596556060, 'none', NULL, NULL, NULL),
(19, '桌台19', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556061, 1774596556061, 'none', NULL, NULL, NULL),
(20, '桌台20', 1, 'idle', NULL, NULL, NULL, 0, NULL, 0, 0, 1774596556062, 1774596556062, 'none', NULL, NULL, NULL);

-- =============================================
-- 执行完成提示
-- =============================================
SELECT '=====================================' AS '';
SELECT '初始化数据脚本执行完成！' AS '';
SELECT '=====================================' AS '';

-- =============================================
-- 数据统计
-- =============================================
SELECT
  '系统配置' AS '数据类型',
  COUNT(*) AS '数量'
FROM sys_config
UNION ALL
SELECT
  '权限',
  COUNT(*)
FROM sys_permission
UNION ALL
SELECT
  '角色',
  COUNT(*)
FROM sys_role
UNION ALL
SELECT
  '用户',
  COUNT(*)
FROM sys_user
UNION ALL
SELECT
  '桌台分类',
  COUNT(*)
FROM biz_table_category
UNION ALL
SELECT
  '桌台',
  COUNT(*)
FROM biz_table
UNION ALL
SELECT
  '会员等级',
  COUNT(*)
FROM biz_member_level;
