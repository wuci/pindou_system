# 修改记录

## 日期
2026-04-06

## 修改文件
- `sql/20260403/01_建表脚本.sql`

## 修改内容
更新操作日志表 (sys_operation_log) 建表语句，补全缺失字段

## 字段变更

### 删除字段
- `action` → 改名为 `operation`
- `action_name` → 改名为 `description`
- `execute_time` → 改名为 `duration`

### 新增字段
| 字段 | 类型 | 说明 |
|------|------|------|
| module | VARCHAR(50) | 操作模块 |
| method | VARCHAR(200) | 请求方法 |
| params | TEXT | 请求参数 |
| result | TEXT | 响应结果 |
| status | TINYINT(1) | 操作状态：0-失败，1-成功 |
| error_msg | VARCHAR(500) | 错误信息 |

### 新增索引
- idx_module (module)
- idx_operation (operation)
- idx_status (status)

## 原因
实体类 OperationLog.java 包含完整字段定义，但建表语句缺少部分字段，导致结账时插入操作日志报错。

@author wuci
