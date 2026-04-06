-- =============================================
-- 豆屿温柔集管理系统 - 初始数据脚本
-- 版本: 1.0
-- =============================================

USE `pindou_timer`;

-- =============================================
-- 1. 预设角色数据
-- =============================================

-- 超级管理员
INSERT INTO `sys_role` (`id`, `name`, `code`, `permissions`, `sort`, `status`, `is_built_in`, `description`, `created_at`, `updated_at`)
VALUES (
  'a0000000-0000-0000-0000-000000000001',
  '超级管理员',
  'super_admin',
  '["*"]',
  1,
  1,
  1,
  '系统最高权限',
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
);

-- 店长
INSERT INTO `sys_role` (`id`, `name`, `code`, `permissions`, `sort`, `status`, `is_built_in`, `description`, `created_at`, `updated_at`)
VALUES (
  'a0000000-0000-0000-0000-000000000002',
  '店长',
  'manager',
  '["table:view","table:start","table:pause","table:end","table:config","order:view","order:detail","order:export","report:view","system:config","user:view","user:create","user:edit","role:manage","log:view","remind:ignore"]',
  2,
  1,
  1,
  '管理权限',
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
);

-- 店员
INSERT INTO `sys_role` (`id`, `name`, `code`, `permissions`, `sort`, `status`, `is_built_in`, `description`, `created_at`, `updated_at`)
VALUES (
  'a0000000-0000-0000-0000-000000000003',
  '店员',
  'staff',
  '["table:view","table:start","table:pause","table:end","order:view","order:detail","report:view","log:view","remind:ignore"]',
  3,
  1,
  1,
  '操作权限',
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
);

-- 只读用户
INSERT INTO `sys_role` (`id`, `name`, `code`, `permissions`, `sort`, `status`, `is_built_in`, `description`, `created_at`, `updated_at`)
VALUES (
  'a0000000-0000-0000-0000-000000000004',
  '只读用户',
  'readonly',
  '["table:view","order:view","order:detail","report:view"]',
  4,
  1,
  1,
  '查看权限',
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
);

-- =============================================
-- 2. 预设用户数据
-- =============================================

-- 超级管理员账号：admin / 123456
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `role_id`, `status`, `created_at`, `updated_at`)
VALUES (
  UUID(),
  'admin',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
  '超级管理员',
  'a0000000-0000-0000-0000-000000000001',
  1,
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
);

-- 店长账号：manager / 123456
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `role_id`, `status`, `created_at`, `updated_at`)
VALUES (
  UUID(),
  'manager',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
  '店长',
  'a0000000-0000-0000-0000-000000000002',
  1,
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
);

-- 店员账号：staff / 123456
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `role_id`, `status`, `created_at`, `updated_at`)
VALUES (
  UUID(),
  'staff',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
  '店员',
  'a0000000-0000-0000-0000-000000000003',
  1,
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
);

-- =============================================
-- 3. 预设桌台数据（20个桌台）
-- =============================================

INSERT INTO `biz_table` (`id`, `name`, `status`, `created_at`, `updated_at`)
SELECT
  t.n AS id,
  CONCAT(t.n, '号桌') AS name,
  'idle' AS status,
  UNIX_TIMESTAMP() * 1000 AS created_at,
  UNIX_TIMESTAMP() * 1000 AS updated_at
FROM (
  SELECT 1 AS n UNION ALL
  SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
  SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
  SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL
  SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) t;

-- =============================================
-- 4. 预设系统配置数据
-- =============================================

-- 桌台数量配置
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`)
VALUES (
  UUID(),
  'table_count',
  '20',
  '桌台总数',
  UNIX_TIMESTAMP() * 1000
);

-- 计费规则配置
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`)
VALUES (
  UUID(),
  'billing_rule',
  '{"type":"hour","pricePerHour":30,"pricePerMinute":0.5,"overtimeRate":1.5}',
  '计费规则',
  UNIX_TIMESTAMP() * 1000
);

-- 提醒配置
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`)
VALUES (
  UUID(),
  'remind_config',
  '{"threshold":5,"soundEnabled":true,"repeatInterval":30,"expiringCloseTime":3,"timeoutCloseTime":5}',
  '提醒配置',
  UNIX_TIMESTAMP() * 1000
);

-- 会话超时配置
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`)
VALUES (
  UUID(),
  'session_timeout',
  '1800',
  '会话超时时间（秒）',
  UNIX_TIMESTAMP() * 1000
);

-- =============================================
-- 数据初始化完成
-- =============================================

-- 显示初始化结果
SELECT '=====================================' AS '';
SELECT '数据初始化完成！' AS '';
SELECT '=====================================' AS '';
SELECT '预设账号信息：' AS '';
SELECT CONCAT('账号: admin, 密码: 123456 (超级管理员)') AS '';
SELECT CONCAT('账号: manager, 密码: 123456 (店长)') AS '';
SELECT CONCAT('账号: staff, 密码: 123456 (店员)') AS '';
SELECT '=====================================' AS '';
