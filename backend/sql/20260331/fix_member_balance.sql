-- =============================================
-- 检查并修复会员余额字段
-- 版本: 1.0
-- 日期: 2026-03-31
-- =============================================

USE `pindou_timer`;

-- 1. 检查 biz_member 表是否有 balance 字段
SELECT '=== 检查 biz_member 表结构 ===' AS '';
DESCRIBE `biz_member`;

-- 2. 如果 balance 字段不存在，添加它
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'pindou_timer'
    AND TABLE_NAME = 'biz_member'
    AND COLUMN_NAME = 'balance'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `biz_member` ADD COLUMN `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT ''会员余额（元）'' AFTER `total_amount`',
    'SELECT ''字段 balance 已存在，跳过添加'' AS result'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 验证 balance 字段是否已添加
SELECT '=== 验证 balance 字段 ===' AS '';
DESCRIBE `biz_member`;

-- 4. 检查充值记录表是否存在
SELECT '=== 检查充值记录表 ===' AS '';
SHOW TABLES LIKE 'biz_recharge_record';

-- 5. 如果充值记录表不存在，创建它
SET @table_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = 'pindou_timer'
    AND TABLE_NAME = 'biz_recharge_record'
);

SET @sql = IF(@table_exists = 0,
    'CREATE TABLE `biz_recharge_record` (
      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT ''充值记录ID'',
      `member_id` BIGINT NOT NULL COMMENT ''会员ID'',
      `member_name` VARCHAR(50) NOT NULL COMMENT ''会员姓名（冗余）'',
      `member_phone` VARCHAR(20) NOT NULL COMMENT ''会员手机号（冗余）'',
      `amount` DECIMAL(10,2) NOT NULL COMMENT ''充值金额'',
      `balance_before` DECIMAL(10,2) NOT NULL COMMENT ''充值前余额'',
      `balance_after` DECIMAL(10,2) NOT NULL COMMENT ''充值后余额'',
      `payment_method` VARCHAR(20) NOT NULL DEFAULT ''cash'' COMMENT ''支付方式：cash-现金,wechat-微信,alipay-支付宝,card-刷卡'',
      `remark` VARCHAR(200) DEFAULT NULL COMMENT ''备注'',
      `operator_id` BIGINT DEFAULT NULL COMMENT ''操作员ID'',
      `operator_name` VARCHAR(50) DEFAULT NULL COMMENT ''操作员姓名'',
      `created_at` BIGINT NOT NULL COMMENT ''创建时间（毫秒时间戳）'',
      PRIMARY KEY (`id`),
      KEY `idx_member_id` (`member_id`),
      KEY `idx_created_at` (`created_at`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT=''会员充值记录表''',
    'SELECT ''表 biz_recharge_record 已存在，跳过创建'' AS result'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 检查消费记录表是否存在
SELECT '=== 检查消费记录表 ===' AS ';
SHOW TABLES LIKE 'biz_consumption_record';

-- 7. 如果消费记录表不存在，创建它
SET @table_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = 'pindou_timer'
    AND TABLE_NAME = 'biz_consumption_record'
);

SET @sql = IF(@table_exists = 0,
    'CREATE TABLE `biz_consumption_record` (
      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT ''消费记录ID'',
      `member_id` BIGINT NOT NULL COMMENT ''会员ID'',
      `member_name` VARCHAR(50) NOT NULL COMMENT ''会员姓名（冗余）'',
      `member_phone` VARCHAR(20) NOT NULL COMMENT ''会员手机号（冗余）'',
      `order_id` VARCHAR(50) DEFAULT NULL COMMENT ''关联订单ID'',
      `amount` DECIMAL(10,2) NOT NULL COMMENT ''消费金额'',
      `balance_before` DECIMAL(10,2) NOT NULL COMMENT ''消费前余额'',
      `balance_after` DECIMAL(10,2) NOT NULL COMMENT ''消费后余额'',
      `remark` VARCHAR(200) DEFAULT NULL COMMENT ''备注'',
      `created_at` BIGINT NOT NULL COMMENT ''创建时间（毫秒时间戳）'',
      PRIMARY KEY (`id`),
      KEY `idx_member_id` (`member_id`),
      KEY `idx_order_id` (`order_id`),
      KEY `idx_created_at` (`created_at`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT=''会员余额消费记录表''',
    'SELECT ''表 biz_consumption_record 已存在，跳过创建'' AS result'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8. 为现有会员初始化余额（如果余额为NULL）
UPDATE `biz_member`
SET `balance` = 0.00
WHERE `balance` IS NULL OR `balance` = 0;

-- 9. 显示当前会员状态
SELECT '=== 当前会员列表（前10条）===' AS '';
SELECT
  id,
  name,
  phone,
  total_amount AS '累计消费',
  balance AS '余额',
  level_id
FROM `biz_member`
ORDER BY created_at DESC
LIMIT 10;

SELECT '=== 修复完成！ ===' AS '';
