#!/bin/bash
# 检查操作日志是否正常工作

echo "检查最近的操作日志记录..."
echo ""

# 方法1: 如果有 mysql 客户端，直接查询
# mysql -uroot -p123456 -D pindou_timer -e "SELECT id, module, operation, description, status, created_at FROM sys_operation_log ORDER BY created_at DESC LIMIT 5;"

# 方法2: 通过 API 查询（需要有效token）
# curl -H "Authorization: Bearer YOUR_TOKEN" "http://localhost:9026/api/logs?page=1&pageSize=5"

# 方法3: 检查后端日志中的记录信息
echo "请检查以下位置："
echo "1. 数据库表: SELECT * FROM sys_operation_log ORDER BY created_at DESC LIMIT 5;"
echo "2. 后端日志中是否有 '操作日志保存成功' 的消息"
echo "3. 后端日志中是否有异常信息"
echo ""

# 显示最近的表结构
echo "当前 sys_operation_log 表应该包含以下字段:"
echo "- id"
echo "- user_id"
echo "- username"
echo "- operation"
echo "- description"
echo "- content (新增，兼容旧表结构)"
echo "- method"
echo "- params"
echo "- result"
echo "- duration"
echo "- ip"
echo "- user_agent"
echo "- status"
echo "- error_msg"
echo "- module"
echo "- target_type (可选)"
echo "- target_id (可选)"
echo "- created_at"
