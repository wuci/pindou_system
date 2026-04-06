-- =============================================
-- 豆屿温柔集管理系统 - 初始化数据脚本
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
TRUNCATE TABLE `biz_discount`;
TRUNCATE TABLE `design_store_rules`;
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
('system_param', 'system', 'system:param', '系统参数设置', 'permission', '', '', 3, 1, 1, '系统参数配置', 1775205986000, 1775205986000, 0),
('system_discount', 'system', 'system:discount', '折扣设置', 'permission', '', '', 4, 1, 1, '配置折扣规则', 1775205986000, 1775205986000, 0),
-- 店铺规则模块权限
('store_rules', '0', 'store:rules:view', '店铺规则', 'module', 'Notebook', '/store-rules', 11, 1, 1, '店铺规则管理模块', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_view', 'store_rules', 'store:rules:view', '查看规则', 'permission', '', '', 1, 1, 1, '查看店铺规则列表', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_create', 'store_rules', 'store:rules:create', '新增规则', 'permission', '', '', 2, 1, 1, '创建新规则', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_update', 'store_rules', 'store:rules:update', '编辑规则', 'permission', '', '', 3, 1, 1, '编辑规则内容', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_delete', 'store_rules', 'store:rules:delete', '删除规则', 'permission', '', '', 4, 1, 1, '删除规则', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
('store_rules_toggle', 'store_rules', 'store:rules:toggle', '启用/禁用', 'permission', '', '', 5, 1, 1, '切换规则启用状态', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0);

-- =============================================
-- 3. 角色表 (sys_role)
-- =============================================
INSERT INTO `sys_role` (id, name, code, permissions, sort, status, is_built_in, description, created_at, updated_at, deleted_at) VALUES
('a0000000-0000-0000-0000-000000000001', '超级管理员', 'super_admin', '["*"]', 1, 1, 1, '系统最高权限', 1774583841000, 1774583841000, 0),
('a0000000-0000-0000-0000-000000000002', '店长', 'manager', '["dashboard:view", "table:category", "table:config", "table:start", "table:end", "table:reserve", "table:bill", "table:extend", "table:pause", "order:view", "order:detail", "order:export", "statistics:view", "member:create", "member:update", "member:recharge", "member:record", "system:rule", "system:discount", "store:rules:update"]', 2, 1, 1, '管理权限', 1774583841000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
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
(1, '大厅', 'grid', 1, '大厅区域桌台', FROM_UNIXTIME(1774596555000 / 1000), FROM_UNIXTIME(1774596555000 / 1000)),
(2, '包间', 'office-building', 2, '包间桌台', FROM_UNIXTIME(1774596555000 / 1000), FROM_UNIXTIME(1774596555000 / 1000));

-- =============================================
-- 6. 会员等级配置表 (biz_member_level)
-- =============================================
INSERT INTO `biz_member_level` (id, name, min_amount, max_amount, discount_rate, sort, created_at, updated_at) VALUES
(1, '豆豆萌新', 0.00, 300.00, 0.950, 1, 1774959221249, 1774959221249),
(2, '熨烫能手', 300.01, 1000.00, 0.900, 2, 1774959221249, 1774959221249),
(3, '像素匠人', 1000.01, 3000.00, 0.850, 3, 1774959221249, 1774959221249),
(4, '熔豆典藏', 3000.01, NULL, 0.800, 4, 1774959221249, 1774959221249);

-- =============================================
-- 7. 折扣设置表 (biz_discount)
-- 说明：默认插入两个折扣示例
-- =============================================
INSERT INTO `biz_discount` (id, name, type, discount_rate, min_amount, status, sort, description, created_at, updated_at, deleted_at) VALUES
(UUID(), '全场9折', 1, 0.900, NULL, 1, 1, '全场通用9折优惠', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0),
(UUID(), '全场8折', 1, 0.800, NULL, 0, 2, '全场通用8折优惠', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 0);

-- =============================================
-- 8. 店铺规则表 (design_store_rules)
-- 说明：默认插入15条规则数据
-- =============================================
INSERT INTO `design_store_rules` (`id`, `category`, `title`, `content`, `rule_type`, `sort_order`, `is_enabled`, `created_at`, `created_by`) VALUES
-- 套餐规则
('1', 'packages', '基础套餐', '[
  {"name":"轻体验包","price":"¥39","content":"小号模板1个 + 拼豆200颗 + 基础工具1套 + 普通熨烫1次","suitable":"初次体验者","duration":"30-45分钟"},
  {"name":"标准畅玩包","price":"¥68","content":"中号模板1个 + 拼豆500颗 + 基础工具1套 + 普通熨烫1次 + 钥匙扣配件1个","suitable":"单人/情侣","duration":"45-90分钟"},
  {"name":"家庭时光包","price":"¥128","content":"大号模板2个 + 拼豆800颗 + 基础工具2套 + 普通熨烫2次 + 配件任选2个","suitable":"亲子/2-3人","duration":"60-120分钟"},
  {"name":"创作大师包","price":"¥188","content":"大号模板任选2个 + 拼豆1500颗 + 工具全套 + 普通熨烫2次 + 配件任选3个 + 相框1个","suitable":"深度爱好者","duration":"90-180分钟"}
]', 'table', 1, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('2', 'packages', '计费说明', '<ul><li><strong>计时加时：</strong>超出套餐规定时间，按 ¥15/30分钟 收取（不足30分钟按30分钟计）</li><li><strong>材料补购：</strong>拼豆补充包 ¥10/200颗（限同色系），额外模板租赁 ¥5/个</li><li><strong>特殊配件：</strong>胸针、磁铁、挂绳等 ¥3-8/个</li><li><strong>二次熨烫：</strong>首次熨烫由店员免费协助；如需重烫或修改，收取 ¥5/次</li></ul>', 'list', 2, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('3', 'packages', '优惠与会员', '<ul><li>首次到店：关注小红书/大众点评，享首单9折</li><li>会员储值：充¥300送¥30，充¥500送¥80</li><li>积分规则：消费1元积1分，100分可抵扣¥5</li><li>团购优惠：3人及以上同行，每人立减¥10（不可叠加）</li></ul>', 'list', 3, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
-- 增值服务
('4', 'services', '免费服务', '<ul><li><strong>免费熨烫协助</strong> - 每位顾客享有1次由店员操作的专业熨烫</li><li><strong>图案参考图册</strong> - 店内提供200+图案模板供免费参考</li><li><strong>作品拍照服务</strong> - 店员可协助拍摄成品照，提供简易背景板</li><li><strong>问题咨询</strong> - 拼豆技巧、配色建议等免费指导</li><li><strong>Wi-Fi & 充电</strong> - 免费使用，扫码即可连接</li><li><strong>茶水自助</strong> - 柠檬水/薄荷水免费自取</li></ul>', 'list', 1, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('5', 'services', '付费升级服务', '[
  {"name":"设计师定制","price":"¥20-50","description":"根据顾客需求，设计师提供独家图案设计"},
  {"name":"作品装裱","price":"¥25起","description":"含相框/立体展示盒，可制作成摆件或挂饰"},
  {"name":"成品邮寄","price":"¥8（同城）","description":"作品完成后可代寄，需填写邮寄委托单"},
  {"name":"拼豆材料包","price":"¥29起","description":"含图纸+拼豆+模板，可带回家继续制作"},
  {"name":"企业团建定制","price":"面议","description":"含场地布置、专属物料、伴手礼等（需提前3天预约）"},
  {"name":"生日派对包场","price":"¥588起","description":"含2小时包场 + 8人标准包 + 主题装饰 + 生日小礼品"}
]', 'table', 2, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('6', 'services', '温柔帮烫', '<strong>✨ "温柔帮烫"服务：</strong>担心自己烫不好？店员可全程代烫，你只需完成拼豆部分。代烫作品不保证100%完美，但我们会尽最大努力。如需完美品，建议自行练习或选择"设计师定制"服务。', 'special', 3, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('7', 'services', '时光胶囊', '<strong>🎁 "时光胶囊"寄存：</strong>作品完成后可寄存店内，一年后凭存根领取，我们将为你附上当日日期戳和手写祝福卡（寄存费 ¥10/件/年）。', 'special', 4, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
-- 安全须知
('8', 'safety', '安全警告', '<p>本店使用电熨斗进行拼豆定型，熨斗底板工作温度可达150℃-220℃，存在烫伤、触电、火灾等风险。</p>', 'warning', 1, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('9', 'safety', '高温烫伤风险', '<ul><li><strong>风险告知：</strong>电熨斗工作时温度极高；刚熨烫完成的拼豆表面温度可达80℃-100℃</li><li><strong>安全要求：</strong>熨烫操作必须由成年顾客或监护人完成；儿童需在监护人全程看护下</li><li><strong>免责条款：</strong>如因顾客未遵守安全要求导致意外事故，由顾客自行承担全部责任</li></ul>', 'list', 2, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('10', 'safety', '小零件窒息风险', '<ul><li><strong>风险告知：</strong>拼豆颗粒直径约5mm，属于小零件，儿童误吞可能导致窒息</li><li><strong>安全要求：</strong>3岁以下儿童禁止入内；3-6岁儿童必须在监护人全程陪同下进行</li><li><strong>免责条款：</strong>如因监护人看护不当导致意外，本店不承担任何法律责任</li></ul>', 'list', 3, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('11', 'safety', '材质与呼吸道健康', '<ul><li><strong>风险告知：</strong>熨烫过程中可能释放微量挥发性气体，部分人群可能敏感</li><li><strong>建议：</strong>孕妇、哺乳期女性建议避免参与熨烫环节；哮喘患者请谨慎参与</li></ul>', 'list', 4, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
-- 其他规定
('12', 'other', '预约与占座', '<ul><li>周末及节假日建议提前1天预约，预约保留15分钟，超时自动取消</li><li>工作日可直接到店，如遇客满需排队等候</li><li>包场/团建活动需提前3天预约，并支付50%定金</li></ul>', 'list', 1, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('13', 'other', '退改规则', '<ul><li>未到店消费可随时退款（线上订单原路返回）</li><li>已开始拼豆制作但未完成，可按剩余材料折算退款</li><li>完成熨烫后视为服务完成，不接受退款</li></ul>', 'list', 2, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('14', 'other', '禁止事项', '<ul><li>🚫 禁止在店内吸烟、饮酒</li><li>🚫 禁止携带宠物（导盲犬除外）</li><li>🚫 禁止外带餐食（婴儿食品除外）</li><li>🚫 禁止制作涉及侵权、暴力、色情等违法违规图案</li><li>🚫 禁止将拼豆带出店外（购买的成品/材料包除外）</li></ul>', 'list', 3, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL),
('15', 'other', '财物保管', '<ul><li>请妥善保管个人贵重物品</li><li>本店提供自助储物柜（扫码使用），贵重物品请务必入柜</li><li>放置在公共区域的物品如有丢失，本店不承担赔偿责任</li></ul>', 'list', 4, 1, UNIX_TIMESTAMP(NOW()) * 1000, NULL);

-- =============================================
-- 9. 桌台表 (biz_table)
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
FROM biz_member_level
UNION ALL
SELECT
  '折扣',
  COUNT(*)
FROM biz_discount
UNION ALL
SELECT
  '店铺规则',
  COUNT(*)
FROM design_store_rules;
