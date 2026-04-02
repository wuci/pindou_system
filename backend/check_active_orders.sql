-- 检查是否有 active 状态的订单
SELECT COUNT(*) as active_count, 
       MIN(id) as first_id,
       MAX(created_at) as latest_created_at
FROM `order`
WHERE status = 'active';

-- 查看 active 订单详情
SELECT id, table_id, table_name, status, created_at, start_time
FROM `order`
WHERE status = 'active'
ORDER BY created_at DESC
LIMIT 5;
