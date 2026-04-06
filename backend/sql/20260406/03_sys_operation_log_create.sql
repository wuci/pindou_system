-- ================================================================================
-- 操作日志表 (sys_operation_log) 建表语句
-- 整合了所有相关 SQL 文件，确保字段完整
-- @author wuci
-- @date 2026-04-06
-- ================================================================================

USE `pindou_timer`;

-- 删除旧表（如果需要重建，请先备份数据！）
DROP TABLE IF EXISTS `sys_operation_log`;

CREATE TABLE `sys_operation_log` (
  `id` VARCHAR(36) NOT NULL COMMENT '日志ID（UUID）',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
  `operation` VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  `user_id` VARCHAR(36) DEFAULT NULL COMMENT '操作用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `params` TEXT DEFAULT NULL COMMENT '请求参数',
  `result` TEXT DEFAULT NULL COMMENT '响应结果',
  `duration` BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `status` TINYINT(1) DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `content` TEXT DEFAULT NULL COMMENT '操作内容（兼容旧版）',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标类型：table/order/user/role/config',
  `target_id` VARCHAR(36) DEFAULT NULL COMMENT '目标ID',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_operation` (`operation`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 验证表结构
DESCRIBE `sys_operation_log`;
