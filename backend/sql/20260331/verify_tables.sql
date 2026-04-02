-- =============================================
-- 会员管理功能 - 数据库表验证脚本
-- 版本: 1.0
-- 日期: 2026-03-31
-- =============================================
--
-- 使用说明:
-- 1. 使用MySQL客户端连接数据库: mysql -u root -p pindou_timer
-- 2. 执行此脚本: source verify_tables.sql
--    或者直接执行以下SQL语句
-- =============================================

USE `pindou_timer`;

-- 1. 检查会员等级表
SELECT '=== 会员等级表结构验证 ===' AS '';
SHOW CREATE TABLE `biz_member_level`;

-- 2. 检查会员表
SELECT '=== 会员表结构验证 ===' AS '';
SHOW CREATE TABLE `biz_member`;

-- 3. 检查订单表新增字段
SELECT '=== 订单表会员字段验证 ===' AS '';
DESCRIBE `biz_order`;

-- 4. 验证外键约束
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'pindou_timer'
  AND REFERENCED_TABLE_NAME IN ('biz_member_level', 'biz_member');

-- 5. 验证唯一索引
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'pindou_timer'
  AND TABLE_NAME IN ('biz_member', 'biz_member_level')
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 6. 预期输出验证
SELECT '=== 预期验证结果 ===' AS '';
SELECT 'biz_member_level表应包含以下字段:' AS 'Check 1';
SELECT '  - id, name, min_amount, max_amount, discount_rate, sort, created_at, updated_at' AS '';

SELECT 'biz_member表应包含以下字段:' AS 'Check 2';
SELECT '  - id, name, phone, address, total_amount, level_id, created_at, updated_at' AS '';

SELECT 'biz_order表应包含以下新字段:' AS 'Check 3';
SELECT '  - member_id (NULLable), original_amount' AS '';

SELECT '应存在以下约束:' AS 'Check 4';
SELECT '  - biz_member.uk_phone (唯一索引)' AS '';
SELECT '  - biz_member.level_id → biz_member_level.id (外键)' AS '';

-- =============================================
-- 验证检查清单
-- =============================================
--
-- [ ] biz_member_level表存在且结构正确
-- [ ] biz_member表存在且结构正确
-- [ ] biz_order表包含member_id和original_amount字段
-- [ ] biz_member.phone字段有唯一索引uk_phone
-- [ ] biz_member.level_id有外键约束指向biz_member_level.id
-- [ ] 所有必需的索引都已创建
-- =============================================
