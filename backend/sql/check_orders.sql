-- 检查最近订单的会员信息
SELECT 
  o.id as order_id,
  o.table_name,
  o.member_id,
  o.original_amount,
  o.amount,
  o.status,
  m.name as member_name,
  ml.name as level_name,
  ml.discount_rate
FROM biz_order o
LEFT JOIN biz_member m ON o.member_id = m.id
LEFT JOIN biz_member_level ml ON m.level_id = ml.id
ORDER BY o.created_at DESC
LIMIT 10;
