# 数据库脚本说明

## 目录结构

```
sql/
├── README.md      # 本说明文件
├── schema.sql     # 建表脚本
└── data.sql       # 初始数据脚本
```

## 使用说明

### 1. 创建数据库

```bash
# 方式一：使用MySQL命令行
mysql -u root -p -e "CREATE DATABASE pindou_timer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 方式二：登录MySQL后创建
mysql -u root -p
```

```sql
CREATE DATABASE pindou_timer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pindou_timer;
```

### 2. 执行建表脚本

```bash
mysql -u root -p pindou_timer < sql/schema.sql
```

或在MySQL客户端中：

```sql
SOURCE /path/to/sql/schema.sql;
```

### 3. 执行初始数据脚本

```bash
mysql -u root -p pindou_timer < sql/data.sql
```

或在MySQL客户端中：

```sql
SOURCE /path/to/sql/data.sql;
```

### 4. 一键执行（推荐）

创建一个批处理文件 `init_database.sh`：

```bash
#!/bin/bash

# 数据库配置
DB_USER="root"
DB_PASS="your_password"
DB_NAME="pindou_timer"
SQL_DIR="./sql"

# 创建数据库
mysql -u $DB_USER -p$DB_PASS -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入脚本
mysql -u $DB_USER -p$DB_PASS $DB_NAME < $SQL_DIR/schema.sql
mysql -u $DB_USER -p$DB_PASS $DB_NAME < $SQL_DIR/data.sql

echo "数据库初始化完成！"
```

Windows批处理文件 `init_database.bat`：

```batch
@echo off
set DB_USER=root
set DB_PASS=your_password
set DB_NAME=pindou_timer
set SQL_DIR=.\sql

echo 正在创建数据库...
mysql -u %DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo 正在导入建表脚本...
mysql -u %DB_USER% -p%DB_PASS% %DB_NAME% < %SQL_DIR%\schema.sql

echo 正在导入初始数据...
mysql -u %DB_USER% -p%DB_PASS% %DB_NAME% < %SQL_DIR%\data.sql

echo 数据库初始化完成！
pause
```

## 脚本说明

### schema.sql - 建表脚本

包含以下数据库表的创建：
- `sys_user` - 用户表
- `sys_role` - 角色表
- `biz_table` - 桌台表
- `biz_order` - 订单表
- `sys_operation_log` - 操作日志表
- `sys_config` - 系统配置表

### data.sql - 初始数据脚本

包含以下初始数据：
- 4个预设角色（超级管理员、店长、店员、只读用户）
- 3个预设用户（admin、manager、staff）
- 20个桌台（1-20号桌）
- 系统配置数据（桌台数量、计费规则、提醒配置、会话超时）

## 默认账号

初始化完成后，可以使用以下默认账号登录系统：

| 角色 | 用户名 | 密码 | 权限说明 |
|------|--------|------|----------|
| 超级管理员 | admin | 123456 | 系统最高权限 |
| 店长 | manager | 123456 | 管理权限（不含角色管理） |
| 店员 | staff | 123456 | 操作权限（计时、查看订单等） |

**重要：** 生产环境部署时请修改默认密码！

## 数据库版本要求

- MySQL 8.0+
- 字符集：utf8mb4
- 排序规则：utf8mb4_unicode_ci

## 注意事项

1. **字符集要求**：确保数据库和表的字符集为 `utf8mb4`，以支持表情符号等特殊字符
2. **时区设置**：数据库时区建议设置为 `Asia/Shanghai`
3. **SQL模式**：确保 `ONLY_FULL_GROUP_BY` 模式已启用
4. **外键约束**：已配置外键约束，确保数据完整性

## 备份与恢复

### 备份数据库

```bash
# 备份整个数据库
mysqldump -u root -p pindou_timer > backup_$(date +%Y%m%d_%H%M%S).sql

# 只备份数据（不含建表语句）
mysqldump -u root -p -t pindou_timer > data_$(date +%Y%m%d_%H%M%S).sql

# 只备份结构（不含数据）
mysqldump -u root -p -d pindou_timer > schema_$(date +%Y%m%d_%H%M%S).sql
```

### 恢复数据库

```bash
mysql -u root -p pindou_timer < backup_xxxxxxxx.sql
```

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2026-03-27 | 初始版本 |
