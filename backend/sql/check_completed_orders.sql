-- 检查是否有 completed 状态的订单
SELECT COUNT(*) as completed_count, 
       MIN(id) as first_id,
       MAX(created_at) as latest_created_at
FROM `order`
WHERE status = 'completed';

-- 查看 completed 订单详情
SELECT id, table_id, table_name, status, created_at, start_time, amount
FROM `order`
WHERE status = 'completed'
ORDER BY created_at DESC
LIMIT 15;

-- 检查所有订单状态分布
SELECT status, COUNT(*) as count 
FROM `order` 
GROUP BY status;
