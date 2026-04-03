-- ===================================================
-- 权限配置表初始化脚本（安全版）
-- 创建日期：2026-04-03
-- 说明：使用 INSERT IGNORE，跳过已存在的数据
-- ===================================================

-- 1. 确保表存在
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` VARCHAR(32) NOT NULL COMMENT '权限ID（UUID）',
    `parent_id` VARCHAR(32) NOT NULL DEFAULT 'root' COMMENT '父权限ID，顶级节点为root',
    `permission_key` VARCHAR(100) NOT NULL COMMENT '权限编码（唯一标识，如 table:view）',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称（显示名称）',
    `permission_type` VARCHAR(20) NOT NULL COMMENT '权限类型：module-模块，permission-权限项',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标名称',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径（可选）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_built_in` TINYINT NOT NULL DEFAULT 0 COMMENT '是否内置：0-否，1-是（内置权限不可删除）',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted_at` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_key` (`permission_key`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限配置表';

-- 2. 使用 INSERT IGNORE 跳过已存在的数据
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `permission_key`, `permission_name`, `permission_type`, `icon`, `path`, `sort`, `status`, `is_built_in`, `description`, `created_at`, `updated_at`) VALUES
-- 工作台
('1001', 'root', 'dashboard-module', '工作台', 'module', 'Odometer', '/dashboard', 1, 1, 1, '工作台模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('1002', '1001', 'dashboard:view', '查看工作台', 'permission', NULL, NULL, 1, 1, 1, '查看工作台权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 桌台管理
('2001', 'root', 'table-module', '桌台管理', 'module', 'Grid', '/tables', 2, 1, 1, '桌台管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('2002', '2001', 'table:view', '查看桌台', 'permission', NULL, NULL, 1, 1, 1, '查看桌台权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('2003', '2001', 'table:update', '编辑桌台', 'permission', NULL, NULL, 2, 1, 1, '编辑桌台信息权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('2004', '2001', 'table:start', '开始计时', 'permission', NULL, NULL, 3, 1, 1, '开始计时权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('2005', '2001', 'table:pause', '暂停计时', 'permission', NULL, NULL, 4, 1, 1, '暂停计时权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('2006', '2001', 'table:resume', '恢复计时', 'permission', NULL, NULL, 5, 1, 1, '恢复计时权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('2007', '2001', 'table:extend', '续费时长', 'permission', NULL, NULL, 6, 1, 1, '续费时长权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('2008', '2001', 'table:end', '结束计时', 'permission', NULL, NULL, 7, 1, 1, '结束计时权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('2009', '2001', 'table:ignore', '忽略提醒', 'permission', NULL, NULL, 8, 1, 1, '忽略提醒权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('2010', '2001', 'table:delete', '删除桌台', 'permission', NULL, NULL, 9, 1, 1, '删除桌台权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 订单管理
('3001', 'root', 'order-module', '订单管理', 'module', 'Document', '/orders', 3, 1, 1, '订单管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('3002', '3001', 'order:view', '查看订单', 'permission', NULL, NULL, 1, 1, 1, '查看订单权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('3003', '3001', 'order:create', '创建订单', 'permission', NULL, NULL, 2, 1, 1, '创建订单权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('3004', '3001', 'order:update', '编辑订单', 'permission', NULL, NULL, 3, 1, 1, '编辑订单权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('3005', '3001', 'order:delete', '删除订单', 'permission', NULL, NULL, 4, 1, 1, '删除订单权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 数据统计
('4001', 'root', 'report-module', '数据统计', 'module', 'DataAnalysis', '/reports', 4, 1, 1, '数据统计模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('4002', '4001', 'report:view', '查看统计', 'permission', NULL, NULL, 1, 1, 1, '查看统计权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 用户管理
('5001', 'root', 'user-module', '用户管理', 'module', 'User', '/users', 5, 1, 1, '用户管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('5002', '5001', 'user:view', '查看用户', 'permission', NULL, NULL, 1, 1, 1, '查看用户权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('5003', '5001', 'user:create', '新增用户', 'permission', NULL, NULL, 2, 1, 1, '新增用户权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('5004', '5001', 'user:update', '编辑用户', 'permission', NULL, NULL, 3, 1, 1, '编辑用户权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('5005', '5001', 'user:delete', '删除用户', 'permission', NULL, NULL, 4, 1, 1, '删除用户权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('5006', '5001', 'user:resetPassword', '重置密码', 'permission', NULL, NULL, 5, 1, 1, '重置密码权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 角色管理
('6001', 'root', 'role-module', '角色管理', 'module', 'Lock', '/roles', 6, 1, 1, '角色管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('6002', '6001', 'role:manage', '管理角色', 'permission', NULL, NULL, 1, 1, 1, '管理角色权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 操作日志
('7001', 'root', 'log-module', '操作日志', 'module', 'Notebook', '/logs', 7, 1, 1, '操作日志模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('7002', '7001', 'log:view', '查看日志', 'permission', NULL, NULL, 1, 1, 1, '查看日志权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 会员管理
('8001', 'root', 'member-module', '会员管理', 'module', 'User', '/members', 8, 1, 1, '会员管理模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('8002', '8001', 'member:view', '查看会员', 'permission', NULL, NULL, 1, 1, 1, '查看会员权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('8003', '8001', 'member:create', '新增会员', 'permission', NULL, NULL, 2, 1, 1, '新增会员权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('8004', '8001', 'member:update', '编辑会员', 'permission', NULL, NULL, 3, 1, 1, '编辑会员权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('8005', '8001', 'member:delete', '删除会员', 'permission', NULL, NULL, 4, 1, 1, '删除会员权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('8006', '8001', 'member:recharge', '会员充值', 'permission', NULL, NULL, 5, 1, 1, '会员充值权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 会员等级
('9001', 'root', 'member-level-module', '会员等级', 'module', 'Star', '/member-levels', 9, 1, 1, '会员等级模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('9002', '9001', 'member:level', '管理等级', 'permission', NULL, NULL, 1, 1, 1, '管理等级权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 系统设置
('10001', 'root', 'system-module', '系统设置', 'module', 'Tools', '/settings', 10, 1, 1, '系统设置模块', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('10002', '10001', 'system:config', '系统配置', 'permission', NULL, NULL, 1, 1, 1, '系统配置权限', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 3. 验证数据
SELECT
    parent_id,
    permission_type,
    COUNT(*) as count
FROM sys_permission
WHERE status = 1
GROUP BY parent_id, permission_type
ORDER BY parent_id, permission_type;
