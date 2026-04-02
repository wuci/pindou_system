-- 同步操作日志表结构以匹配实体类
-- 执行前请备份数据！

USE `pindou_timer`;

-- 删除旧的 content 字段（因为实体类中没有此字段）
ALTER TABLE `sys_operation_log` DROP COLUMN IF EXISTS `content`;

-- 删除旧的 target_type 和 target_id 字段（因为实体类中没有这些字段）
ALTER TABLE `sys_operation_log` DROP COLUMN IF EXISTS `target_type`;
ALTER TABLE `sys_operation_log` DROP COLUMN IF EXISTS `target_id`;

-- 确保 user_id 可以为空（因为某些操作可能没有用户信息）
ALTER TABLE `sys_operation_log` MODIFY COLUMN `user_id` VARCHAR(36) DEFAULT NULL COMMENT '操作用户ID';

-- 确保 username 可以为空
ALTER TABLE `sys_operation_log` MODIFY COLUMN `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名';

-- 确保 operation 可以为空
ALTER TABLE `sys_operation_log` MODIFY COLUMN `operation` VARCHAR(50) DEFAULT NULL COMMENT '操作类型';

-- 确保 description 可以为空且长度足够
ALTER TABLE `sys_operation_log` MODIFY COLUMN `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述';

-- 添加 module 字段（如果不存在）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块' AFTER `id`;

-- 添加 method 字段（如果不存在）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法' AFTER `description`;

-- 添加 params 字段（如果不存在）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `params` TEXT DEFAULT NULL COMMENT '请求参数' AFTER `method`;

-- 添加 result 字段（如果不存在）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `result` TEXT DEFAULT NULL COMMENT '响应结果' AFTER `params`;

-- 添加 status 字段（如果不存在）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `status` TINYINT(1) DEFAULT 1 COMMENT '操作状态：0-失败，1-成功' AFTER `ip`;

-- 添加 error_msg 字段（如果不存在）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息' AFTER `status`;

-- 添加索引（如果不存在）
ALTER TABLE `sys_operation_log`
ADD KEY IF NOT EXISTS `idx_module` (`module`),
ADD KEY IF NOT EXISTS `idx_operation` (`operation`),
ADD KEY IF NOT EXISTS `idx_status` (`status`);

-- 查看表结构确认
DESCRIBE `sys_operation_log`;
