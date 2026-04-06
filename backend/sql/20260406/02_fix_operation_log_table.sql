-- ================================================================================
-- 修复操作日志表结构
-- @author wuci
-- @date 2026-04-06
-- ================================================================================

USE `pindou_timer`;

-- 添加 module 字段（操作模块）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块' AFTER `id`;

-- 添加 method 字段（请求方法）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法' AFTER `description`;

-- 添加 params 字段（请求参数）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `params` TEXT DEFAULT NULL COMMENT '请求参数' AFTER `method`;

-- 添加 result 字段（响应结果）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `result` TEXT DEFAULT NULL COMMENT '响应结果' AFTER `params`;

-- 添加 error_msg 字段（错误信息）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息' AFTER `status`;

-- 添加 target_type 字段（目标类型）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标类型' AFTER `content`;

-- 添加 target_id 字段（目标ID）
ALTER TABLE `sys_operation_log`
ADD COLUMN IF NOT EXISTS `target_id` VARCHAR(36) DEFAULT NULL COMMENT '目标ID' AFTER `target_type`;

-- 添加索引
ALTER TABLE `sys_operation_log`
ADD KEY IF NOT EXISTS `idx_module` (`module`),
ADD KEY IF NOT EXISTS `idx_operation` (`operation`),
ADD KEY IF NOT EXISTS `idx_status` (`status`);

-- 查看表结构确认
DESCRIBE `sys_operation_log`;
