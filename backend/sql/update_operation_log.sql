-- 更新操作日志表结构以匹配实体类
-- 执行前请备份数据！

USE `pindou_timer`;

-- 修改现有字段
ALTER TABLE `sys_operation_log`
  CHANGE COLUMN `action` `operation` VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
  CHANGE COLUMN `action_name` `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  CHANGE COLUMN `execute_time` `duration` BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）',
  CHANGE COLUMN `user_agent` `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理';

-- 添加新字段
ALTER TABLE `sys_operation_log`
  ADD COLUMN `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块' AFTER `id`,
  ADD COLUMN `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法' AFTER `description`,
  ADD COLUMN `params` TEXT DEFAULT NULL COMMENT '请求参数' AFTER `method`,
  ADD COLUMN `result` TEXT DEFAULT NULL COMMENT '响应结果' AFTER `params`,
  ADD COLUMN `status` TINYINT(1) DEFAULT 1 COMMENT '操作状态：0-失败，1-成功' AFTER `ip`,
  ADD COLUMN `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息' AFTER `status`;

-- 添加索引
ALTER TABLE `sys_operation_log`
  ADD KEY `idx_module` (`module`),
  ADD KEY `idx_operation` (`operation`),
  ADD KEY `idx_status` (`status`);
